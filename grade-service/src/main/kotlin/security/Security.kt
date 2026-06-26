package org.burgas.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic
import io.ktor.server.auth.form
import io.ktor.server.auth.session
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import org.burgas.dto.AuthToken
import org.burgas.dto.ExceptionResponse

fun Application.configureSecurity() {

    authentication {
        session<AuthToken>("basic-auth-session") {
            validate { it }
            challenge {
                val exceptionResponse = ExceptionResponse(
                    status = HttpStatusCode.Unauthorized.description,
                    code = HttpStatusCode.Unauthorized.value,
                    message = "Not authenticated"
                )
                call.respond(HttpStatusCode.Unauthorized, exceptionResponse)
            }
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val exceptionResponse = ExceptionResponse(
                status = HttpStatusCode.BadRequest.description,
                code = HttpStatusCode.BadRequest.value,
                message = cause.message
            )
            call.respond(HttpStatusCode.BadRequest, exceptionResponse)
        }
    }

    install(Sessions) {
        cookie<AuthToken>("AUTH_TOKEN")
    }
}
