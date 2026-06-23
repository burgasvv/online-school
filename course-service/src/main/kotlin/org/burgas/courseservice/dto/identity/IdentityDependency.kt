package org.burgas.courseservice.dto.identity

import org.burgas.courseservice.dto.image.ImageResponse
import java.util.UUID

data class IdentityDependency(
    val id: UUID? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val about: String? = null,
    val image: ImageResponse? = null
)