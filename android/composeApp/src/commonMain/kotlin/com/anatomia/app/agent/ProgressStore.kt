package com.anatomia.app.agent

// organId → (questionId → AnswerRecord)
expect object ProgressStore {
    fun init(context: Any?)
    fun loadAll(): Map<String, Map<String, AnswerRecord>>
    fun save(data: Map<String, Map<String, AnswerRecord>>)
}
