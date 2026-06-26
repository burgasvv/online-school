package org.burgas.courseservice.service

import jakarta.servlet.http.Part
import org.burgas.courseservice.dao.project.Project
import org.burgas.courseservice.dto.project.ProjectDependency
import org.burgas.courseservice.dto.project.ProjectRequest
import org.burgas.courseservice.dto.project.ProjectResponse
import org.burgas.courseservice.handler.ClientHandler
import org.burgas.courseservice.mapper.ProjectMapper
import org.burgas.courseservice.service.contract.CreateService
import org.burgas.courseservice.service.contract.DeleteService
import org.burgas.courseservice.service.contract.ReadService
import org.burgas.courseservice.service.contract.UpdateService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
class ProjectService : ReadService<UUID, Project, ProjectResponse>, CreateService<ProjectRequest, ProjectResponse>,
    UpdateService<ProjectRequest, ProjectResponse>, DeleteService<UUID> {

    private final val projectMapper: ProjectMapper
    private final val clientHandler: ClientHandler

    constructor(projectMapper: ProjectMapper, clientHandler: ClientHandler) {
        this.projectMapper = projectMapper
        this.clientHandler = clientHandler
    }

    override fun findEntity(id: UUID): Project {
        return projectMapper.projectRepository.findById(id)
            .orElseThrow { throw IllegalArgumentException("Project not found") }
    }

    override fun findById(id: UUID): ProjectResponse {
        return projectMapper.toResponse(findEntity(id))
    }

    fun findDependencyById(id: UUID): ProjectDependency {
        return projectMapper.toDependency(findEntity(id))
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun create(request: ProjectRequest): ProjectResponse {
        val project = projectMapper.projectRepository.save(projectMapper.toEntity(request))
        return projectMapper.toResponse(project)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun update(request: ProjectRequest): ProjectResponse {
        if (request.id == null) throw IllegalArgumentException("Request project id is null")
        val project = projectMapper.projectRepository.save(projectMapper.toEntity(request))
        return projectMapper.toResponse(project)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun delete(id: UUID) {
        val project = findEntity(id)
        projectMapper.projectRepository.delete(project)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    fun uploadDocument(projectId: UUID, part: Part) {
        val project = findEntity(projectId)
        val documentResponse = clientHandler.uploadDocument(part)
        if (project.taskId == null) {
            project.taskId = documentResponse.id
        } else {
            throw IllegalArgumentException("Task is already set")
        }
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    fun removeDocument(projectId: UUID) {
        val project = findEntity(projectId)
        if (project.taskId != null) {
            clientHandler.removeDocument(project.taskId)
        } else {
            throw IllegalArgumentException("Project task is already set")
        }
    }
}