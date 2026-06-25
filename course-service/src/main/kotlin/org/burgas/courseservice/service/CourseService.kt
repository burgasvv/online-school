package org.burgas.courseservice.service

import jakarta.servlet.http.Cookie
import org.burgas.courseservice.dao.course.Course
import org.burgas.courseservice.dao.course.CourseIdentity
import org.burgas.courseservice.dao.course.CourseIdentityId
import org.burgas.courseservice.dto.course.CourseDependency
import org.burgas.courseservice.dto.course.CourseIdentityRequest
import org.burgas.courseservice.dto.course.CourseRequest
import org.burgas.courseservice.dto.course.CourseResponse
import org.burgas.courseservice.handler.ClientHandler
import org.burgas.courseservice.mapper.CourseMapper
import org.burgas.courseservice.repository.CourseIdentityRepository
import org.burgas.courseservice.service.contract.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
class CourseService : CollectService<CourseResponse>, ReadService<UUID, Course, CourseResponse>,
    CreateService<CourseRequest, CourseResponse>, UpdateService<CourseRequest, CourseResponse>, DeleteService<UUID> {

    private val courseMapper: CourseMapper
    private val courseIdentityRepository: CourseIdentityRepository
    private val clientHandler: ClientHandler

    constructor(
        courseMapper: CourseMapper,
        courseIdentityRepository: CourseIdentityRepository,
        clientHandler: ClientHandler
    ) {
        this.courseMapper = courseMapper
        this.courseIdentityRepository = courseIdentityRepository
        this.clientHandler = clientHandler
    }

    override fun findAll(): Set<CourseResponse> {
        return courseMapper.courseRepository.findAll().map { courseMapper.toResponse(it) }.toSet()
    }

    fun findCoursesByIdentityId(identityId: UUID): Set<CourseDependency> {
        return courseMapper.courseRepository.findAllById(courseIdentityRepository.findCourseIdsByIdentityId(identityId))
            .map { courseMapper.toDependency(it) }.toSet()
    }

    override fun findEntity(id: UUID): Course {
        return courseMapper.courseRepository.findById(id)
            .orElseThrow { throw IllegalArgumentException("Course not found") }
    }

    override fun findById(id: UUID): CourseResponse {
        return courseMapper.toResponse(findEntity(id))
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun create(request: CourseRequest): CourseResponse {
        val course = courseMapper.courseRepository.save(courseMapper.toEntity(request))
        val courseResponse = courseMapper.toResponse(course)
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
        return courseResponse
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    override fun delete(id: UUID) {
        val course = findEntity(id)
        courseMapper.courseRepository.delete(course)
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    fun addIdentity(courseIdentityRequest: CourseIdentityRequest, cookie: Cookie) {
        val course = findEntity(courseIdentityRequest.courseId)
        val identityResponse = clientHandler.getIdentityResponseById(courseIdentityRequest.identityId, cookie)
        val courseIdentityId = CourseIdentityId(courseId = course.id, identityId = identityResponse.id!!)
        var courseIdentity = courseIdentityRepository.findById(courseIdentityId).orElse(null)
        if (courseIdentity != null)
            throw IllegalArgumentException("Identity already in course")
        else {
            courseIdentity = CourseIdentity(courseIdentityId)
            courseIdentityRepository.save(courseIdentity)
        }
    }

    @Transactional(
        isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
        rollbackFor = [Throwable::class, Exception::class]
    )
    fun removeIdentity(courseIdentityRequest: CourseIdentityRequest, cookie: Cookie) {
        val course = findEntity(courseIdentityRequest.courseId)
        val identityResponse = clientHandler.getIdentityResponseById(courseIdentityRequest.identityId, cookie)
        val courseIdentityId = CourseIdentityId(courseId = course.id, identityId = identityResponse.id!!)
        var courseIdentity = courseIdentityRepository.findById(courseIdentityId).orElse(null)
        if (courseIdentity != null) {
            courseIdentity = CourseIdentity(courseIdentityId)
            courseIdentityRepository.delete(courseIdentity)
        } else {
            throw IllegalArgumentException("Identity not in course for remove")
        }
    }
}