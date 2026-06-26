package org.burgas.courseservice.router

import org.burgas.courseservice.dto.course.CourseIdentityRequest
import org.burgas.courseservice.dto.course.CourseRequest
import org.burgas.courseservice.dto.exception.ExceptionResponse
import org.burgas.courseservice.service.CourseService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.body
import org.springframework.web.servlet.function.router
import java.util.UUID

@Configuration
class CourseRouter {

    @Bean
    fun courseRouting(courseService: CourseService) = router {
        "/api/v1/courses".nest {
            GET("") {
                ServerResponse.ok().body(courseService.findAll())
            }
            GET("/by-identity") {
                val identityId = UUID.fromString(it.param("identityId").orElseThrow())
                ServerResponse.ok().body(courseService.findCoursesByIdentityId(identityId))
            }
            GET("/by-id") {
                val courseId = UUID.fromString(it.param("courseId").orElseThrow())
                ServerResponse.ok().body(courseService.findById(courseId))
            }
            POST("/create") {
                val courseRequest = it.body<CourseRequest>()
                courseService.create(courseRequest)
                ServerResponse.ok().build()
            }
            PUT("/update") {
                val courseRequest = it.body<CourseRequest>()
                courseService.update(courseRequest)
                ServerResponse.ok().build()
            }
            DELETE("/delete") {
                val courseId = UUID.fromString(it.param("courseId").orElseThrow())
                courseService.delete(courseId)
                ServerResponse.noContent().build()
            }
            PUT("/add-identity") {
                val courseIdentityRequest = it.body<CourseIdentityRequest>()
                val cookie = it.cookies().getFirst("AUTH_TOKEN")
                courseService.addIdentity(courseIdentityRequest, cookie!!)
                ServerResponse.ok().build()
            }
            PUT("/remove-identity") {
                val courseIdentityRequest = it.body<CourseIdentityRequest>()
                val cookie = it.cookies().getFirst("AUTH_TOKEN")
                courseService.removeIdentity(courseIdentityRequest, cookie!!)
                ServerResponse.ok().build()
            }
            onError<Throwable> { throwable, _ ->
                val exceptionResponse = ExceptionResponse(
                    status = HttpStatus.BAD_REQUEST.name,
                    code = HttpStatus.BAD_REQUEST.value(),
                    message = throwable.message
                )
                ServerResponse.status(HttpStatus.BAD_REQUEST).body(exceptionResponse)
            }
        }
    }
}