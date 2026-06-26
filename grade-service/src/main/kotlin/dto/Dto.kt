package org.burgas.dto

import kotlinx.serialization.Serializable
import org.burgas.serialization.UUIDSerializer
import java.util.UUID

interface Request

interface Dependency

interface Response

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
data class AuthToken(val token: String, val authority: Authority)

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
data class ProjectDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val date: String? = null
) : Dependency

@Serializable
data class GradeRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val projectId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val studentId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val teacherId: UUID? = null,
    val description: String? = null,
    val mark: Int? = null
): Request

@Serializable
data class GradeDependency(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val student: IdentityDependency? = null,
    val teacher: IdentityDependency? = null,
    val description: String? = null,
    val mark: Int? = null,
    val date: String? = null
): Dependency

@Serializable
data class GradeResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val project: ProjectDependency? = null,
    val student: IdentityDependency? = null,
    val teacher: IdentityDependency? = null,
    val description: String? = null,
    val mark: Int? = null,
    val date: String? = null
): Response