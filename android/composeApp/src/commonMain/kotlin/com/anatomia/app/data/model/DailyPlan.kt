package com.anatomia.app.data.model

data class DailyPlan(
    val date: String,
    val subject: String,
    val progressPct: Int,
    val tasks: List<PlanTask>
)

data class PlanTask(
    val id: String,
    val title: String,
    val type: String,
    val durationMin: Int,
    val completed: Boolean
)
