package com.anatomia.app.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// URL base del servidor Teacher App vía ngrok.
// Cuando ngrok genere una URL nueva, cambia solo esta constante.
const val BASE_URL = "https://stable-jailbreak-squire.ngrok-free.dev"

fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.BODY
    }
}
