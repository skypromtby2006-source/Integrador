package com.anatomia.app.ui.screen.agent

import androidx.lifecycle.ViewModel
import com.anatomia.app.agent.AgentAction
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.agent.Belief
import com.anatomia.app.agent.ContentBank
import com.anatomia.app.agent.DecisionEngine
import com.anatomia.app.agent.Desire
import com.anatomia.app.agent.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Sender { AGENT, STUDENT }

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val question: Question? = null,
)

sealed class AgentUiState {
    data object Loading : AgentUiState()
    data class Chatting(
        val messages: List<ChatMessage>,
        val currentQuestion: Question?,
    ) : AgentUiState()
    data class Finished(
        val messages: List<ChatMessage>,
        val score: Int,
        val total: Int,
    ) : AgentUiState()
}

class AgentViewModel(private val organId: String) : ViewModel() {

    private val repository = AgentRepository()
    private val questions  = ContentBank.questionsFor(organId)
    private val beliefs    = mutableListOf<Belief>(Belief.OrganFocused(organId))
    private val desire: Desire = Desire.Teach(organId)
    private val answeredIds = mutableSetOf<String>()
    private var feedbackPending = false
    private var msgCount = 0
    private var isFinished = false
    private var finalScore = 0
    private var finalTotal = 0

    private val _uiState = MutableStateFlow<AgentUiState>(AgentUiState.Loading)
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        val msgs = mutableListOf<ChatMessage>()
        msgs.add(agentMsg("Hola. Vamos a repasar ${ContentBank.organName(organId)}. " +
            "Te haré ${questions.size} preguntas. ¡Empieza cuando quieras!"))
        if (questions.isEmpty()) {
            msgs.add(agentMsg("No hay preguntas disponibles para este órgano aún."))
            isFinished = true
            _uiState.value = AgentUiState.Finished(msgs, 0, 0)
        } else {
            applyAction(DecisionEngine.decide(beliefs, desire, questions, false), msgs)
            _uiState.value = buildState(msgs)
        }
    }

    fun onOptionSelected(questionId: String, selectedIndex: Int) {
        val state = _uiState.value as? AgentUiState.Chatting ?: return
        val question = questions.find { it.id == questionId } ?: return
        val wasCorrect = selectedIndex == question.correctIndex

        beliefs.add(Belief.QuestionAnswered(questionId, wasCorrect))
        answeredIds.add(questionId)
        repository.recordAnswer(organId, questionId, wasCorrect)
        feedbackPending = true

        val msgs = state.messages.toMutableList()
        msgs.add(studentMsg(question.options[selectedIndex]))

        val feedbackAction = DecisionEngine.decide(beliefs, desire, questions, feedbackPending)
        feedbackPending = false
        applyAction(feedbackAction, msgs)

        _uiState.value = buildState(msgs)
    }

    private fun applyAction(action: AgentAction, msgs: MutableList<ChatMessage>) {
        when (action) {
            AgentAction.Greet -> Unit
            is AgentAction.PoseQuestion -> {
                msgs.add(agentMsg(action.question.stem, question = action.question))
            }
            is AgentAction.ProvideFeedback -> {
                val prefix = if (action.wasCorrect) "Correcto. " else "Incorrecto. "
                msgs.add(agentMsg(prefix + action.explanation))
                // Decide inmediatamente qué hacer después del feedback
                applyAction(DecisionEngine.decide(beliefs, desire, questions, false), msgs)
            }
            is AgentAction.Summarize -> {
                val pct = if (action.total > 0) action.score * 100 / action.total else 0
                val suffix = if (pct >= 70) "¡Buen trabajo!" else "Sigue practicando, lo lograrás."
                msgs.add(agentMsg("Sesión completada. Obtuviste ${action.score}/${action.total} ($pct%). $suffix"))
                isFinished = true
                finalScore = action.score
                finalTotal = action.total
            }
        }
    }

    private fun buildState(msgs: List<ChatMessage>): AgentUiState {
        if (isFinished) return AgentUiState.Finished(msgs, finalScore, finalTotal)
        val currentQuestion = msgs.lastOrNull { it.question != null && it.question.id !in answeredIds }?.question
        return AgentUiState.Chatting(msgs, currentQuestion)
    }

    private fun agentMsg(text: String, question: Question? = null) =
        ChatMessage(id = "msg_${msgCount++}", sender = Sender.AGENT, text = text, question = question)

    private fun studentMsg(text: String) =
        ChatMessage(id = "msg_${msgCount++}", sender = Sender.STUDENT, text = text)
}
