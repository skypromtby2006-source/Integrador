package com.anatomia.app.agent

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val file = File("quiz_progress.json")

actual object ProgressStore {

    actual fun init(context: Any?) = Unit

    actual fun loadAll(): Map<String, Map<String, Boolean>> = try {
        Json.decodeFromString(file.readText())
    } catch (_: Exception) {
        emptyMap()
    }

    actual fun save(data: Map<String, Map<String, Boolean>>) {
        file.writeText(Json.encodeToString(data))
    }
}
