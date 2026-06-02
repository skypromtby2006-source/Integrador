package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.agent.DecisionEngine
import com.anatomia.app.agent.Desire
import com.anatomia.app.agent.MoodStore
import com.anatomia.app.agent.ProgressStore
import com.anatomia.app.agent.StudentProgress
import com.anatomia.app.db.ProgressRepository
import com.anatomia.app.db.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private data class DashboardData(
        val effectiveOrganId: String,
        val progress        : StudentProgress,
        val score           : Float,
        val organName       : String,
        val desire          : Desire,
        val steps           : List<PlanStepUiItem>,
        val beliefs         : List<BeliefUiItem>,
        val goal            : GoalUiState,
        val weekSummary     : String,
        val savedMood       : MoodOption?,
        val userName        : String,
        val weekNumber      : Int,
    )

    fun loadDashboard(organId: String = "default") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val data = withContext(Dispatchers.IO) {
                val effectiveOrganId = when (organId) {
                    "default" -> detectActiveOrgan()
                    else      -> organId
                }
                val progress  = repository.getProgress(effectiveOrganId)
                val score     = if (progress.totalAnswered > 0)
                    progress.totalCorrect.toFloat() / progress.totalAnswered else 0f
                val organName   = organIdToName(effectiveOrganId)
                val beliefs     = buildBeliefs(progress, score, organName)
                val goal        = buildGoal(organName, progress, score)
                val desire      = DecisionEngine.decideNextDesire(
                    score          = score,
                    attemptCount   = progress.totalAnswered,
                    organId        = effectiveOrganId,
                    totalQuestions = 8,
                    answeredCount  = progress.totalAnswered,
                )
                val steps       = buildPlanSteps(desire, progress, effectiveOrganId)
                val weekSummary = buildWeekSummary(score, organName)
                val savedMood   = MoodStore.load()
                    ?.let { runCatching { MoodOption.valueOf(it) }.getOrNull() }
                val userName    = SessionRepository.load()
                    ?.name
                    ?.split(" ")
                    ?.firstOrNull()
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Estudiante"
                val weekNumber  = calculateCurrentWeek()
                DashboardData(effectiveOrganId, progress, score, organName, desire, steps, beliefs, goal, weekSummary, savedMood, userName, weekNumber)
            }

            _uiState.update {
                AgentDashboardUiState(
                    isLoading    = false,
                    userName     = data.userName,
                    weekNumber   = data.weekNumber,
                    weekSummary  = data.weekSummary,
                    beliefs      = data.beliefs,
                    goal         = data.goal,
                    planSteps    = data.steps,
                    organId      = data.effectiveOrganId,
                    selectedMood = data.savedMood,
                )
            }
        }
    }

    fun detectActiveOrgan(): String {
        val allProgress = ProgressStore.loadAll()
        if (allProgress.isEmpty()) return "heart"
        return allProgress.entries
            .maxByOrNull { entry ->
                entry.value.values.maxOfOrNull { it.answeredAt } ?: 0L
            }
            ?.key ?: "heart"
    }

    fun selectMood(mood: MoodOption) {
        _uiState.update { it.copy(selectedMood = mood) }
        MoodStore.save(mood.name)
    }

    // ── Privado ──────────────────────────────────────────────────────────────

    private fun calculateCurrentWeek(): Int {
        return try {
            val firstAnswerAt = ProgressRepository.getFirstAnsweredAt()
            if (firstAnswerAt == null) return 1
            val msPerWeek = 7L * 24 * 60 * 60 * 1000
            val weeksSince = ((System.currentTimeMillis() - firstAnswerAt) / msPerWeek).toInt()
            (weeksSince + 1).coerceAtLeast(1)
        } catch (e: Exception) {
            1
        }
    }

    private fun countDistinctSessions(progress: StudentProgress): Int {
        if (progress.answeredQuestions.isEmpty()) return 0
        val MS_PER_DAY = 24L * 60 * 60 * 1000
        return progress.answeredQuestions.values
            .map { it.answeredAt / MS_PER_DAY }
            .distinct()
            .size
    }

    private fun organIdToName(organId: String): String = when (organId) {
        "heart"   -> "Corazón"
        "lungs"   -> "Pulmones"
        "kidneys" -> "Riñones"
        else      -> organId.replaceFirstChar { it.uppercase() }
    }

    private fun buildGoal(
        organName: String,
        progress : StudentProgress,
        score    : Float,
    ): GoalUiState {
        val title = when {
            score >= 0.70f -> "Dominar $organName a nivel avanzado"
            score >= 0.35f -> "Consolidar el conocimiento de $organName"
            else           -> "Aprender los fundamentos de $organName"
        }

        val targetAnswers = 8
        val progressPct = when {
            score >= 0.70f             -> 1.0f
            progress.totalAnswered == 0 -> 0f
            else -> (score * 0.85f + (progress.totalAnswered.toFloat() / targetAnswers) * 0.15f)
                .coerceIn(0f, 0.95f)
        }

        val daysRemaining = when {
            score >= 0.70f              -> 0
            progress.totalAnswered == 0 -> 7
            else -> {
                val remaining     = (targetAnswers - progress.totalAnswered).coerceAtLeast(1)
                val sessionCount  = countDistinctSessions(progress).coerceAtLeast(1)
                val avgPerSession = (progress.totalAnswered.toFloat() / sessionCount)
                    .coerceAtLeast(1f)
                (remaining / avgPerSession).toInt().coerceIn(1, 14)
            }
        }

        val rhythm = when {
            score >= 0.70f              -> "meta alcanzada"
            progress.totalAnswered >= 5 -> "buen ritmo"
            progress.totalAnswered >= 2 -> "empezando"
            else                        -> "sin datos aún"
        }

        return GoalUiState(
            title         = title,
            progressPct   = progressPct,
            daysRemaining = daysRemaining,
            rhythm        = rhythm,
        )
    }

    private fun buildBeliefs(progress: StudentProgress, score: Float, organName: String): List<BeliefUiItem> {
        val beliefs = mutableListOf<BeliefUiItem>()

        when {
            score >= 0.70f -> beliefs.add(
                BeliefUiItem(
                    title  = "Buen dominio de $organName",
                    detail = "Acertaste ${progress.totalCorrect} de ${progress.totalAnswered} preguntas en total",
                    level  = BeliefLevel.STRONG,
                )
            )
            score >= 0.35f -> beliefs.add(
                BeliefUiItem(
                    title  = "Conocimiento intermedio de $organName",
                    detail = "Hay margen de mejora — ${progress.totalCorrect} correctas de ${progress.totalAnswered}",
                    level  = BeliefLevel.PATTERN,
                )
            )
            progress.totalAnswered > 0 -> beliefs.add(
                BeliefUiItem(
                    title  = "Estás comenzando con $organName",
                    detail = "Con más práctica el agente ajustará el nivel automáticamente",
                    level  = BeliefLevel.WEAK,
                )
            )
            else -> beliefs.add(
                BeliefUiItem(
                    title  = "Sin datos aún para $organName",
                    detail = "Completa tu primer quiz para que el agente te conozca",
                    level  = BeliefLevel.WEAK,
                )
            )
        }

        if (progress.totalAnswered >= 3) {
            val ratio = progress.totalCorrect.toFloat() / progress.totalAnswered
            if (ratio >= 0.80f) {
                beliefs.add(
                    BeliefUiItem(
                        title  = "Respuestas consistentes",
                        detail = "Tus aciertos se mantienen por encima del 80% — patrón sólido",
                        level  = BeliefLevel.STRONG,
                    )
                )
            } else if (ratio < 0.40f) {
                beliefs.add(
                    BeliefUiItem(
                        title  = "El agente detectó dificultad frecuente",
                        detail = "Menos del 40% de aciertos — el plan de refuerzo está activo",
                        level  = BeliefLevel.WEAK,
                    )
                )
            }
        }

        return beliefs
    }

    private fun buildPlanSteps(desire: Desire, progress: StudentProgress, organId: String): List<PlanStepUiItem> {
        val organName = organIdToName(organId)

        return when (desire) {

            is Desire.Teach -> listOf(
                PlanStepUiItem(
                    order       = 1,
                    title       = "Explorar $organName en 3D",
                    reason      = "Construir la imagen mental antes de responder preguntas",
                    durationMin = 5,
                    status      = if (progress.totalAnswered > 0) StepStatus.DONE else StepStatus.CURRENT,
                ),
                PlanStepUiItem(
                    order       = 2,
                    title       = "Quiz de introducción — $organName",
                    reason      = "Medir tu punto de partida con preguntas de nivel básico",
                    durationMin = 8,
                    status      = if (progress.totalAnswered > 0) StepStatus.CURRENT else StepStatus.PENDING,
                ),
                PlanStepUiItem(
                    order       = 3,
                    title       = "Revisar resultados con el agente",
                    reason      = "El agente analiza tus patrones y ajusta el siguiente paso",
                    durationMin = 3,
                    status      = StepStatus.PENDING,
                ),
                PlanStepUiItem(
                    order       = 4,
                    title       = "Quiz de consolidación",
                    reason      = "Afianzar lo aprendido con preguntas de nivel medio",
                    durationMin = 10,
                    status      = StepStatus.PENDING,
                ),
            )

            is Desire.Reinforce -> listOf(
                PlanStepUiItem(
                    order       = 1,
                    title       = "Repasar errores — $organName",
                    reason      = "Volver a los temas donde fallaste para corregir conceptos",
                    durationMin = 8,
                    status      = StepStatus.CURRENT,
                ),
                PlanStepUiItem(
                    order       = 2,
                    title       = "Quiz de refuerzo",
                    reason      = "Preguntas específicas de los temas con más errores",
                    durationMin = 6,
                    status      = StepStatus.PENDING,
                ),
                PlanStepUiItem(
                    order       = 3,
                    title       = "Comparar con sesión anterior",
                    reason      = "Ver tu progreso real respecto al último intento",
                    durationMin = 3,
                    status      = StepStatus.PENDING,
                ),
            )

            is Desire.Motivate -> listOf(
                PlanStepUiItem(
                    order       = 1,
                    title       = "¡Nivel avanzado — $organName!",
                    reason      = "Tu dominio del tema es sólido — hora de un desafío real",
                    durationMin = 10,
                    status      = StepStatus.CURRENT,
                ),
                PlanStepUiItem(
                    order       = 2,
                    title       = "Explorar el siguiente sistema",
                    reason      = "Estás listo para ampliar a nuevos órganos relacionados",
                    durationMin = 5,
                    status      = StepStatus.PENDING,
                ),
            )
        }
    }

    private fun buildWeekSummary(score: Float, organName: String): String {
        val pct = (score * 100).toInt()
        return if (pct > 0)
            "Esta semana avanzaste un $pct% en $organName. Sigues mejorando — vamos a mantener el ritmo."
        else
            "Todavía no hay datos de esta semana. ¡Empieza tu primer quiz de $organName!"
    }
}
