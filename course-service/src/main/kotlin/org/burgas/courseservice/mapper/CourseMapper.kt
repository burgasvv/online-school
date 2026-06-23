package org.burgas.courseservice.mapper

import org.burgas.courseservice.dao.course.Course
import org.burgas.courseservice.dto.course.CourseDependency
import org.burgas.courseservice.dto.course.CourseRequest
import org.burgas.courseservice.dto.course.CourseResponse
import org.burgas.courseservice.handler.ClientHandler
import org.burgas.courseservice.mapper.contract.Mapper
import org.burgas.courseservice.repository.CourseIdentityRepository
import org.burgas.courseservice.repository.CourseRepository
import org.springframework.beans.factory.ObjectFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class CourseMapper : Mapper<CourseRequest, Course, CourseDependency, CourseResponse> {

    final val courseRepository: CourseRepository
    private final val clientHandler: ClientHandler
    private final val courseIdentityRepository: CourseIdentityRepository
    private final val projectMapperObjectFactory: ObjectFactory<ProjectMapper>

    constructor(
        courseRepository: CourseRepository,
        clientHandler: ClientHandler,
        courseIdentityRepository: CourseIdentityRepository,
        projectMapperObjectFactory: ObjectFactory<ProjectMapper>
    ) {
        this.courseRepository = courseRepository
        this.clientHandler = clientHandler
        this.courseIdentityRepository = courseIdentityRepository
        this.projectMapperObjectFactory = projectMapperObjectFactory
    }

    private fun getProjectMapper(): ProjectMapper = projectMapperObjectFactory.`object`

    override fun toEntity(request: CourseRequest): Course {
        return courseRepository.findById(request.id ?: UUID(0,0))
            .map {
                Course().apply {
                    this.id = it.id
                    this.name = request.name ?: it.name
                    this.description = request.description ?: it.description
                    this.date = it.date
                }
            }
            .orElseGet {
                Course().apply {
                    this.name = request.name!!
                    this.description = request.description!!
                    this.date = LocalDateTime.now()
                }
            }
    }

    override fun toDependency(entity: Course): CourseDependency {
        return CourseDependency(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            date = entity.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm"))
        )
    }

    override fun toResponse(entity: Course): CourseResponse {
        val identityIds = courseIdentityRepository.findIdentityIdsByCourseId(entity.id)
        val identityDependencies = clientHandler.getIdentityDependenciesByIds(identityIds)
        return CourseResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            date = entity.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm")),
            identities = identityDependencies,
            projects = entity.projects.map { getProjectMapper().toDependency(it) }.toSet()
        )
    }
}