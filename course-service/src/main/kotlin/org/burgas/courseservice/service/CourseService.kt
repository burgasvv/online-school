package org.burgas.courseservice.service

import org.burgas.courseservice.dao.course.Course
import org.burgas.courseservice.dto.course.CourseDependency
import org.burgas.courseservice.dto.course.CourseRequest
import org.burgas.courseservice.dto.course.CourseResponse
import org.burgas.courseservice.dto.project.ProjectResponse
import org.burgas.courseservice.handler.ClientHandler
import org.burgas.courseservice.mapper.CourseMapper
import org.burgas.courseservice.redis.CacheHandler
import org.burgas.courseservice.redis.RedisKeys
import org.burgas.courseservice.repository.CourseIdentityRepository
import org.burgas.courseservice.service.contract.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
class CourseService : CacheHandler<CourseResponse>, CollectService<CourseResponse>,
    ReadService<UUID, Course, CourseResponse>, CreateService<CourseRequest, CourseResponse>,
    UpdateService<CourseRequest, CourseResponse>, DeleteService<UUID> {

    private val courseMapper: CourseMapper
    private val courseRedisTemplate: RedisTemplate<String, CourseResponse>
    private val projectRedisTemplate: RedisTemplate<String, ProjectResponse>
    private val courseIdentityRepository: CourseIdentityRepository
    private val clientHandler: ClientHandler

    constructor(
        courseMapper: CourseMapper,
        courseRedisTemplate: RedisTemplate<String, CourseResponse>,
        projectRedisTemplate: RedisTemplate<String, ProjectResponse>,
        courseIdentityRepository: CourseIdentityRepository,
        clientHandler: ClientHandler
    ) {
        this.courseMapper = courseMapper
        this.courseRedisTemplate = courseRedisTemplate
        this.projectRedisTemplate = projectRedisTemplate
        this.courseIdentityRepository = courseIdentityRepository
        this.clientHandler = clientHandler
    }

    override fun handleCache(response: CourseResponse) {
        val courseKey = RedisKeys.COURSE_KEY.format(response.id)
        if (courseRedisTemplate.hasKey(courseKey)) courseRedisTemplate.delete(courseKey)

        val projects = response.projects
        if (!projects.isNullOrEmpty()) {
            projects.forEach { projectDependency ->
                val projectKey = RedisKeys.PROJECT_KEY.format(projectDependency.id)
                if (projectRedisTemplate.hasKey(projectKey)) projectRedisTemplate.delete(projectKey)
            }
        }

        handleIdentityDependencyCache(response.id!!)
    }

    fun handleIdentityDependencyCache(courseId: UUID) {
        val courseResponse = findById(courseId)
        val identities = courseResponse.identities
        if (!identities.isNullOrEmpty()) {
            identities.forEach { identityDependency ->
                clientHandler.handleIdentityCache(identityDependency.id!!)
            }
        }
    }

    override fun findAll(): Set<CourseResponse> {
        return courseMapper.courseRepository.findAll().map { courseMapper.toResponse(it) }.toSet()
    }

    fun findCoursesByIdentityId(identityId: UUID): Set<CourseDependency> {
        return courseMapper.courseRepository.findAllById(courseIdentityRepository.findCourseIdsByIdentityId(identityId))
            .map { courseMapper.toDependency(it) }.toSet()
    }

    override fun findEntity(id: UUID): Course {
        return courseMapper.courseRepository.findById(id).orElseThrow { throw IllegalArgumentException("Course not found") }
    }

    override fun findById(id: UUID): CourseResponse {
        val courseKey = RedisKeys.COURSE_KEY.format(id)
        return courseRedisTemplate.opsForValue().get(courseKey) ?: courseMapper.toResponse(findEntity(id))
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun create(request: CourseRequest): CourseResponse {
        val course = courseMapper.courseRepository.save(courseMapper.toEntity(request))
        val courseResponse = courseMapper.toResponse(course)
        handleCache(courseResponse)
        val courseKey = RedisKeys.COURSE_KEY.format(courseResponse.id)
        courseRedisTemplate.opsForValue().set(courseKey, courseResponse)
        return courseResponse
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun update(request: CourseRequest): CourseResponse {
        if (request.id == null) throw IllegalArgumentException("Request course id is null")
        val course = courseMapper.courseRepository.save(courseMapper.toEntity(request))
        val courseResponse = courseMapper.toResponse(course)
        handleCache(courseResponse)
        val courseKey = RedisKeys.COURSE_KEY.format(courseResponse.id)
        courseRedisTemplate.opsForValue().set(courseKey, courseResponse)
        return courseResponse
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun delete(id: UUID) {
        val course = findEntity(id)
        val courseResponse = courseMapper.toResponse(course)
        handleCache(courseResponse)
        courseMapper.courseRepository.delete(course)
    }
}