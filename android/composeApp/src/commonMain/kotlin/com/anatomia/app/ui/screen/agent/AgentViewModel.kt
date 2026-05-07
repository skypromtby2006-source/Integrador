package com.anatomia.app.ui.screen.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.AgentAction
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.agent.Belief
import com.anatomia.app.agent.ContentBank
import com.anatomia.app.agent.DecisionEngine
import com.anatomia.app.agent.Desire
import com.anatomia.app.agent.Question
import com.anatomia.app.agent.StudentState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val repository   = AgentRepository()
    private var questions    = emptyList<Question>()
    private val beliefs      = mutableListOf<Belief>(Belief.OrganFocused(organId))
    private var desire: Desire = Desire.Teach(organId)
    private val answeredIds  = mutableSetOf<String>()
    private var studentState = StudentState()
    private var msgCount     = 0

    private val _uiState = MutableStateFlow<AgentUiState>(AgentUiState.Loading)
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            questions = ContentBank.loadForOrgan(organId)
            initializeChat()
        }
    }

    private fun initializeChat() {
        val msgs = mutableListOf<ChatMessage>()
        msgs.add(agentMsg("Hola. Vamos a repasar ${ContentBank.organName(organId)}. " +
            "Te haré ${questions.size} preguntas. ¡Empieza cuando quieras!"))
        if (questions.isEmpty()) {
            msgs.add(agentMsg("No hay preguntas disponibles para este órgano aún."))
            _uiState.value = AgentUiState.Finished(msgs, 0, 0)
        } else {
            val action = DecisionEngine.decide(beliefs, desire, questions, false,
                studentState.knowledgeScore, studentState.incorrectTopics, answeredIds)
            if (action is AgentAction.PoseQuestion) {
                if (action.contextMessage.isNotEmpty()) msgs.add(agentMsg(action.contextMessage))
                msgs.add(agentMsg(action.question.stem, question = action.question))
                _uiState.value = AgentUiState.Chatting(msgs, action.question)
            } else {
                _uiState.value = AgentUiState.Chatting(msgs, null)
            }
        }
    }

    fun onOptionSelected(questionId: String, selectedIndex: Int) {
        val state    = _uiState.value as? AgentUiState.Chatting ?: return
        val question = questions.find { it.id == questionId } ?: return

        // 1. Evaluar respuesta y actualizar score
        val result = DecisionEngine.evaluateAnswer(
            question        = question,
            selectedIndex   = selectedIndex,
            currentScore    = studentState.knowledgeScore,
            incorrectTopics = studentState.incorrectTopics,
        )
        studentState = studentState.copy(
            knowledgeScore  = result.newScore,
            correctCount    = studentState.correctCount + if (result.isCorrect) 1 else 0,
            incorrectCount  = studentState.incorrectCount + if (result.isCorrect) 0 else 1,
            incorrectTopics = result.updatedIncorrectTopics,
        )
        beliefs.add(Belief.QuestionAnswered(questionId, result.isCorrect))
        answeredIds.add(questionId)
        repository.recordAnswer(organId, questionId, result.isCorrect)

        // 2. Mostrar selección del estudiante + feedback inmediatamente.
        //    currentQuestion = null hace que las opciones desaparezcan.
        val prefix = if (result.isCorrect) "Correcto. " else "Incorrecto. "
        val msgs   = state.messages.toMutableList()
        msgs.add(studentMsg(question.options[selectedIndex]))
        msgs.add(agentMsg(prefix + result.explanation))
        _uiState.value = AgentUiState.Chatting(messages = msgs, currentQuestion = null)

        // 3. Esperar 2 s para que la UI renderice el feedback, luego avanzar
        viewModelScope.launch {
            delay(2000)
            advanceToNextStep()
        }
    }

    private fun advanceToNextStep() {
        val state = _uiState.value as? AgentUiState.Chatting ?: return
        val msgs  = state.messages.toMutableList()

        desire = DecisionEngine.decideNextDesire(
            score           = studentState.knowledgeScore,
            attemptCount    = answeredIds.size,
            organId         = organId,
            totalQuestions  = questions.size,
            answeredCount   = answeredIds.size,
        )

        when (val action = DecisionEngine.decide(
            beliefs, desire, questions, false,
            studentState.knowledgeScore, studentState.incorrectTopics, answeredIds,
        )) {
            is AgentAction.PoseQuestion -> {
                if (action.contextMessage.isNotEmpty()) msgs.add(agentMsg(action.contextMessage))
                msgs.add(agentMsg(action.question.stem, question = action.question))
                _uiState.value = AgentUiState.Chatting(messages = msgs, currentQuestion = action.question)
            }
            is AgentAction.Summarize -> {
                val pct    = if (action.total > 0) action.score * 100 / action.total else 0
                val suffix = if (pct >= 70) "¡Buen trabajo!" else "Sigue practicando, lo lograrás."
                msgs.add(agentMsg("Sesión completada. Obtuviste ${action.score}/${action.total} ($pct%). $suffix"))
                _uiState.value = AgentUiState.Finished(messages = msgs, score = action.score, total = action.total)
            }
            is AgentAction.ProvideFeedback -> {
                // No debería ocurrir aquí (feedbackPending = false), pero si ocurre se muestra
                msgs.add(agentMsg(action.explanation))
                _uiState.value = AgentUiState.Chatting(messages = msgs, currentQuestion = null)
            }
            AgentAction.Greet -> Unit
        }
    }

    private fun agentMsg(text: String, question: Question? = null) =
        ChatMessage(id = "msg_${msgCount++}", sender = Sender.AGENT, text = text, question = question)

    private fun studentMsg(text: String) =
        ChatMessage(id = "msg_${msgCount++}", sender = Sender.STUDENT, text = text)
}
