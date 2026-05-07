package com.anatomia.app.agent

data class AnswerResult(
    val isCorrect: Boolean,
    val newScore: Float,
    val updatedIncorrectTopics: List<String>,
    val explanation: String,
)

// Función pura: mismo input → mismo output, sin efectos secundarios.
// Eso permite testearla sin ViewModel ni Compose.
object DecisionEngine {

    fun decide(
        beliefs: List<Belief>,
        desire: Desire,
        questions: List<Question>,
        feedbackPending: Boolean,
        score: Float = 0f,
        incorrectTopics: List<String> = emptyList(),
        askedQuestionIds: Set<String> = emptySet(),
    ): AgentAction = when (desire) {
        is Desire.Teach     -> decideTeach(beliefs, questions, feedbackPending, score, incorrectTopics, askedQuestionIds)
        is Desire.Reinforce -> decideReinforce(beliefs, questions, score, incorrectTopics)
        Desire.Motivate     -> summarize(beliefs, questions)
    }

    fun evaluateAnswer(
        question: Question,
        selectedIndex: Int,
        currentScore: Float,
        incorrectTopics: List<String>,
    ): AnswerResult {
        val isCorrect = selectedIndex == question.correctIndex
        val delta = when {
            isCorrect  && question.difficulty == Difficulty.HARD   ->  0.20f
            isCorrect  && question.difficulty == Difficulty.MEDIUM ->  0.15f
            isCorrect  && question.difficulty == Difficulty.EASY   ->  0.10f
            !isCorrect && question.difficulty == Difficulty.HARD   -> -0.05f
            !isCorrect && question.difficulty == Difficulty.MEDIUM -> -0.08f
            else                                                   -> -0.10f
        }
        val newScore = (currentScore + delta).coerceIn(0f, 1f)
        val updatedTopics = if (!isCorrect)
            (incorrectTopics + question.topic).distinct().takeLast(5)
        else incorrectTopics
        return AnswerResult(
            isCorrect = isCorrect,
            newScore = newScore,
            updatedIncorrectTopics = updatedTopics,
            explanation = question.explanation,
        )
    }

    fun decideNextDesire(
        score: Float,
        attemptCount: Int,
        organId: String,
        totalQuestions: Int = Int.MAX_VALUE,
        answeredCount: Int = 0,
    ): Desire {
        if (answeredCount < minOf(totalQuestions, 3)) return Desire.Teach(organId)
        return when {
            attemptCount == 0 -> Desire.Teach(organId)
            score < 0.35f     -> Desire.Reinforce(organId)
            score < 0.70f     -> Desire.Teach(organId)
            else              -> Desire.Motivate
        }
    }

    private fun decideTeach(
        beliefs: List<Belief>,
        questions: List<Question>,
        feedbackPending: Boolean,
        score: Float,
        incorrectTopics: List<String>,
        askedQuestionIds: Set<String>,
    ): AgentAction {
        if (feedbackPending) {
            val last = beliefs.filterIsInstance<Belief.QuestionAnswered>().lastOrNull()
            val q = last?.let { a -> questions.find { it.id == a.questionId } }
            if (last != null && q != null) {
                return AgentAction.ProvideFeedback(last.wasCorrect, q.explanation)
            }
        }
        val next = selectNextQuestion(questions, askedQuestionIds.toList(), incorrectTopics, score)
        return next?.let { AgentAction.PoseQuestion(it, contextMessage(score)) }
            ?: summarize(beliefs, questions)
    }

    // Reinforce: repregunta las falladas priorizando por dificultad según score
    private fun decideReinforce(
        beliefs: List<Belief>,
        questions: List<Question>,
        score: Float,
        incorrectTopics: List<String>,
    ): AgentAction {
        val answeredBeliefs = beliefs.filterIsInstance<Belief.QuestionAnswered>()
        val wrongIds = answeredBeliefs
            .filter { !it.wasCorrect }
            .map { it.questionId }
            .toSet()
        val wrongQuestions = questions.filter { it.id in wrongIds }
        // Exclude questions already answered more than once (already re-asked in reinforce)
        val alreadyReasked = answeredBeliefs
            .groupBy { it.questionId }
            .filter { (_, entries) -> entries.size > 1 }
            .keys
            .toList()
        val next = selectNextQuestion(wrongQuestions, alreadyReasked, incorrectTopics, score)
        return next?.let { AgentAction.PoseQuestion(it, contextMessage(score)) }
            ?: summarize(beliefs, questions)
    }

    private fun selectNextQuestion(
        questions: List<Question>,
        askedIds: List<String>,
        incorrectTopics: List<String>,
        score: Float,
    ): Question? {
        val unanswered = questions.filter { it.id !in askedIds }
        if (unanswered.isEmpty()) return null

        return when {
            // Score bajo → prioriza EASY para afianzar bases
            score < 0.35f -> {
                unanswered.firstOrNull { it.difficulty == Difficulty.EASY }
                    ?: unanswered.firstOrNull { it.difficulty == Difficulty.MEDIUM }
                    ?: unanswered.first()
            }
            // Score medio → prioriza temas donde falló, luego MEDIUM
            score < 0.70f -> {
                val onFailedTopic = unanswered.filter { it.topic in incorrectTopics }
                onFailedTopic.firstOrNull { it.difficulty == Difficulty.EASY }
                    ?: onFailedTopic.firstOrNull { it.difficulty == Difficulty.MEDIUM }
                    ?: unanswered.firstOrNull { it.difficulty == Difficulty.MEDIUM }
                    ?: unanswered.first()
            }
            // Score alto → prioriza HARD para desafiar
            else -> {
                unanswered.firstOrNull { it.difficulty == Difficulty.HARD }
                    ?: unanswered.firstOrNull { it.difficulty == Difficulty.MEDIUM }
                    ?: unanswered.first()
            }
        }
    }

    private fun contextMessage(score: Float): String = when {
        score < 0.35f -> "Vamos con algo más sencillo para afianzar las bases."
        score < 0.70f -> "Sigamos practicando."
        else          -> "Buen nivel. Aquí va un desafío más difícil."
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
