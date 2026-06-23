package org.burgas.courseservice.dto.project

import org.burgas.courseservice.dto.Dependency
import java.util.UUID

data class ProjectDependency(
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val date: String? = null
) : Dependency
