package org.burgas.courseservice.dto.project

import org.burgas.courseservice.dto.Response
import org.burgas.courseservice.dto.course.CourseDependency
import org.burgas.courseservice.dto.document.DocumentResponse
import java.util.UUID

data class ProjectResponse(
    val id: UUID? = null,
    val name: String? = null,
    val description: String? = null,
    val course: CourseDependency? = null,
    val link: String? = null,
    val date: String? = null,
    val task: DocumentResponse? = null
) : Response
