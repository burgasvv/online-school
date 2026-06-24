package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.database.IdentityTable
import org.burgas.dto.AuthToken
import org.burgas.dto.CsrfToken
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.UUID

fun Application.configureSecurityRouter() {

    routing {

        route("/api/v1/security") {

            get("/csrf-token") {
                val csrfToken = call.sessions.get(CsrfToken::class)
                if (csrfToken != null) {
                    call.respond(HttpStatusCode.OK, csrfToken)
                } else {
                    val csrfToken = CsrfToken(token = UUID.randomUUID())
                    call.sessions.set(csrfToken, CsrfToken::class)
                    call.respond(HttpStatusCode.OK, csrfToken)
                }
            }

            authenticate("basic-auth-all") {

                get("/login") {
                    val authToken = call.sessions.get(AuthToken::class)
                    if (authToken != null) {
                        call.respond(HttpStatusCode.OK, "You are already logged in")
                    } else {
                        val principal = call.principal<UserPasswordCredential>()!!
                        val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                            IdentityEntity.find { IdentityTable.email eq principal.name }.single()
                        }
                        call.sessions.set(AuthToken(identityEntity.email, identityEntity.authority), AuthToken::class)
                        call.respond(HttpStatusCode.OK, "You successfully logged in")
                    }
                }
            }

            authenticate("basic-auth-session") {

                get("/logout") {
                    call.sessions.clear(AuthToken::class)
                    call.respond(HttpStatusCode.OK, "You successfully logged out")
                }
            }
        }
    }
}