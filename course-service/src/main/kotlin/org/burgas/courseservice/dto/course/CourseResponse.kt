package org.burgas.courseservice.dto.course

import org.burgas.courseservice.dto.Response
import org.burgas.courseservice.dto.identity.IdentityDependency
import org.burgas.courseservice.dto.project.ProjectDependency
import java.util.UUID

data class CourseResponse(
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val date: String? = null,
    val identities: Set<IdentityDependency>? = null,
    val projects: Set<ProjectDependency>? = null
) : Response
