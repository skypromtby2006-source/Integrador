package com.anatomia.app.network

import android.content.Context
import android.content.SharedPreferences

actual object ServerConfig {
    private const val PREFS_NAME = "server_config"
    private const val KEY_BASE_URL = "base_url"
    actual val defaultUrl = "https://stable-jailbreak-squire.ngrok-free.dev"

    private var prefs: SharedPreferences? = null

    actual fun init(context: Any?) {
        prefs = (context as Context)
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun getBaseUrl(): String {
        return prefs?.getString(KEY_BASE_URL, defaultUrl) ?: defaultUrl
    }

    actual fun setBaseUrl(url: String) {
        prefs?.edit()?.putString(KEY_BASE_URL, url.trimEnd('/'))?.apply()
    }

    actual fun resetToDefault() {
        prefs?.edit()?.remove(KEY_BASE_URL)?.apply()
    }
}
