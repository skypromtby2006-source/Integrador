package com.anatomia.app.agent

// organId → (questionId → wasCorrect)
expect object ProgressStore {
    fun init(context: Any?)
    fun loadAll(): Map<String, Map<String, Boolean>>
    fun save(data: Map<String, Map<String, Boolean>>)
}
