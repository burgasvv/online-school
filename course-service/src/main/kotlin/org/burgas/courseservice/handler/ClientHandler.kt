package org.burgas.courseservice.handler

import jakarta.servlet.http.Cookie
import org.burgas.courseservice.dto.document.DocumentResponse
import org.burgas.courseservice.dto.identity.IdentityDependency
import org.burgas.courseservice.dto.identity.IdentityList
import org.burgas.courseservice.dto.identity.IdentityResponse
import org.burgas.courseservice.dto.token.CsrfToken
import org.springframework.http.HttpHeaders
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

    fun getIdentityServiceCsrfToken(): CsrfToken {
        return restClient.get()
            .uri("http://localhost:9010/api/v1/security/csrf-token")
            .retrieve()
            .requiredBody<CsrfToken>()
    }

    fun getIdentityDependenciesByIds(identityIds: List<UUID>): Set<IdentityDependency> {
        return restClient.post()
            .uri("http://localhost:9010/api/v1/identities/dependencies/by-ids")
            .header(HttpHeaders.ORIGIN, "http://localhost:8000")
            .header("X-CSRF-Token", getIdentityServiceCsrfToken().token.toString())
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

    fun getIdentityResponseById(identityId: UUID, cookie: Cookie): IdentityResponse {
        return restClient.get()
            .uri("http://localhost:9010/api/v1/identities/no-cache/by-id?identityId=$identityId")
            .cookie("AUTH_TOKEN", cookie.value)
            .retrieve()
            .requiredBody<IdentityResponse>()
    }
}