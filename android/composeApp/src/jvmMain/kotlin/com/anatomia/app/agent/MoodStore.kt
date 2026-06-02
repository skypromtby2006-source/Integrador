package com.anatomia.app.agent

import java.io.File

actual object MoodStore {
    private val file = File(
        System.getProperty("user.home") ?: ".",
        ".didactai/mood.json"
    )

    actual fun init(context: Any?) {
        file.parentFile?.mkdirs()
    }

    actual fun save(mood: String) {
        try { file.writeText(mood) } catch (_: Exception) {}
    }

    actual fun load(): String? {
        return try {
            if (file.exists()) file.readText().trim() else null
        } catch (_: Exception) { null }
    }
}
