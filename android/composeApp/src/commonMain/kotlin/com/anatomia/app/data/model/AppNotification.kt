package com.anatomia.app.data.model

data class AppNotification(
    val id       : String,
    val title    : String,
    val body     : String,
    val type     : NotificationType,
    val organId  : String  = "",
    val timestamp: Long    = System.currentTimeMillis(),
    val isRead   : Boolean = false,
)

enum class NotificationType {
    NEW_QUESTIONS,
    AGENT_SUGGESTION,
    ACHIEVEMENT,
}
