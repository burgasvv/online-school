package org.burgas.courseservice.dto.course

import org.burgas.courseservice.dto.Request
import java.util.UUID

data class CourseIdentityRequest(
    val courseId: UUID,
    val identityId: UUID
) : Request
