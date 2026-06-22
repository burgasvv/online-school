package org.burgas.dto

import kotlinx.serialization.Serializable
import org.burgas.database.Authority
import org.burgas.serialization.UUIDSerializer
import java.util.UUID

interface Request

interface Dependency

interface Response

@Serializable
data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String
) : Response

@Serializable
data class CsrfToken(
    @Serializable(with = UUIDSerializer::class)
    val token: UUID
) : Response

@Serializable
data class AuthToken(val token: String)

@Serializable
data class ImageResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val contentType: String? = null,
    val preview: Boolean? = null,
    val size: Long? = null
) : Response

@Serializable
data class DocumentRequest(
    val documentIds: Set<@Serializable(with = UUIDSerializer::class) UUID>? = null
) : Request

@Serializable
data class DocumentResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val contentType: String? = null,
    val size: Long? = null
) : Response

@Serializable
data class IdentityRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val authority: Authority? = null,
    val email: String? = null,
    val password: String? = null,
    val status: Boolean? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val about: String? = null
) : Request

@Serializable
data class IdentityDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val about: String? = null,
    val image: ImageResponse? = null
) : Dependency

@Serializable
data class IdentityResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val about: String? = null,
    val image: ImageResponse? = null,
    val documents: Set<DocumentResponse>? = null
) : Response