package org.burgas.courseservice.dto.document

import java.util.UUID

data class DocumentResponse(
    val id: UUID? = null,
    val name: String? = null,
    val contentType: String? = null,
    val size: Long? = null
)