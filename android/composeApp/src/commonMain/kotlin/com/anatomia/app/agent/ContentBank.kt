package com.anatomia.app.agent

import anatomiaapp.composeapp.generated.resources.Res
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Serializable
private data class OrganQuestionsDto(val questions: List<QuestionDto>)

@Serializable
private data class QuestionDto(
    val id: String,
    val topic: String,
    val difficulty: Difficulty,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
) {
    fun toQuestion(organId: String) = Question(
        id           = id,
        organId      = organId,
        stem         = text,
        options      = options,
        correctIndex = correctIndex,
        explanation  = explanation,
        topic        = topic,
        difficulty   = difficulty,
    )
}

object ContentBank {

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadForOrgan(organId: String): List<Question> {
        val filename = "files/questions_$organId.json"
        return try {
            val bytes = Res.readBytes(filename)
            val data = Json.decodeFromString<OrganQuestionsDto>(bytes.decodeToString())
            data.questions.map { it.toQuestion(organId) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun organName(organId: String): String = when (organId) {
        "heart"   -> "el Corazón"
        "lungs"   -> "los Pulmones"
        "kidneys" -> "los Riñones"
        "brain"   -> "el Cerebro"
        "liver"   -> "el Hígado"
        else      -> "este órgano"
    }
}
