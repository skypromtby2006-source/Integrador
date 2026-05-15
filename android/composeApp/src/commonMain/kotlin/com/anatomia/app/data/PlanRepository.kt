package com.anatomia.app.data

import com.anatomia.app.data.model.DailyPlan

expect object PlanRepository {
    fun loadFromContext(context: Any?): DailyPlan
    fun loadFromAgent(jsonString: String): DailyPlan
}
