package org.burgas.courseservice.dto.identity

import org.burgas.courseservice.dto.Response
import org.burgas.courseservice.dto.course.CourseDependency
import org.burgas.courseservice.dto.document.DocumentResponse
import org.burgas.courseservice.dto.image.ImageResponse
import java.util.UUID

data class IdentityResponse(
    val id: UUID? = null,
    val email: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val patronymic: String? = null,
    val about: String? = null,
    val image: ImageResponse? = null,
    val documents: Set<DocumentResponse>? = null,
    val courses: Set<CourseDependency>? = null
) : Response
