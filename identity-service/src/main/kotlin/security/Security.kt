package org.burgas.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.burgas.dao.IdentityEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.database.IdentityTable
import org.burgas.dto.AuthToken
import org.burgas.dto.CsrfToken
import org.burgas.dto.ExceptionResponse
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt

fun Application.configureSecurity() {

    authentication {
        basic(name = "basic-auth-all") {
            validate { credentials ->
                val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity.find { IdentityTable.email eq credentials.name }.singleOrNull()
                }
                if (
                    identityEntity != null && identityEntity.status &&
                    BCrypt.checkpw(credentials.password, identityEntity.password)
                ) {
                    UserPasswordCredential(credentials.name, credentials.password)
                } else {
                    null
                }
            }
        }
        basic(name = "basic-auth-admin") {
            validate { credentials ->
                val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity.find { IdentityTable.email eq credentials.name }.singleOrNull()
                }
                if (
                    identityEntity != null && identityEntity.status &&
                    identityEntity.authority == Authority.ADMIN &&
                    BCrypt.checkpw(credentials.password, identityEntity.password)
                ) {
                    UserPasswordCredential(credentials.name, credentials.password)
                } else {
                    null
                }
            }
        }
        basic(name = "basic-auth-employee") {
            validate { credentials ->
                val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity.find { IdentityTable.email eq credentials.name }.singleOrNull()
                }
                if (
                    identityEntity != null && identityEntity.status &&
                    identityEntity.authority == Authority.ADMIN && identityEntity.authority == Authority.TEACHER &&
                    BCrypt.checkpw(credentials.password, identityEntity.password)
                ) {
                    UserPasswordCredential(credentials.name, credentials.password)
                } else {
                    null
                }
            }
        }
        session<AuthToken>(name = "basic-auth-session") {
            validate { session -> session }
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
                message = cause.localizedMessage
            )
            call.respond(HttpStatusCode.BadRequest, exceptionResponse)
        }
    }

    install(Sessions) {
        cookie<CsrfToken>("CSRF_TOKEN")
        cookie<AuthToken>("AUTH_TOKEN")
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
        allowOrigin("http://localhost:8000")
        allowOrigin("http://localhost:8010")
        checkHeader("X-CSRF-Token")
    }
}