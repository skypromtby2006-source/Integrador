package com.anatomia.app.agent

data class StudentProgress(
    val organId          : String,
    val answeredQuestions: Map<String, AnswerRecord> = emptyMap(),
) {
    val totalAnswered  : Int          get() = answeredQuestions.size
    val totalCorrect   : Int          get() = answeredQuestions.values.count { it.wasCorrect }
    val lastUpdatedAt  : Long         get() = answeredQuestions.values.maxOfOrNull { it.answeredAt } ?: 0L
    val incorrectTopics: List<String> get() = answeredQuestions.values
        .filter { !it.wasCorrect }
        .map { it.topic }
        .distinct()
        .take(5)
}

class AgentRepository {

    fun getProgress(organId: String): StudentProgress {
        val all = ProgressStore.loadAll()
        return StudentProgress(
            organId           = organId,
            answeredQuestions = all[organId] ?: emptyMap(),
        )
    }

    fun recordAnswer(organId: String, questionId: String, wasCorrect: Boolean, topic: String) {
        val all   = ProgressStore.loadAll().toMutableMap()
        val organ = all[organId]?.toMutableMap() ?: mutableMapOf()
        organ[questionId] = AnswerRecord(
            wasCorrect = wasCorrect,
            topic      = topic,
            answeredAt = System.currentTimeMillis(),
        )
        all[organId] = organ
        ProgressStore.save(all)
    }

    fun clearProgress(organId: String) {
        val all = ProgressStore.loadAll().toMutableMap()
        all.remove(organId)
        ProgressStore.save(all)
    }
}
