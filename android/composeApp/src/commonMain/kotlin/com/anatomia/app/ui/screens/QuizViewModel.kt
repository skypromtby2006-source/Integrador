package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.agent.ContentBank
import com.anatomia.app.agent.Question
import com.anatomia.app.db.ProgressRepository
import com.anatomia.app.db.QuestionRepository
import com.anatomia.app.db.SessionRepository
import com.anatomia.app.db.SyncRepository
import com.anatomia.app.network.AnswerRecordDto
import com.anatomia.app.network.ProgressService
import com.anatomia.app.network.createHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class QuizUiState {
    object Loading : QuizUiState()

    data class Active(
        val questions     : List<Question>,
        val currentIndex  : Int            = 0,
        val selectedAnswer: Int?           = null,
        val isAnswered    : Boolean        = false,
        val answers       : Map<Int, Int?> = emptyMap(),
    ) : QuizUiState() {
        val currentQuestion: Question get() = questions[currentIndex]
        val isLastQuestion : Boolean  get() = currentIndex == questions.lastIndex
        val progress       : Float    get() = (currentIndex + 1).toFloat() / questions.size
    }

    data class Finished(
        val questions: List<Question>,
        val answers  : Map<Int, Int?>,
        val score    : Int,
        val total    : Int,
    ) : QuizUiState() {
        val pct: Float get() = if (total > 0) score.toFloat() / total else 0f
    }

    data class Error(val message: String) : QuizUiState()
}

class QuizViewModel : ViewModel() {

    private val repository = AgentRepository()
    private val progressService = ProgressService(createHttpClient())

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var currentOrganId: String = "heart"

    fun loadQuiz(organId: String) {
        currentOrganId = organId
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading

            // 1. Intenta sync del servidor (no bloquea si falla)
            SyncRepository.syncQuestionsForOrgan(organId)

            // 2. Carga desde SQLDelight
            val fromDb = QuestionRepository.getByOrgan(organId)

            // 3. Si SQLDelight tiene preguntas, úsalas; si no, usa ContentBank como fallback
            val questions = if (fromDb.isNotEmpty()) {
                fromDb
            } else {
                ContentBank.loadForOrgan(organId)
            }

            if (questions.isEmpty()) {
                _uiState.value = QuizUiState.Error("No hay preguntas disponibles para este órgano")
                return@launch
            }

            _uiState.value = QuizUiState.Active(questions = questions.shuffled())
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val state = _uiState.value as? QuizUiState.Active ?: return
        if (state.isAnswered) return
        _uiState.update { state.copy(selectedAnswer = optionIndex, isAnswered = true) }
    }

    fun nextQuestion() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        val updatedAnswers = state.answers + (state.currentIndex to state.selectedAnswer)
        if (state.isLastQuestion) {
            finishQuiz(state.questions, updatedAnswers)
        } else {
            _uiState.value = state.copy(
                currentIndex   = state.currentIndex + 1,
                selectedAnswer = null,
                isAnswered     = false,
                answers        = updatedAnswers,
            )
        }
    }

    fun skipQuestion() {
        val state = _uiState.value as? QuizUiState.Active ?: return
        val updatedAnswers = state.answers + (state.currentIndex to null)
        if (state.isLastQuestion) {
            finishQuiz(state.questions, updatedAnswers)
        } else {
            _uiState.value = state.copy(
                currentIndex   = state.currentIndex + 1,
                selectedAnswer = null,
                isAnswered     = false,
                answers        = updatedAnswers,
            )
        }
    }

    fun restart() {
        val state = _uiState.value as? QuizUiState.Finished ?: return
        _uiState.value = QuizUiState.Active(questions = state.questions)
    }

    private fun finishQuiz(questions: List<Question>, answers: Map<Int, Int?>) {
        val studentId = SessionRepository.load()?.id ?: run {
            println("[PROGRESS] No hay sesión activa — no se envía progreso")
            return
        }

        val answerRecords = mutableListOf<AnswerRecordDto>()

        answers.forEach { (idx, selected) ->
            if (selected != null) {
                val q          = questions[idx]
                val wasCorrect = selected == q.correctIndex

                repository.recordAnswer(
                    organId    = currentOrganId,
                    questionId = q.id,
                    wasCorrect = wasCorrect,
                )
                ProgressRepository.recordAnswer(
                    organId    = currentOrganId,
                    questionId = q.id,
                    wasCorrect = wasCorrect,
                )

                answerRecords.add(AnswerRecordDto(
                    questionId = q.id,
                    wasCorrect = wasCorrect
                ))
            }
        }

        val score = answers.entries.count { (idx, selected) ->
            selected != null && selected == questions[idx].correctIndex
        }
        _uiState.value = QuizUiState.Finished(
            questions = questions,
            answers   = answers,
            score     = score,
            total     = questions.size,
        )

        viewModelScope.launch {
            val ok = progressService.submitProgress(
                studentId = studentId,
                organId   = currentOrganId,
                answers   = answerRecords
            )
            if (ok) {
                println("[PROGRESS] Progreso enviado al servidor correctamente")
            } else {
                println("[PROGRESS] Fallo en envío — guardado localmente")
            }
        }
    }
}
