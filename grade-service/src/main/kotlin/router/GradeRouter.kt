package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.burgas.dto.GradeRequest
import org.burgas.service.GradeService
import java.util.UUID

fun Application.configureGradeRouter() {

    val gradeService = GradeService()

    routing {

        route("/api/v1/grades") {

            authenticate("basic-auth-session") {

                get("/by-id") {
                    val gradeId = UUID.fromString(call.parameters["gradeId"])
                    call.respond(HttpStatusCode.OK, gradeService.findById(gradeId))
                }

                post("/create") {
                    val gradeRequest = call.receive(GradeRequest::class)
                    gradeService.create(gradeRequest)
                    call.respond(HttpStatusCode.OK)
                }

                put("/update") {
                    val gradeRequest = call.receive(GradeRequest::class)
                    gradeService.update(gradeRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val gradeId = UUID.fromString(call.parameters["gradeId"])
                    gradeService.delete(gradeId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}