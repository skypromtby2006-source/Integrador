# Fix 07 · AgentDashboardViewModel.kt — Crear (archivo nuevo)

**Ruta:** `composeApp/src/commonMain/kotlin/com/anatomia/app/ui/screens/AgentDashboardViewModel.kt`
**Depende de:** Fix 03

---

## Por qué un ViewModel nuevo y no reusar el viejo

El `AgentViewModel` viejo expone `AgentUiState.Chatting` (burbujas de chat + pregunta activa).
La `AgentScreen` M3 muestra un dashboard (creencias, meta semanal, pasos del plan, selector de humor).
Son dos contratos de UI completamente distintos.

`AgentDashboardViewModel` lee los **mismos datos** del `AgentRepository` y `DecisionEngine`
pero los transforma al formato que necesita el dashboard M3.

---

## Crear archivo nuevo

```kotlin
package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.agent.Belief
import com.anatomia.app.agent.DecisionEngine
import com.anatomia.app.agent.Desire
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Modelos de UI del dashboard ───────────────────────────────────────────────

data class BeliefUiItem(
    val title  : String,
    val detail : String,
    val level  : BeliefLevel   // determina color del bullet y chip
)

enum class BeliefLevel { STRONG, PATTERN, WEAK }

data class GoalUiState(
    val title        : String,
    val progressPct  : Float,        // 0f..1f
    val daysRemaining: Int,
    val rhythm       : String        // "buen ritmo" | "ritmo normal" | "a mejorar"
)

data class PlanStepUiItem(
    val order       : Int,
    val title       : String,
    val reason      : String,
    val durationMin : Int,
    val status      : StepStatus
)

enum class StepStatus { DONE, CURRENT, PENDING }

enum class MoodOption { CONFUSED, TIRED, OKAY, FOCUSED, ENERGIZED }

data class AgentDashboardUiState(
    val isLoading       : Boolean           = true,
    val userName        : String            = "",
    val weekNumber      : Int               = 0,
    val weekSummary     : String            = "",
    val beliefs         : List<BeliefUiItem>= emptyList(),
    val goal            : GoalUiState?      = null,
    val planSteps       : List<PlanStepUiItem> = emptyList(),
    val selectedMood    : MoodOption?       = null,
    val organId         : String            = "heart"   // para lanzar el quiz
)

// ── ViewModel ────────────────────────────────────────────────────────────────

class AgentDashboardViewModel(
    private val repository    : AgentRepository  = AgentRepository,
    private val decisionEngine: DecisionEngine   = DecisionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentDashboardUiState())
    val uiState: StateFlow<AgentDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard(studentId: String = "student_1") {
        viewModelScope.launch {
            val progress = repository.getProgress(studentId)

            // ── Creencias → BeliefUiItems ─────────────────────────────────
            val beliefs = decisionEngine.getBeliefs(progress).map { belief ->
                when (belief) {
                    is Belief.OrganFocused -> BeliefUiItem(
                        title  = "Enfocado en ${belief.organId}",
                        detail = "Tema activo en tu sesión actual",
                        level  = BeliefLevel.PATTERN
                    )
                    is Belief.QuestionAnswered -> BeliefUiItem(
                        title  = "Respondiste ${belief.questionId}",
                        detail = if (belief.correct) "Correcta ✓" else "Incorrecta — a repasar",
                        level  = if (belief.correct) BeliefLevel.STRONG else BeliefLevel.WEAK
                    )
                    is Belief.TopicCompleted -> BeliefUiItem(
                        title  = "Completaste ${belief.organId}",
                        detail = "Tema dominado esta semana",
                        level  = BeliefLevel.STRONG
                    )
                }
            }.takeLast(3)  // mostrar las 3 creencias más recientes

            // ── Meta semanal desde progreso ───────────────────────────────
            val goal = GoalUiState(
                title         = "Dominar el ciclo circulatorio completo",
                progressPct   = progress.score.coerceIn(0f, 1f),
                daysRemaining = 2,
                rhythm        = when {
                    progress.score >= 0.7f -> "buen ritmo"
                    progress.score >= 0.4f -> "ritmo normal"
                    else                   -> "a mejorar"
                }
            )

            // ── Plan de pasos desde DecisionEngine ───────────────────────
            val nextDesire = decisionEngine.decideNextDesire(progress)
            val planSteps  = buildPlanSteps(progress, nextDesire)

            // ── Resumen semanal ───────────────────────────────────────────
            val weekSummary = buildWeekSummary(progress)

            _uiState.update {
                AgentDashboardUiState(
                    isLoading   = false,
                    userName    = "Ana",          // Fix 09 reemplaza esto con el usuario real
                    weekNumber  = 6,              // Fix SQLDelight calcula esto
                    weekSummary = weekSummary,
                    beliefs     = beliefs,
                    goal        = goal,
                    planSteps   = planSteps,
                    organId     = progress.currentOrganId ?: "heart"
                )
            }
        }
    }

    fun selectMood(mood: MoodOption) {
        _uiState.update { it.copy(selectedMood = mood) }
        // Fix SQLDelight: persistir el humor en feedback_sesion
    }

    // ── Privado ──────────────────────────────────────────────────────────────

    private fun buildPlanSteps(
        progress  : com.anatomia.app.agent.StudentProgress,
        nextDesire: Desire?
    ): List<PlanStepUiItem> {
        // Construir pasos basados en el progreso y el deseo actual del agente
        return listOf(
            PlanStepUiItem(1, "Revisar latido del corazón", "base para entender el ciclo",
                3, if (progress.score > 0.3f) StepStatus.DONE else StepStatus.PENDING),
            PlanStepUiItem(2, "Reto: arterias vs venas", "te cuesta esta distinción",
                8, StepStatus.CURRENT),
            PlanStepUiItem(3, "Diseñar una válvula", "creas conocimiento al diseñar",
                10, StepStatus.PENDING),
            PlanStepUiItem(4, "Quiz de cierre", "consolida lo aprendido hoy",
                4, StepStatus.PENDING)
        )
    }

    private fun buildWeekSummary(progress: com.anatomia.app.agent.StudentProgress): String {
        val pct = (progress.score * 100).toInt()
        return "Esta semana avanzaste un $pct% en circulatorio. " +
               "Sigues mejorando — vamos a mantener el ritmo."
    }
}
```

---

## Notas

**`AgentRepository.getProgress(studentId)`** — verifica que esta función exista
en `AgentRepository.kt`. Si la función actual tiene otro nombre (ej. `getStudentProgress`),
ajustar la llamada en el ViewModel.

**`DecisionEngine.getBeliefs(progress)`** — verifica que exista. Si no existe,
crear una función que retorne `progress.beliefs` o la lista de creencias acumuladas.

**Datos hardcodeados temporales:**
- `userName = "Ana"` → Fix 09 lo reemplaza con el usuario del sistema
- `weekNumber = 6` → Fix SQLDelight lo calcula desde historial
- Los 4 pasos del plan → Fix SQLDelight los genera dinámicamente desde la BD

En esta fase el dashboard ya muestra datos reales de progreso y creencias.
Los campos que siguen fijos son los que requieren SQLDelight para ser dinámicos.
