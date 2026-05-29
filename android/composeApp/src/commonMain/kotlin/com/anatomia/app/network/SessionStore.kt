package com.anatomia.app.network

// Interfaz multiplatform para guardar la sesión del alumno localmente.
// Mismo patrón expect/actual que ProgressStore.kt existente.
expect object SessionStore {
    fun init(context: Any?)
    fun save(student: StudentData)
    fun load(): StudentData?
    fun clear()
    fun isLoggedIn(): Boolean
}
