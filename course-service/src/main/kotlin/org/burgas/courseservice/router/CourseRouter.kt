package org.burgas.courseservice.router

import org.burgas.courseservice.dto.course.CourseRequest
import org.burgas.courseservice.service.CourseService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
            PUT("/dependency-cache") {
                val courseId = UUID.fromString(it.param("courseId").orElseThrow())
                courseService.handleIdentityDependencyCache(courseId)
                ServerResponse.ok().build()
            }
        }
    }
}