package com.anatomia.app.db

import com.anatomia.app.agent.Difficulty
import com.anatomia.app.agent.Question
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Almacena preguntas descargadas del servidor Teacher App.
// Question.id es String en el dominio ("heart_q1" local o "42" del servidor).
// En SQLite se guarda como INTEGER; al leer se convierte a String con toString().
object QuestionRepository {

    private val queries get() = DatabaseProvider.database.questionsQueries
    private val json = Json { ignoreUnknownKeys = true }

    fun upsert(question: Question) {
        queries.upsertQuestion(
            id            = question.id.toLongOrNull() ?: 0L,
            topic         = question.topic,
            organ_id      = question.organId,
            stem          = question.stem,
            explanation   = question.explanation,
            options       = json.encodeToString(question.options),
            correct_index = question.correctIndex.toLong(),
            difficulty    = question.difficulty.ordinal.toLong()
        )
    }

    fun getByOrgan(organId: String): List<Question> =
        queries.getByOrgan(organId).executeAsList().map { it.toQuestion() }

    fun getAll(): List<Question> =
        queries.getAllQuestions().executeAsList().map { it.toQuestion() }

    fun clear() = queries.clearQuestions()

    private fun Questions.toQuestion() = Question(
        id           = id.toString(),
        organId      = organ_id,
        stem         = stem,
        explanation  = explanation,
        options      = json.decodeFromString(options),
        correctIndex = correct_index.toInt(),
        topic        = topic,
        difficulty   = Difficulty.entries.getOrElse(difficulty.toInt() - 1) { Difficulty.EASY }
    )
}
