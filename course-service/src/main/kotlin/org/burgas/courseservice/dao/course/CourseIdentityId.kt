package org.burgas.courseservice.dao.course

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
class CourseIdentityId {

    @Column(name = "course_id", columnDefinition = "uuid")
    final var courseId: UUID

    @Column(name = "identity_id", columnDefinition = "uuid")
    final var identityId: UUID

    constructor(courseId: UUID, identityId: UUID) {
        this.courseId = courseId
        this.identityId = identityId
    }
}