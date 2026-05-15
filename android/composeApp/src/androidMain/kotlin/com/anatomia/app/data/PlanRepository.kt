package com.anatomia.app.data

import android.content.Context
import com.anatomia.app.data.model.DailyPlan
import com.anatomia.app.data.model.PlanTask
import org.json.JSONObject

actual object PlanRepository {

    actual fun loadFromContext(context: Any?): DailyPlan {
        val ctx = context as Context
        val json = ctx.assets.open("daily_plan.json").bufferedReader().use { it.readText() }
        return parsePlan(json)
    }

    actual fun loadFromAgent(jsonString: String): DailyPlan = parsePlan(jsonString)

    private fun parsePlan(json: String): DailyPlan {
        val root = JSONObject(json)
        val arr = root.getJSONArray("tasks")
        val tasks = (0 until arr.length()).map { i ->
            val t = arr.getJSONObject(i)
            PlanTask(
                id = t.getString("id"),
                title = t.getString("title"),
                type = t.getString("type"),
                durationMin = t.getInt("duration_min"),
                completed = t.getBoolean("completed")
            )
        }
        return DailyPlan(
            date = root.getString("date"),
            subject = root.getString("subject"),
            progressPct = root.getInt("progress_pct"),
            tasks = tasks
        )
    }
}
