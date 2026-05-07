package com.anatomia.app.agent

// Sustituir por SQLDelight cuando se agregue persistencia entre sesiones.
data class StudentProgress(
    val organId: String,
    val answeredQuestions: Map<String, Boolean> = emptyMap(), // questionId → wasCorrect
) {
    val totalAnswered: Int get() = answeredQuestions.size
    val totalCorrect: Int  get() = answeredQuestions.values.count { it }
}

class AgentRepository {

    private val store = mutableMapOf<String, StudentProgress>()

    fun getProgress(organId: String): StudentProgress =
        store.getOrDefault(organId, StudentProgress(organId))

    fun recordAnswer(organId: String, questionId: String, wasCorrect: Boolean) {
        val current = getProgress(organId)
        store[organId] = current.copy(
            answeredQuestions = current.answeredQuestions + (questionId to wasCorrect),
        )
    }

    fun clearProgress(organId: String) {
        store.remove(organId)
    }
}
