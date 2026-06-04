package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.agent.DecisionEngine
import com.anatomia.app.agent.Desire
import com.anatomia.app.agent.ProgressStore
import com.anatomia.app.data.NotificationRepository
import com.anatomia.app.data.model.AppNotification
import com.anatomia.app.data.model.DailyPlan
import com.anatomia.app.data.model.PlanTask
import com.anatomia.app.db.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HomeUiState(
    val isLoading      : Boolean              = true,
    val plan           : DailyPlan?           = null,
    val userName       : String               = "",
    val currentOrganId : String               = "heart",
    val suggestionTask : PlanTask?            = null,
    val suggestionLabel: String               = "",
    val notifications  : List<AppNotification> = emptyList(),
    val unreadCount    : Int                  = 0,
)

class HomeViewModel : ViewModel() {

    private val repository             = AgentRepository()
    private val notificationRepository = NotificationRepository()
    private val _uiState               = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            withContext(Dispatchers.IO) {
                val organId   = detectActiveOrgan()
                val organName = organIdToName(organId)
                val progress  = repository.getProgress(organId)
                val score     = if (progress.quizAnswered > 0)
                    progress.quizCorrect.toFloat() / progress.quizAnswered
                else 0f

                val desire = DecisionEngine.decideNextDesire(
                    score          = score,
                    attemptCount   = progress.quizAnswered,
                    organId        = organId,
                    totalQuestions = 8,
                    answeredCount  = progress.quizAnswered,
                )

                val tasks = buildPlanTasksFromDesire(desire, organId, organName, progress)

                val suggestionTask = tasks.firstOrNull { !it.completed }
                val suggestionLabel = when (desire) {
                    is Desire.Motivate  -> "SIGUIENTE DESAFÍO · AGENTE"
                    is Desire.Reinforce -> "REFUERZO RECOMENDADO · AGENTE"
                    is Desire.Teach     -> "SUGERENCIA · AGENTE"
                }

                val plan = DailyPlan(
                    date        = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date.toString(),
                    subject     = organName,
                    progressPct = (score * 100).toInt().coerceIn(0, 100),
                    tasks       = tasks,
                )

                val userName = SessionRepository.load()
                    ?.name
                    ?.split(" ")
                    ?.firstOrNull()
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Estudiante"

                // Polling de notificaciones
                val notifications = mutableListOf<AppNotification>()
                notificationRepository.buildAgentSuggestion(
                    buildSuggestionText(suggestionTask)
                )?.let { notifications.add(it) }
                listOf("heart", "lungs", "kidneys").forEach { oid ->
                    notificationRepository.checkForNewQuestions(oid)
                        ?.let { notifications.add(it) }
                }

                _uiState.update {
                    it.copy(
                        isLoading       = false,
                        plan            = plan,
                        userName        = userName,
                        currentOrganId  = organId,
                        suggestionTask  = suggestionTask,
                        suggestionLabel = suggestionLabel,
                        notifications   = notifications,
                        unreadCount     = notifications.size,
                    )
                }
            }
        }
    }

    private fun detectActiveOrgan(): String {
        val allProgress = ProgressStore.loadAll()
        if (allProgress.isEmpty()) return "heart"
        return allProgress.entries
            .maxByOrNull { entry ->
                entry.value.values.maxOfOrNull { it.answeredAt } ?: 0L
            }
            ?.key ?: "heart"
    }

    private fun organIdToName(organId: String): String = when (organId) {
        "heart"   -> "Corazón"
        "lungs"   -> "Pulmones"
        "kidneys" -> "Riñones"
        else      -> organId.replaceFirstChar { it.uppercase() }
    }

    private fun buildSuggestionText(task: PlanTask?): String = when (task?.type) {
        "video_3d"      -> "¿exploramos el modelo 3D? es el mejor punto de partida"
        "repaso"        -> "¿hacemos el quiz ahora? el agente ajustó las preguntas a tu nivel"
        "lectura"       -> "hay una lectura corta que refuerza lo que aprendiste hoy"
        "reto_creativo" -> "¿saltamos al reto creativo? va bien con lo que aprendiste hoy"
        else            -> ""
    }

    private fun buildPlanTasksFromDesire(
        desire   : Desire,
        organId  : String,
        organName: String,
        progress : com.anatomia.app.agent.StudentProgress,
    ): List<PlanTask> = when (desire) {

        is Desire.Teach -> listOf(
            PlanTask(
                id          = "task_read_$organId",
                title       = "Leer sobre $organName",
                type        = "lectura",
                durationMin = 10,
                completed   = false,
            ),
            PlanTask(
                id          = "task_explore_$organId",
                title       = "Explorar $organName en 3D",
                type        = "video_3d",
                durationMin = 5,
                completed   = progress.exploredTopics.isNotEmpty(),
            ),
            PlanTask(
                id          = "task_quiz_intro_$organId",
                title       = "Quiz de introducción · $organName",
                type        = "repaso",
                durationMin = 8,
                completed   = false,
            ),
            PlanTask(
                id          = "task_review_$organId",
                title       = "Revisar resultados con el agente",
                type        = "lectura",
                durationMin = 3,
                completed   = false,
            ),
        )

        is Desire.Reinforce -> listOf(
            PlanTask(
                id          = "task_reinforce_$organId",
                title       = "Repasar errores · $organName",
                type        = "repaso",
                durationMin = 8,
                completed   = false,
            ),
            PlanTask(
                id          = "task_quiz_refuerzo_$organId",
                title       = "Quiz de refuerzo",
                type        = "repaso",
                durationMin = 6,
                completed   = false,
            ),
            PlanTask(
                id          = "task_agent_$organId",
                title       = "Ver análisis del agente",
                type        = "lectura",
                durationMin = 3,
                completed   = false,
            ),
        )

        is Desire.Motivate -> listOf(
            PlanTask(
                id          = "task_advanced_$organId",
                title       = "Nivel avanzado · $organName",
                type        = "repaso",
                durationMin = 10,
                completed   = false,
            ),
            PlanTask(
                id          = "task_next_organ",
                title       = "Explorar siguiente sistema",
                type        = "video_3d",
                durationMin = 5,
                completed   = false,
            ),
        )
    }
}
