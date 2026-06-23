package org.burgas.courseservice.dto.course

import org.burgas.courseservice.dto.Request
import java.util.UUID

data class CourseRequest(
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null
) : Request
