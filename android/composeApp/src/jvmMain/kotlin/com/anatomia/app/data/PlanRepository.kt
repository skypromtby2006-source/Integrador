package com.anatomia.app.data

import com.anatomia.app.data.model.DailyPlan
import com.anatomia.app.data.model.PlanTask

actual object PlanRepository {

    actual fun loadFromContext(context: Any?): DailyPlan = defaultPlan()

    actual fun loadFromAgent(jsonString: String): DailyPlan = defaultPlan()

    private fun defaultPlan() = DailyPlan(
        date = "2025-05-15",
        subject = "Sistema circulatorio",
        progressPct = 40,
        tasks = listOf(
            PlanTask("task_001", "El viaje de la sangre", "lectura", 5, true),
            PlanTask("task_002", "Latido del corazón", "video_3d", 3, true),
            PlanTask("task_003", "Diseña una válvula", "reto_creativo", 10, false),
            PlanTask("task_004", "Quiz · 4 preguntas", "repaso", 4, false),
        )
    )
}
