package com.anatomia.app.agent

// Función pura: mismo input → mismo output, sin efectos secundarios.
// Eso permite testearla sin ViewModel ni Compose.
object DecisionEngine {

    fun decide(
        beliefs: List<Belief>,
        desire: Desire,
        questions: List<Question>,
        feedbackPending: Boolean,
    ): AgentAction = when (desire) {
        is Desire.Teach    -> decideTeach(beliefs, questions, feedbackPending)
        is Desire.Reinforce -> decideReinforce(beliefs, questions)
        Desire.Motivate    -> summarize(beliefs, questions)
    }

    private fun decideTeach(
        beliefs: List<Belief>,
        questions: List<Question>,
        feedbackPending: Boolean,
    ): AgentAction {
        if (feedbackPending) {
            val last = beliefs.filterIsInstance<Belief.QuestionAnswered>().lastOrNull()
            val q = last?.let { a -> questions.find { it.id == a.questionId } }
            if (last != null && q != null) {
                return AgentAction.ProvideFeedback(last.wasCorrect, q.explanation)
            }
        }
        val answeredIds = beliefs.filterIsInstance<Belief.QuestionAnswered>()
            .map { it.questionId }.toSet()
        val next = questions.firstOrNull { it.id !in answeredIds }
        return next?.let { AgentAction.PoseQuestion(it) } ?: summarize(beliefs, questions)
    }

    // Reinforce: repregunta solo las que el estudiante falló
    private fun decideReinforce(beliefs: List<Belief>, questions: List<Question>): AgentAction {
        val wrongIds = beliefs
            .filterIsInstance<Belief.QuestionAnswered>()
            .filter { !it.wasCorrect }
            .map { it.questionId }
            .toSet()
        val toRetry = questions.firstOrNull { it.id in wrongIds }
        return toRetry?.let { AgentAction.PoseQuestion(it) } ?: summarize(beliefs, questions)
    }

    private fun summarize(beliefs: List<Belief>, questions: List<Question>): AgentAction {
        val answered = beliefs.filterIsInstance<Belief.QuestionAnswered>()
        return AgentAction.Summarize(
            organId = questions.firstOrNull()?.organId ?: "",
            score   = answered.count { it.wasCorrect },
            total   = questions.size,
        )
    }
}
