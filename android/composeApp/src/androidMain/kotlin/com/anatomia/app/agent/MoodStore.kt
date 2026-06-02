package com.anatomia.app.agent

import android.content.Context
import java.io.File

actual object MoodStore {
    private var ctx: Context? = null

    actual fun init(context: Any?) {
        ctx = context as? Context
    }

    actual fun save(mood: String) {
        try {
            File(ctx!!.filesDir, "mood.json").writeText(mood)
        } catch (_: Exception) {}
    }

    actual fun load(): String? {
        return try {
            val file = File(ctx!!.filesDir, "mood.json")
            if (file.exists()) file.readText().trim() else null
        } catch (_: Exception) { null }
    }
}
