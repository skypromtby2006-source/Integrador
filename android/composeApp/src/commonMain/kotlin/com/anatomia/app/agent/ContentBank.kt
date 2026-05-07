package com.anatomia.app.agent

import anatomiaapp.composeapp.generated.resources.Res
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Serializable
private data class QuestionsData(val organs: List<OrganQuestionsDto>)

@Serializable
private data class OrganQuestionsDto(val organId: String, val questions: List<QuestionDto>)

@Serializable
private data class QuestionDto(
    val id: String,
    val topic: String,
    val difficulty: Difficulty,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

object ContentBank {

    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(): Map<String, List<Question>> {
        val bytes = Res.readBytes("files/questions.json")
        val data = Json.decodeFromString<QuestionsData>(bytes.decodeToString())
        return data.organs.associate { organ ->
            organ.organId to organ.questions.map { q ->
                Question(
                    id          = q.id,
                    organId     = organ.organId,
                    stem        = q.text,
                    options     = q.options,
                    correctIndex = q.correctIndex,
                    explanation = q.explanation,
                    topic       = q.topic,
                    difficulty  = q.difficulty,
                )
            }
        }
    }

    fun organName(organId: String): String = when (organId) {
        "heart"   -> "el Corazón"
        "lungs"   -> "los Pulmones"
        "kidneys" -> "los Riñones"
        else      -> organId
    }
}
