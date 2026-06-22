package org.burgas.service

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.content.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.burgas.client.RestClient
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.database.IdentityDocumentTable
import org.burgas.dto.DocumentResponse
import org.burgas.dto.IdentityRequest
import org.burgas.dto.IdentityResponse
import org.burgas.dto.ImageResponse
import org.burgas.redis.CacheHandler
import org.burgas.redis.RedisKeys
import org.burgas.service.contract.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.util.*

class IdentityService : CacheHandler<IdentityResponse>, CollectService<IdentityResponse>,
    ReadService<UUID, IdentityEntity, IdentityResponse>, CreateService<IdentityRequest, IdentityResponse>,
    UpdateService<IdentityRequest, IdentityResponse>, DeleteService<UUID> {

    private val redis = DatabaseConnection.jedis
    private val httpClient = RestClient.httpClient

    override suspend fun handleCache(response: IdentityResponse) {
        val identityKey = RedisKeys.IDENTITY_KEY.format(response.id)
        if (redis.exists(identityKey)) redis.del(identityKey)
    }

    override suspend fun findAll(): Set<IdentityResponse> = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.all().map { it.toResponse() }.toSet()
    }

    override suspend fun findEntity(id: UUID): IdentityEntity = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        IdentityEntity.findById(id)!!
    }

    override suspend fun findById(id: UUID): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        val identityKey = RedisKeys.IDENTITY_KEY.format(id)
        if (redis.exists(identityKey)) {
            Json.decodeFromString<IdentityResponse>(redis.get(identityKey))
        } else {
            val identityResponse = findEntity(id).toResponse()
            redis.set(identityKey, Json.encodeToString(identityResponse))
            identityResponse
        }
    }

    override suspend fun create(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityResponse = IdentityEntity.new { insert(request) }.toResponse()
        handleCache(identityResponse)
        val identityKey = RedisKeys.IDENTITY_KEY.format(identityResponse.id)
        redis.set(identityKey, Json.encodeToString(identityResponse))
        identityResponse
    }

    override suspend fun update(request: IdentityRequest): IdentityResponse = suspendTransaction(
        db = DatabaseConnection.postgres, transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    ) {
        val identityResponse = IdentityEntity.findByIdAndUpdate(request.id!!) { it.update(request) }!!.toResponse()
        handleCache(identityResponse)
        val identityKey = RedisKeys.IDENTITY_KEY.format(identityResponse.id)
        redis.set(identityKey, Json.encodeToString(identityResponse))
        identityResponse
    }

    override suspend fun delete(id: UUID) = suspendTransaction(
        db = DatabaseConnection.postgres, readOnly = true
    ) {
        val identityEntity = findEntity(id)
        handleCache(identityEntity.toResponse())
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
        handleCache(identityEntity.toResponse())
    }

    suspend fun removeImage(identityId: UUID) = suspendTransaction {
        val identityEntity = findEntity(identityId)
        httpClient.delete("http://localhost:9000/api/v1/images/remove") {
            parameter("imageId", identityEntity.imageId!!)
        }
        identityEntity.imageId = null
        handleCache(identityEntity.toResponse())
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
        handleCache(identityEntity.toResponse())
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
            handleCache(identityEntity.toResponse())
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
            handleCache(identityEntity.toResponse())
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
            handleCache(identityEntity.toResponse())
        } else {
            throw IllegalArgumentException("Statuses matched")
        }
    }
}