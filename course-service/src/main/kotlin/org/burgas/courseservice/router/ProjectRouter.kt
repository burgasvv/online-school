package org.burgas.courseservice.router

import org.burgas.courseservice.dto.project.ProjectRequest
import org.burgas.courseservice.service.ProjectService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.body
import org.springframework.web.servlet.function.router
import java.util.UUID

@Configuration
class ProjectRouter {

    @Bean
    fun projectRoutes(projectService: ProjectService) = router {
        "/api/v1/projects".nest {
            GET("/by-id") {
                val projectId = UUID.fromString(it.param("projectId").orElseThrow())
                ServerResponse.ok().body(projectService.findById(projectId))
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
            POST("/upload-image") {
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
        }
    }
}