# Fix 04 · QuizViewModel.kt — Crear (archivo nuevo)

**Ruta:** `composeApp/src/commonMain/kotlin/com/anatomia/app/ui/screens/QuizViewModel.kt`
**Depende de:** Fix 03
**Requerido por:** Fix 05, 06

---

## Por qué este archivo es el núcleo de la fase lógica

`QuizViewModel` es el puente entre los tres componentes que ya existen
pero nunca se hablaron:

```
ContentBank  ─────────────────► QuizViewModel ─► QuizScreen
DecisionEngine.evaluateAnswer()─┘              └─► QuizResultsScreen
```

Sin él, Quiz y QuizResults seguirán mostrando datos hardcodeados.

---

## Crear archivo nuevo

**Crear:** `composeApp/src/commonMain/kotlin/com/anatomia/app/ui/screens/QuizViewModel.kt`

```kotlin
package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.ContentBank
import com.anatomia.app.agent.DecisionEngine
import com.anatomia.app.agent.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Estado de la UI ───────────────────────────────────────────────────────────

sealed class QuizUiState {
    /** Cargando preguntas del JSON */
    object Loading : QuizUiState()

    /** Quiz en curso */
    data class Active(
        val questions     : List<Question>,
        val currentIndex  : Int                    = 0,
        val selectedAnswer: Int?                   = null,   // índice elegido
        val isAnswered    : Boolean                = false,
        val answers       : Map<Int, Int?>         = emptyMap() // preguntaIdx → opciónIdx (null = saltada)
    ) : QuizUiState() {
        val currentQuestion: Question get() = questions[currentIndex]
        val isLastQuestion : Boolean get() = currentIndex == questions.lastIndex
        val progress       : Float   get() = (currentIndex + 1).toFloat() / questions.size
    }

    /** Quiz terminado — datos para QuizResultsScreen */
    data class Finished(
        val questions : List<Question>,
        val answers   : Map<Int, Int?>,   // preguntaIdx → opciónIdx (null = saltada)
        val score     : Int,              // correctas
        val total     : Int
    ) : QuizUiState() {
        val pct: Float get() = if (total > 0) score.toFloat() / total else 0f
    }

    /** Error al cargar el JSON */
    data class Error(val message: String) : QuizUiState()
}

// ── ViewModel ────────────────────────────────────────────────────────────────

class QuizViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el quiz para el órgano indicado.
     * Llamar desde QuizScreen con el organId recibido por navegación.
     */
    fun loadQuiz(organId: String) {
        // Evitar recargar si ya está en curso o terminado
        if (_uiState.value !is QuizUiState.Loading) return

        viewModelScope.launch {
            try {
                val questions = ContentBank.loadQuestions(organId)
                if (questions.isEmpty()) {
                    _uiState.value = QuizUiState.Error("No hay preguntas para $organId")
                    return@launch
                }
                _uiState.value = QuizUiState.Active(questions = questions)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * El estudiante seleccionó una opción.
     * Muestra feedback visual pero no avanza todavía.
     */
    fun selectAnswer(optionIndex: Int) {
        val state = _uiState.value as? QuizUiState.Active ?: return
        if (state.isAnswered) return  // ya respondida, ignorar

        _uiState.update {
            state.copy(
                selectedAnswer = optionIndex,
                isAnswered     = true
            )
        }
    }

    /**
     * Avanza a la siguiente pregunta o termina el quiz.
     * Se llama al tocar "Siguiente pregunta".
     */
    fun nextQuestion() {
        val state = _uiState.value as? QuizUiState.Active ?: return

        // Registrar respuesta (null si isAnswered es false = saltada)
        val updatedAnswers = state.answers + (state.currentIndex to state.selectedAnswer)

        if (state.isLastQuestion) {
            _finishQuiz(state.questions, updatedAnswers)
        } else {
            _uiState.value = state.copy(
                currentIndex   = state.currentIndex + 1,
                selectedAnswer = null,
                isAnswered     = false,
                answers        = updatedAnswers
            )
        }
    }

    /**
     * El estudiante saltó la pregunta (sin seleccionar nada).
     */
    fun skipQuestion() {
        val state = _uiState.value as? QuizUiState.Active ?: return

        // Registrar como saltada (null)
        val updatedAnswers = state.answers + (state.currentIndex to null)

        if (state.isLastQuestion) {
            _finishQuiz(state.questions, updatedAnswers)
        } else {
            _uiState.value = state.copy(
                currentIndex   = state.currentIndex + 1,
                selectedAnswer = null,
                isAnswered     = false,
                answers        = updatedAnswers
            )
        }
    }

    /**
     * Reinicia el quiz desde el principio (botón "Repasar errores").
     */
    fun restart() {
        val state = _uiState.value as? QuizUiState.Finished ?: return
        _uiState.value = QuizUiState.Active(questions = state.questions)
    }

    // ── Privado ──────────────────────────────────────────────────────────────

    private fun _finishQuiz(questions: List<Question>, answers: Map<Int, Int?>) {
        val score = answers.entries.count { (idx, selected) ->
            selected != null && selected == questions[idx].correctIndex
        }
        _uiState.value = QuizUiState.Finished(
            questions = questions,
            answers   = answers,
            score     = score,
            total     = questions.size
        )
    }
}
```

---

## Notas de implementación

**`ContentBank.loadQuestions(organId)`** — verifica que la firma existente en
`ContentBank.kt` sea `suspend fun loadQuestions(organId: String): List<Question>`.
Si la función actual no es `suspend`, envuélvela con `withContext(Dispatchers.Default)`.

**`selectedAnswer: null` vs saltada** — ambas pasan por `nextQuestion()` y `skipQuestion()`.
La diferencia: `skipQuestion()` guarda `null` sin esperar respuesta.
`nextQuestion()` guarda el valor actual de `selectedAnswer` (puede ser null si se
llama sin haber seleccionado, pero la UI deshabilita ese botón hasta que hay respuesta).

**¿Por qué no usar `SavedStateHandle` para el organId?**
En KMP con Compose Navigation, `SavedStateHandle` no está disponible en commonMain.
El `organId` se pasa como parámetro de la función `loadQuiz()` desde la pantalla,
que lo recibe de `navBackStackEntry.arguments`.
