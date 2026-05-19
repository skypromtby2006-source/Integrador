package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.agent.DecisionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Modelos de UI del dashboard ───────────────────────────────────────────────

data class BeliefUiItem(
    val title  : String,
    val detail : String,
    val level  : BeliefLevel,
)

enum class BeliefLevel { STRONG, PATTERN, WEAK }

data class GoalUiState(
    val title        : String,
    val progressPct  : Float,
    val daysRemaining: Int,
    val rhythm       : String,
)

data class PlanStepUiItem(
    val order      : Int,
    val title      : String,
    val reason     : String,
    val durationMin: Int,
    val status     : StepStatus,
)

enum class StepStatus { DONE, CURRENT, PENDING }

enum class MoodOption { CONFUSED, TIRED, OKAY, FOCUSED, ENERGIZED }

data class AgentDashboardUiState(
    val isLoading   : Boolean               = true,
    val userName    : String                = "",
    val weekNumber  : Int                   = 0,
    val weekSummary : String                = "",
    val beliefs     : List<BeliefUiItem>    = emptyList(),
    val goal        : GoalUiState?          = null,
    val planSteps   : List<PlanStepUiItem>  = emptyList(),
    val selectedMood: MoodOption?           = null,
    val organId     : String                = "heart",
)

// ── ViewModel ────────────────────────────────────────────────────────────────

class AgentDashboardViewModel : ViewModel() {

    private val repository = AgentRepository()

    private val _uiState = MutableStateFlow(AgentDashboardUiState())
    val uiState: StateFlow<AgentDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard(organId: String = "heart") {
        viewModelScope.launch {
            val progress = repository.getProgress(organId)
            val score    = if (progress.totalAnswered > 0)
                progress.totalCorrect.toFloat() / progress.totalAnswered
            else 0f

            val beliefs = buildBeliefs(progress, organId)

            val goal = GoalUiState(
                title         = "Dominar el ciclo circulatorio completo",
                progressPct   = score.coerceIn(0f, 1f),
                daysRemaining = 2,
                rhythm        = when {
                    score >= 0.7f -> "buen ritmo"
                    score >= 0.4f -> "ritmo normal"
                    else          -> "a mejorar"
                },
            )

            val desire      = DecisionEngine.decideNextDesire(
                score          = score,
                attemptCount   = progress.totalAnswered,
                organId        = organId,
                answeredCount  = progress.totalAnswered,
            )
            val planSteps   = buildPlanSteps(score)
            val weekSummary = buildWeekSummary(score)

            _uiState.update {
                AgentDashboardUiState(
                    isLoading   = false,
                    userName    = "Ana",        // TODO: reemplazar con UserRepository (SQLDelight)
                    weekNumber  = 6,            // TODO: calcular desde historial (SQLDelight)
                    weekSummary = weekSummary,
                    beliefs     = beliefs,
                    goal        = goal,
                    planSteps   = planSteps,
                    organId     = organId,
                )
            }
        }
    }

    fun selectMood(mood: MoodOption) {
        _uiState.update { it.copy(selectedMood = mood) }
        // TODO: persistir el humor (SQLDelight)
    }

    // ── Privado ──────────────────────────────────────────────────────────────

    private fun buildBeliefs(
        progress: com.anatomia.app.agent.StudentProgress,
        organId : String,
    ): List<BeliefUiItem> {
        val list = mutableListOf<BeliefUiItem>()

        if (progress.totalAnswered == 0) {
            list += BeliefUiItem(
                title  = "Sin sesiones todavía",
                detail = "Completa tu primer quiz para ver tus creencias",
                level  = BeliefLevel.PATTERN,
            )
            return list
        }

        val correctPct = if (progress.totalAnswered > 0)
            progress.totalCorrect * 100 / progress.totalAnswered else 0

        list += BeliefUiItem(
            title  = "Aciertas el $correctPct% en $organId",
            detail = "${progress.totalCorrect} correctas de ${progress.totalAnswered} respondidas",
            level  = if (correctPct >= 70) BeliefLevel.STRONG else BeliefLevel.WEAK,
        )

        val incorrectIds = progress.answeredQuestions
            .filter { !it.value }
            .keys
            .toList()

        if (incorrectIds.isNotEmpty()) {
            list += BeliefUiItem(
                title  = "${incorrectIds.size} preguntas a repasar",
                detail = "El agente las incluirá en tu próxima sesión",
                level  = BeliefLevel.WEAK,
            )
        } else {
            list += BeliefUiItem(
                title  = "Sin errores registrados",
                detail = "Vas muy bien — prueba preguntas más difíciles",
                level  = BeliefLevel.STRONG,
            )
        }

        list += BeliefUiItem(
            title  = "Sesiones activas",
            detail = "Has respondido ${progress.totalAnswered} preguntas en total",
            level  = BeliefLevel.PATTERN,
        )

        return list.takeLast(3)
    }

    private fun buildPlanSteps(score: Float): List<PlanStepUiItem> = listOf(
        PlanStepUiItem(1, "Revisar latido del corazón", "base para entender el ciclo",
            3, if (score > 0.3f) StepStatus.DONE else StepStatus.PENDING),
        PlanStepUiItem(2, "Reto: arterias vs venas", "te cuesta esta distinción — práctica visual",
            8, StepStatus.CURRENT),
        PlanStepUiItem(3, "Diseñar una válvula", "creas conocimiento al diseñar",
            10, StepStatus.PENDING),
        PlanStepUiItem(4, "Quiz de cierre", "consolida lo aprendido hoy",
            4, StepStatus.PENDING),
    )

    private fun buildWeekSummary(score: Float): String {
        val pct = (score * 100).toInt()
        return if (pct > 0)
            "Esta semana avanzaste un $pct% en circulatorio. Sigues mejorando — vamos a mantener el ritmo."
        else
            "Todavía no hay datos de esta semana. ¡Empieza tu primer quiz!"
    }
}
