package com.anatomia.app.network

expect object ServerConfig {
    fun init(context: Any?)
    fun getBaseUrl(): String
    fun setBaseUrl(url: String)
    fun resetToDefault()
    val defaultUrl: String
}
