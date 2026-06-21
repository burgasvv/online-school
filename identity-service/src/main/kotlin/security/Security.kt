package org.burgas.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.burgas.dto.CsrfToken
import org.burgas.dto.ExceptionResponse

fun Application.configureSecurity() {

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val exceptionResponse = ExceptionResponse(
                status = HttpStatusCode.BadRequest.description,
                code = HttpStatusCode.BadRequest.value,
                message = cause.localizedMessage
            )
            call.respond(HttpStatusCode.BadRequest, exceptionResponse)
        }
    }

    install(Sessions) {
        cookie<CsrfToken>("CSRF_TOKEN")
    }


    install(CORS) {
        anyMethod()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-CSRF-Token")
        anyHost()

        allowCredentials = true
        allowNonSimpleContentTypes = true
    }


    install(CSRF) {
        allowOrigin("http://localhost:8010")
        allowOrigin("https://localhost:9010")
        allowOrigin("https://localhost:10010")
        checkHeader("X-CSRF-Token")
    }
}