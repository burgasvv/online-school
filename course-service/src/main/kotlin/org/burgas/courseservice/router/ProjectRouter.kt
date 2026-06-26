package org.burgas.courseservice.router

import org.burgas.courseservice.dto.exception.ExceptionResponse
import org.burgas.courseservice.dto.project.ProjectRequest
import org.burgas.courseservice.service.ProjectService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.body
import org.springframework.web.servlet.function.router
import java.util.*

@Configuration
class ProjectRouter {

    @Bean
    fun projectRoutes(projectService: ProjectService) = router {
        "/api/v1/projects".nest {
            GET("/by-id") {
                val projectId = UUID.fromString(it.param("projectId").orElseThrow())
                ServerResponse.ok().body(projectService.findById(projectId))
            }
            GET("/dependency/by-id") {
                val projectId = UUID.fromString(it.param("projectId").orElseThrow())
                ServerResponse.ok().body(projectService.findDependencyById(projectId))
            }
            POST("/create") {
                val projectRequest = it.body<ProjectRequest>()
                projectService.create(projectRequest)
                ServerResponse.ok().build()
            }
            PUT("/update") {
                val projectRequest = it.body<ProjectRequest>()
                projectService.update(projectRequest)
                ServerResponse.ok().build()
            }
            DELETE("/delete") {
                val projectId = UUID.fromString(it.param("projectId").orElseThrow())
                projectService.delete(projectId)
                ServerResponse.ok().build()
            }
            POST("/upload-document") {
                val projectId = UUID.fromString(it.param("projectId").orElseThrow())
                val part = it.multipartData().getFirst("document")!!
                projectService.uploadDocument(projectId, part)
                ServerResponse.ok().build()
            }
            DELETE("/remove-document") {
                val projectId = UUID.fromString(it.param("projectId").orElseThrow())
                projectService.removeDocument(projectId)
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