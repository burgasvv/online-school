package org.burgas.courseservice.mapper

import org.burgas.courseservice.dao.project.Project
import org.burgas.courseservice.dto.project.ProjectDependency
import org.burgas.courseservice.dto.project.ProjectRequest
import org.burgas.courseservice.dto.project.ProjectResponse
import org.burgas.courseservice.handler.ClientHandler
import org.burgas.courseservice.mapper.contract.Mapper
import org.burgas.courseservice.repository.ProjectRepository
import org.springframework.beans.factory.ObjectFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

@Component
class ProjectMapper : Mapper<ProjectRequest, Project, ProjectDependency, ProjectResponse> {

    final val projectRepository: ProjectRepository
    private final val courseMapperObjectFactory: ObjectFactory<CourseMapper>
    private final val clientHandler: ClientHandler

    constructor(
        projectRepository: ProjectRepository,
        courseMapperObjectFactory: ObjectFactory<CourseMapper>,
        clientHandler: ClientHandler
    ) {
        this.projectRepository = projectRepository
        this.courseMapperObjectFactory = courseMapperObjectFactory
        this.clientHandler = clientHandler
    }

    private fun getCourseMapper(): CourseMapper = courseMapperObjectFactory.`object`

    override fun toEntity(request: ProjectRequest): Project {
        return projectRepository.findById(request.id ?: UUID(0,0))
            .map {
                Project().apply {
                    val findCourse = getCourseMapper().courseRepository
                        .findById(request.courseId ?: UUID(0, 0))
                        .orElse(null)
                    this.id = it.id
                    this.name = request.name ?: it.name
                    this.description = request.description ?: it.description
                    this.link = request.link ?: it.link
                    this.date = it.date
                    this.course = findCourse ?: it.course
                }
            }
            .orElseGet {
                Project().apply {
                    val findCourse = getCourseMapper().courseRepository
                        .findById(request.courseId ?: UUID(0, 0))
                        .orElse(null)
                    this.name = request.name!!
                    this.description = request.description!!
                    this.link = request.link!!
                    this.date = LocalDateTime.now()
                    this.course = findCourse
                }
            }
    }

    override fun toDependency(entity: Project): ProjectDependency {
        return ProjectDependency(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            date = entity.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm"))
        )
    }

    override fun toResponse(entity: Project): ProjectResponse {
        return ProjectResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            course = Optional.ofNullable(entity.course)
                .map { getCourseMapper().toDependency(it) }.orElse(null),
            link = entity.link,
            date = entity.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm")),
            task = clientHandler.getDocumentById(entity.taskId ?: UUID(0,0))
        )
    }
}