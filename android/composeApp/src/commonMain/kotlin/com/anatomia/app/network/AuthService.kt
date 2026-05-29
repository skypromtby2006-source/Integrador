package com.anatomia.app.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthService(private val client: HttpClient) {

    // Llama al endpoint POST /login del servidor Teacher App.
    // Devuelve Result<LoginResponseData> para manejar éxito y error
    // sin usar excepciones como flujo de control.
    suspend fun login(email: String, password: String): Result<LoginResponseData> {
        return try {
            val response: ApiResponse = client.post("$BASE_URL/auth/estudiante") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email = email, password = password))
            }.body()

            if (response.ok && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor: ${e.message}"))
        }
    }
}
