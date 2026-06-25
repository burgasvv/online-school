package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AuthToken
import org.burgas.dto.IdentityList
import org.burgas.dto.IdentityRequest
import org.burgas.service.IdentityService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

fun Application.configureIdentityRouter() {

    val identityService = IdentityService()
    val paramUrls: List<String> = listOf(
        "/api/v1/identities/by-id", "/api/v1/identities/delete", "/api/v1/identities/upload-image",
        "/api/v1/identities/remove-image", "/api/v1/identities/upload-document", "/api/v1/identities/remove-document"
    )
    val bodyUrls: List<String> = listOf("/api/v1/identities/update", "/api/v1/identities/change-password")

    intercept(ApplicationCallPipeline.Plugins) {

        if (paramUrls.contains(call.request.path())) {
            val authToken = call.sessions.get(AuthToken::class)!!
            val identityId = UUID.fromString(call.parameters["identityId"])

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(identityId)!!
            }
            if (identityEntity.email == authToken.token) {
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized")
            }

        } else if (bodyUrls.contains(call.request.path())) {
            val authToken = call.sessions.get(AuthToken::class)!!
            val identityRequest = call.receive(IdentityRequest::class)

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(identityRequest.id!!)!!
            }
            if (identityEntity.email == authToken.token) {
                call.attributes[AttributeKey<IdentityRequest>("identityRequest")] = identityRequest
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized")
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/identities") {

            post("/create") {
                val identityRequest = call.receive(IdentityRequest::class)
                identityService.create(identityRequest)
                call.respond(HttpStatusCode.OK)
            }

            post("/dependencies/by-ids") {
                val identityList = call.receive(IdentityList::class)
                call.respond(HttpStatusCode.OK, identityService.findDependenciesByIds(identityList.identityIds))
            }

            authenticate("basic-auth-admin") {

                get {
                    call.respond(HttpStatusCode.OK, identityService.findAll())
                }

                put("/change-status") {
                    val identityRequest = call.receive(IdentityRequest::class)
                    identityService.changeStatus(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("basic-auth-session") {

                get("/by-id") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    call.respond(HttpStatusCode.OK, identityService.findById(identityId))
                }

                get("/no-cache/by-id") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    call.respond(HttpStatusCode.OK, identityService.findByIdNoCache(identityId))
                }

                put("/update") {
                    val identityRequest = call.attributes[AttributeKey<IdentityRequest>("identityRequest")]
                    identityService.update(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/delete") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.delete(identityId)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-image") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.uploadImage(identityId, call.receiveMultipart(Long.MAX_VALUE))
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-image") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.removeImage(identityId)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-document") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.uploadDocument(identityId, call.receiveMultipart(Long.MAX_VALUE))
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-document") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    val documentId = UUID.fromString(call.parameters["documentId"])
                    identityService.removeDocument(identityId, documentId)
                    call.respond(HttpStatusCode.OK)
                }

                put("/change-password") {
                    val identityRequest = call.attributes[AttributeKey<IdentityRequest>("identityRequest")]
                    identityService.changePassword(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}