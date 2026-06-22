package org.burgas.dao

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.burgas.client.RestClient
import org.burgas.database.IdentityDocumentTable
import org.burgas.database.IdentityTable
import org.burgas.dto.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import org.jetbrains.exposed.v1.jdbc.select
import org.mindrot.jbcrypt.BCrypt
import java.util.*

interface Dao

interface Creator<R : Request> {
    fun insert(request: R)
}

interface Modifier<R : Request> {
    fun update(request: R)
}

interface DependencyMapper<D : Dependency> {
    suspend fun toDependency(): D
}

interface ResponseMapper<R : Response> {
    suspend fun toResponse(): R
}

class IdentityEntity(id: EntityID<UUID>) : UUIDEntity(id), Dao,
    DependencyMapper<IdentityDependency>, ResponseMapper<IdentityResponse>,
    Creator<IdentityRequest>, Modifier<IdentityRequest> {

    companion object : UUIDEntityClass<IdentityEntity>(IdentityTable)

    val httpClient = RestClient.httpClient

    var authority by IdentityTable.authority
    var password by IdentityTable.password
    var email by IdentityTable.email
    var status by IdentityTable.status
    var firstname by IdentityTable.firstname
    var lastname by IdentityTable.lastname
    var patronymic by IdentityTable.patronymic
    var about by IdentityTable.about
    var imageId by IdentityTable.imageId

    override fun insert(request: IdentityRequest) {
        this.authority = request.authority!!
        this.email = request.email!!
        this.password = BCrypt.hashpw(request.password!!, BCrypt.gensalt())
        this.status = request.status ?: true
        this.firstname = request.firstname!!
        this.lastname = request.lastname!!
        this.patronymic = request.patronymic!!
        this.about = request.about
    }

    override fun update(request: IdentityRequest) {
        this.authority = request.authority ?: this.authority
        this.email = request.email ?: this.email
        this.firstname = request.firstname ?: this.firstname
        this.lastname = request.lastname ?: this.lastname
        this.patronymic = request.patronymic ?: this.patronymic
        this.about = request.about ?: this.about
    }

    override suspend fun toDependency(): IdentityDependency {
        val imageResponse = httpClient.get("http://localhost:9000/api/v1/images/by-id") {
            parameter("imageId", imageId ?: UUID(0,0))
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }.body<ImageResponse?>()

        return IdentityDependency(
            id = this.id.value,
            email = this.email,
            firstname = this.firstname,
            lastname = this.lastname,
            patronymic = this.patronymic,
            about = this.about,
            image = imageResponse
        )
    }

    override suspend fun toResponse(): IdentityResponse {
        val image = httpClient.get("http://localhost:9000/api/v1/images/by-id") {
            parameter("imageId", imageId ?: UUID(0,0))
            header(HttpHeaders.Host, "localhost:9000")
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }.body<ImageResponse?>()

        val documentIds = IdentityDocumentTable.select(IdentityDocumentTable.documentId)
            .where { IdentityDocumentTable.identityId eq id.value }
            .map { it[IdentityDocumentTable.documentId] }
            .toSet()
        val documentRequest = DocumentRequest(documentIds = documentIds)
        val documents = httpClient.get("http://localhost:9000/api/v1/documents/by-ids") {
            header(HttpHeaders.Host, "localhost:9000")
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(documentRequest)
        }.body<Set<DocumentResponse>?>()

        return IdentityResponse(
            id = this.id.value,
            email = this.email,
            firstname = this.firstname,
            lastname = this.lastname,
            patronymic = this.patronymic,
            about = this.about,
            image = image,
            documents = documents
        )
    }
}