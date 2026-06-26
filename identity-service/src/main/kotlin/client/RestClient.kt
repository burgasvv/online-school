package org.burgas.client

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object RestClient {

    val httpClient = HttpClient(CIO) {
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
}