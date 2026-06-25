package org.burgas.service

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.content.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import org.burgas.client.RestClient
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.database.IdentityDocumentTable
import org.burgas.database.IdentityTable
import org.burgas.dto.*
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.util.*

class IdentityService : CollectService<IdentityResponse>,
    ReadService<UUID, IdentityEntity, IdentityResponse>, CreateService<IdentityRequest, IdentityResponse>,
    UpdateService<IdentityRequest, IdentityResponse>, DeleteService<UUID> {

    private val httpClient = RestClient.httpClient

    override suspend fun findAll(): Set<IdentityResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.all().map { it.toResponse() }.toSet()
    }

    suspend fun findDependenciesByIds(identityIds: List<UUID>): Set<IdentityDependency> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.find { IdentityTable.id inList identityIds }.map { it.toDependency() }.toSet()
    }

    override suspend fun findEntity(id: UUID): IdentityEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.findById(id)!!
    }

    override suspend fun findById(id: UUID): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(id).toResponse()
    }

    suspend fun findByIdNoCache(identityId: UUID): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        findEntity(identityId).toResponse()
    }

    override suspend fun create(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        IdentityEntity.new { insert(request) }.toResponse()
    }

    override suspend fun update(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        IdentityEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!.toResponse()
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        val identityEntity = findEntity(id)
        if (identityEntity.imageId != null) removeImage(identityId = identityEntity.id.value)
        identityEntity.delete()
    }

    suspend fun uploadImage(identityId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = findEntity(identityId)
        val imageResponse = httpClient.post("http://localhost:9000/api/v1/images/upload") {
            val fileItem = multiPartData.asFlow().filterIsInstance<PartData.FileItem>().first()
            setBody(MultiPartFormDataContent(listOf(fileItem)))
        }.body<ImageResponse>()
        identityEntity.imageId = imageResponse.id
    }

    suspend fun removeImage(identityId: UUID) = suspendTransaction {
        val identityEntity = findEntity(identityId)
        httpClient.delete("http://localhost:9000/api/v1/images/remove") {
            parameter("imageId", identityEntity.imageId!!)
        }
        identityEntity.imageId = null
    }

    suspend fun uploadDocument(identityId: UUID, multiPartData: MultiPartData) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = findEntity(identityId)
        val documentResponse = httpClient.post("http://localhost:9000/api/v1/documents/upload") {
            val fileItem = multiPartData.asFlow().filterIsInstance<PartData.FileItem>().first()
            setBody(MultiPartFormDataContent(listOf(fileItem)))
        }.body<DocumentResponse>()
        IdentityDocumentTable.insert {
            it[IdentityDocumentTable.identityId] = identityEntity.id.value
            it[IdentityDocumentTable.documentId] = documentResponse.id!!
        }
    }

    suspend fun removeDocument(identityId: UUID, documentId: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = findEntity(identityId)
        val operation = (IdentityDocumentTable.identityId eq identityEntity.id.value) and
                (IdentityDocumentTable.documentId eq documentId)
        val identityDocument = IdentityDocumentTable.selectAll().where { operation }.singleOrNull()
        if (identityDocument != null) {
            httpClient.delete("http://localhost:9000/api/v1/documents/delete") {
                parameter("documentId", identityDocument[IdentityDocumentTable.documentId])
            }
            IdentityDocumentTable.deleteWhere { operation }
        } else {
            throw IllegalArgumentException("Identity and document connection not found")
        }
    }

    suspend fun changePassword(identityRequest: IdentityRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = findEntity(identityRequest.id!!)
        if (!BCrypt.checkpw(identityRequest.password!!, identityEntity.password)) {
            identityEntity.password = BCrypt.hashpw(identityRequest.password, BCrypt.gensalt())
        } else {
            throw IllegalArgumentException("Passwords matched")
        }
    }

    suspend fun changeStatus(identityRequest: IdentityRequest) = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityEntity = findEntity(identityRequest.id!!)
        if (identityRequest.status!! != identityEntity.status) {
            identityEntity.status = identityRequest.status
        } else {
            throw IllegalArgumentException("Statuses matched")
        }
    }
}