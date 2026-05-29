package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.db.SessionRepository
import com.anatomia.app.network.AuthService
import com.anatomia.app.network.StudentData
import com.anatomia.app.network.createHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val student: StudentData) : LoginState()
    data class Error(val message: String)        : LoginState()
}

class LoginViewModel : ViewModel() {

    private val authService = AuthService(createHttpClient())

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    init {
        val existing = SessionRepository.load()
        if (existing != null) {
            _state.value = LoginState.Success(existing)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState.Error("Ingresa correo y contraseña")
            return
        }

        viewModelScope.launch {
            _state.value = LoginState.Loading

            authService.login(email, password).fold(
                onSuccess = { data ->
                    val student = StudentData(
                        id        = data.usuarioId,
                        name      = "${data.nombre} ${data.apellido}",
                        email     = data.email,
                        className = data.claseNombre,
                        classCode = data.grado,
                        createdAt = ""
                    )
                    SessionRepository.save(student)
                    _state.value = LoginState.Success(student)
                },
                onFailure = { error ->
                    _state.value = LoginState.Error(error.message ?: "Error al iniciar sesión")
                }
            )
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}
