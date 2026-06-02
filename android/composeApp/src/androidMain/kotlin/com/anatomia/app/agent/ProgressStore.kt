package com.anatomia.app.agent

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val FILE = "quiz_progress.json"

actual object ProgressStore {

    private var ctx: Context? = null

    actual fun init(context: Any?) {
        ctx = context as Context
    }

    actual fun loadAll(): Map<String, Map<String, AnswerRecord>> {
        return try {
            val file = File(ctx!!.filesDir, FILE)
            if (!file.exists()) return emptyMap()
            val text = file.readText()
            if (text.isBlank()) return emptyMap()
            Json.decodeFromString(text)
        } catch (e: Exception) {
            // Migración silenciosa: formato viejo incompatible → borrar y empezar limpio
            try { File(ctx!!.filesDir, FILE).delete() } catch (_: Exception) {}
            emptyMap()
        }
    }

    actual fun save(data: Map<String, Map<String, AnswerRecord>>) {
        File(ctx!!.filesDir, FILE).writeText(Json.encodeToString(data))
    }

}
