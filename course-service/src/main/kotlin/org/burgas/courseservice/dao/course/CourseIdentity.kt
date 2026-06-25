package org.burgas.courseservice.dao.course

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "course_identity", schema = "public")
class CourseIdentity {

    @EmbeddedId
    final var courseIdentityId: CourseIdentityId

    constructor(courseIdentityId: CourseIdentityId) {
        this.courseIdentityId = courseIdentityId
    }
}