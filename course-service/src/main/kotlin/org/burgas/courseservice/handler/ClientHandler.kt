package org.burgas.courseservice.handler

import org.burgas.courseservice.dto.document.DocumentResponse
import org.burgas.courseservice.dto.identity.IdentityDependency
import org.burgas.courseservice.dto.identity.IdentityList
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.client.requiredBody
import java.util.*

@Component
class ClientHandler {

    private final val restClient: RestClient

    constructor(restClient: RestClient) {
        this.restClient = restClient
    }

    fun getIdentityDependenciesByIds(identityIds: List<UUID>): Set<IdentityDependency> {
        return restClient.post()
            .uri("http://localhost:9010/api/v1/identities/dependencies/by-ids")
            .contentType(MediaType.APPLICATION_JSON)
            .body(IdentityList(identityIds = identityIds))
            .retrieve()
            .requiredBody<Set<IdentityDependency>>()
    }

    fun getDocumentById(documentId: UUID): DocumentResponse? {
        return restClient.get()
            .uri("http://localhost:9000/api/v1/documents/by-id?documentId=$documentId")
            .retrieve()
            .body<DocumentResponse>()
    }

    fun handleIdentityCache(identityId: UUID) {
        restClient.put()
            .uri("http://localhost:9010/api/v1/identities/dependency-cache?identityId=$identityId")
            .retrieve()
            .toBodilessEntity()
    }
}