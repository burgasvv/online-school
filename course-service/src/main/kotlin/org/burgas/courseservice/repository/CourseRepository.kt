package org.burgas.courseservice.repository

import org.burgas.courseservice.dao.course.Course
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CourseRepository : JpaRepository<Course, UUID> {

    @EntityGraph(value = "course-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    override fun findById(id: UUID): Optional<Course>

    @EntityGraph(value = "course-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    override fun findAll(): List<Course>
}