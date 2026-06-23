package org.burgas.courseservice.dto.image

import java.util.UUID

data class ImageResponse(
    val id: UUID? = null,
    val name: String? = null,
    val contentType: String? = null,
    val preview: Boolean? = null,
    val size: Long? = null
)
