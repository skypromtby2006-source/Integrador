package com.anatomia.app.network

import java.util.prefs.Preferences

actual object ServerConfig {
    private val prefs = Preferences.userRoot().node("anatomia_server_config")
    actual val defaultUrl = "https://stable-jailbreak-squire.ngrok-free.dev"

    actual fun init(context: Any?) = Unit

    actual fun getBaseUrl(): String = prefs.get("base_url", defaultUrl)

    actual fun setBaseUrl(url: String) {
        prefs.put("base_url", url.trimEnd('/'))
    }

    actual fun resetToDefault() {
        prefs.remove("base_url")
    }
}
