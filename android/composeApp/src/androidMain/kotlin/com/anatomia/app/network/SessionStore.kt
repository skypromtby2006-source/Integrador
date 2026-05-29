package com.anatomia.app.network

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val FILE = "session.json"

actual object SessionStore {
    private var ctx: Context? = null

    actual fun init(context: Any?) {
        ctx = context as Context
    }

    actual fun save(student: StudentData) {
        ctx?.openFileOutput(FILE, Context.MODE_PRIVATE)?.use {
            it.write(Json.encodeToString(student).toByteArray())
        }
    }

    actual fun load(): StudentData? = try {
        val text = ctx!!.openFileInput(FILE).bufferedReader().use { it.readText() }
        Json.decodeFromString(text)
    } catch (_: Exception) { null }

    actual fun clear() {
        ctx?.deleteFile(FILE)
    }

    actual fun isLoggedIn(): Boolean = load() != null
}
