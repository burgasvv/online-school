package org.burgas.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.burgas.dto.IdentityDependency
import org.burgas.dto.ProjectDependency
import java.util.*

object RestClient {

    private val httpClient = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(
                Json {
                    explicitNulls = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                    allowComments = true
                }
            )
        }
        install(HttpCookies)
    }

    suspend fun findStudentDependency(studentId: UUID): IdentityDependency {
        return httpClient.get("http://localhost:9010/api/v1/identities/dependency/by-id") {
            parameter("identityId", studentId)
        }.body<IdentityDependency>()
    }

    suspend fun findTeacherDependency(teacherId: UUID): IdentityDependency {
        return httpClient.get("http://localhost:9010/api/v1/identities/dependency/by-id"){
            parameter("identityId", teacherId)
        }.body<IdentityDependency>()
    }

    suspend fun findProjectDependency(projectId: UUID): ProjectDependency {
        return httpClient.get("http://localhost:9020/api/v1/projects/dependency/by-id"){
            parameter("projectId", projectId)
        }.body<ProjectDependency>()
    }
}