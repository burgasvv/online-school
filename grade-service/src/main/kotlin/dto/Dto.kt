package org.burgas.dto

import kotlinx.serialization.Serializable
import org.burgas.serialization.UUIDSerializer
import java.util.UUID

interface Request {
    val id: UUID?
}

interface Dependency {
    val id: UUID?
}

interface Response {
    val id: UUID?
}

@Serializable
data class ExceptionResponse(
    val status: String,
    val code: Int,
    val message: String?
)

enum class Authority {
    ADMIN, TEACHER, STUDENT
}

@Serializable
data class AuthToken(
    @Serializable(with = UUIDSerializer::class)
    val token: UUID,
    val authority: Authority
)