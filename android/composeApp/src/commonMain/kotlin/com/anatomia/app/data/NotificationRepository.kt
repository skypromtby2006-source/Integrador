package com.anatomia.app.data

import com.anatomia.app.data.model.AppNotification
import com.anatomia.app.data.model.NotificationType
import com.anatomia.app.db.QuestionRepository
import com.anatomia.app.network.QuestionsService
import com.anatomia.app.network.createHttpClient

class NotificationRepository {

    private val service = QuestionsService(createHttpClient())

    suspend fun checkForNewQuestions(organId: String): AppNotification? {
        return try {
            val remoteCount = service.countByOrgan(organId).getOrNull() ?: return null
            val localCount  = QuestionRepository.countByOrgan(organId)

            if (remoteCount > localCount) {
                AppNotification(
                    id      = "new_questions_$organId",
                    title   = "Nuevas preguntas disponibles",
                    body    = "Tu docente subió ${remoteCount - localCount} pregunta(s) nueva(s) de ${organIdToName(organId)}",
                    type    = NotificationType.NEW_QUESTIONS,
                    organId = organId,
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun buildAgentSuggestion(suggestionText: String): AppNotification? {
        if (suggestionText.isBlank()) return null
        return AppNotification(
            id    = "agent_suggestion",
            title = "Agente",
            body  = suggestionText,
            type  = NotificationType.AGENT_SUGGESTION,
        )
    }

    private fun organIdToName(organId: String): String = when (organId) {
        "heart"   -> "Corazón"
        "lungs"   -> "Pulmones"
        "kidneys" -> "Riñones"
        else      -> organId.replaceFirstChar { it.uppercase() }
    }
}
