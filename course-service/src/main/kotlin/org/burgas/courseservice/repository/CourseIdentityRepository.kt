package org.burgas.courseservice.repository

import org.burgas.courseservice.dao.course.CourseIdentity
import org.burgas.courseservice.dao.course.CourseIdentityId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CourseIdentityRepository : JpaRepository<CourseIdentity, CourseIdentityId> {

    @Query(
        nativeQuery = true,
        value = "select ci.identity_id from course_identity ci where ci.course_id = :courseId"
    )
    fun findIdentityIdsByCourseId(courseId: UUID): List<UUID>

    @Query(
        nativeQuery = true,
        value = "select ci.course_id from course_identity ci where ci.identity_id = :identityId"
    )
    fun findCourseIdsByIdentityId(identityId: UUID): List<UUID>
}