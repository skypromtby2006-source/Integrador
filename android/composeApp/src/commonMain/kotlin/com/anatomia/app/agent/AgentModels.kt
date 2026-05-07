package com.anatomia.app.agent

// ── Beliefs: lo que el agente sabe del estado del estudiante ─────────────────
sealed class Belief {
    data class OrganFocused(val organId: String) : Belief()
    data class QuestionAnswered(val questionId: String, val wasCorrect: Boolean) : Belief()
    data class TopicCompleted(val organId: String) : Belief()
}

// ── Desires: lo que el agente quiere lograr ───────────────────────────────────
sealed class Desire {
    data class Teach(val organId: String) : Desire()
    data class Reinforce(val organId: String) : Desire()
    data object Motivate : Desire()
}

// ── Actions: lo que el agente puede hacer ────────────────────────────────────
sealed class AgentAction {
    data object Greet : AgentAction()
    data class PoseQuestion(val question: Question) : AgentAction()
    data class ProvideFeedback(val wasCorrect: Boolean, val explanation: String) : AgentAction()
    data class Summarize(val organId: String, val score: Int, val total: Int) : AgentAction()
}

data class Question(
    val id: String,
    val organId: String,
    val stem: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)
