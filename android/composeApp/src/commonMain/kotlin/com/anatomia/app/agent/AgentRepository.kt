package com.anatomia.app.agent

data class StudentProgress(
    val organId          : String,
    val answeredQuestions: Map<String, Boolean> = emptyMap(), // questionId → wasCorrect
) {
    val totalAnswered: Int get() = answeredQuestions.size
    val totalCorrect : Int get() = answeredQuestions.values.count { it }
}

class AgentRepository {

    fun getProgress(organId: String): StudentProgress {
        val answers = ProgressStore.loadAll()[organId] ?: emptyMap()
        return StudentProgress(organId = organId, answeredQuestions = answers)
    }

    fun recordAnswer(organId: String, questionId: String, wasCorrect: Boolean) {
        val all     = ProgressStore.loadAll().toMutableMap()
        val current = all.getOrDefault(organId, emptyMap())
        all[organId] = current + (questionId to wasCorrect)
        ProgressStore.save(all)
    }

    fun clearProgress(organId: String) {
        val all = ProgressStore.loadAll().toMutableMap()
        all.remove(organId)
        ProgressStore.save(all)
    }
}
