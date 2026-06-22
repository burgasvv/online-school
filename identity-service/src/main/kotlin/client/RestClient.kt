package org.burgas.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
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