package org.burgas.courseservice.dto.course

import org.burgas.courseservice.dto.Dependency
import java.util.UUID

data class CourseDependency(
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val date: String? = null
) : Dependency
