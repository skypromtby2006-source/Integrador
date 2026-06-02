package com.anatomia.app.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class AnswerRecordDto(
    val questionId: String,
    val wasCorrect: Boolean
)

@Serializable
data class SubmitProgressDto(
    val studentId: String,
    val organId:   String,
    val answers:   List<AnswerRecordDto>,
    val timestamp: Long = System.currentTimeMillis()
)

class ProgressService(private val client: HttpClient) {

    suspend fun submitProgress(
        studentId: String,
        organId:   String,
        answers:   List<AnswerRecordDto>
    ): Boolean {
        return try {
            val response: HttpResponse = client.post("$BASE_URL/progreso/sesion") {
                contentType(ContentType.Application.Json)
                header(NGROK_HEADER, "true")
                setBody(SubmitProgressDto(
                    studentId = studentId,
                    organId   = organId,
                    answers   = answers
                ))
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("[PROGRESS] Falló envío al servidor: ${e.message}")
            false
        }
    }
}
