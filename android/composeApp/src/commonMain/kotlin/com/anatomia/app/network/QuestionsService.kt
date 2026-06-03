package com.anatomia.app.network

import com.anatomia.app.agent.Difficulty
import com.anatomia.app.agent.Question
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

// DTO que mapea exactamente la respuesta JSON del servidor Teacher App.
// Teacher usa "body" para el texto — lo mapeamos a "stem" del dominio.
@Serializable
data class QuestionDto(
    val id:           Int,
    val topic:        String,
    val organId:      String,
    val body:         String,
    val explanation:  String = "",
    val options:      List<String>,
    val correctIndex: Int,
    val difficulty:   Int = 1       // Teacher: 1=Fácil, 2=Medio, 3=Difícil (1-based)
)

@Serializable
data class QuestionsApiResponse(
    val ok:    Boolean,
    val data:  List<QuestionDto>? = null,
    val error: String?            = null
)

class QuestionsService(private val client: HttpClient) {

    suspend fun fetchByOrgan(organId: String): Result<List<Question>> {
        return try {
            val response: QuestionsApiResponse = client
                .get("$BASE_URL/questions/by-organ/$organId") {
                    header(NGROK_HEADER, "true")
                }.body()

            if (response.ok && response.data != null) {
                Result.success(response.data.map { it.toQuestion() })
            } else {
                Result.failure(Exception(response.error ?: "Error del servidor"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión: ${e.message}"))
        }
    }

    suspend fun fetchAll(): Result<List<Question>> {
        return try {
            val response: QuestionsApiResponse = client
                .get("$BASE_URL/questions") {
                    header(NGROK_HEADER, "true")
                }.body()

            if (response.ok && response.data != null) {
                Result.success(response.data.map { it.toQuestion() })
            } else {
                Result.failure(Exception(response.error ?: "Error del servidor"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión: ${e.message}"))
        }
    }

    suspend fun countByOrgan(organId: String): Result<Int> {
        return try {
            val response: QuestionsApiResponse = client
                .get("$BASE_URL/questions/by-organ/$organId") {
                    header(NGROK_HEADER, "true")
                }.body()
            if (response.ok)
                Result.success(response.data?.size ?: 0)
            else
                Result.failure(Exception(response.error ?: "Error del servidor"))
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión: ${e.message}"))
        }
    }

    private fun QuestionDto.toQuestion() = Question(
        id           = id.toString(),
        organId      = organId,
        stem         = body,
        explanation  = explanation,
        options      = options,
        correctIndex = correctIndex,
        topic        = topic,
        difficulty   = Difficulty.entries.getOrElse(difficulty - 1) { Difficulty.EASY }
    )
}
