package org.burgas.courseservice.dto.project

import org.burgas.courseservice.dto.Request
import java.util.UUID

data class ProjectRequest(
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val courseId: UUID? = null,
    val link: String? = null
) : Request
