package com.anatomia.app.agent

expect object MoodStore {
    fun init(context: Any?)
    fun save(mood: String)
    fun load(): String?
}
