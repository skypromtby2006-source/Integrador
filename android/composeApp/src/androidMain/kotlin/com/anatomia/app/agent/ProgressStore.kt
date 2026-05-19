package com.anatomia.app.agent

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val FILE = "quiz_progress.json"

actual object ProgressStore {

    private var ctx: Context? = null

    actual fun init(context: Any?) {
        ctx = context as Context
    }

    actual fun loadAll(): Map<String, Map<String, Boolean>> = try {
        val text = ctx!!.openFileInput(FILE).bufferedReader().use { it.readText() }
        Json.decodeFromString(text)
    } catch (_: Exception) {
        emptyMap()
    }

    actual fun save(data: Map<String, Map<String, Boolean>>) {
        ctx!!.openFileOutput(FILE, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(Json.encodeToString(data))
        }
    }
}
