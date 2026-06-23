package org.burgas.courseservice.repository

import org.burgas.courseservice.dao.project.Project
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, UUID> {

    @EntityGraph(value = "project-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    override fun findById(id: UUID): Optional<Project>
}