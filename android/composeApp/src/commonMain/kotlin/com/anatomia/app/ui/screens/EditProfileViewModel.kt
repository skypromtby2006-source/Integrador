package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.db.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class EditProfileResult {
    object Idle    : EditProfileResult()
    object Loading : EditProfileResult()
    object Success : EditProfileResult()
    data class Error(val message: String) : EditProfileResult()
}

data class EditProfileUiState(
    val firstName  : String            = "",
    val lastName   : String            = "",
    val displayName: String            = "",
    val email      : String            = "",
    val birthDate  : String            = "",
    val classCode  : String            = "",
    val result     : EditProfileResult = EditProfileResult.Idle,
)

class EditProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadFromSession()
    }

    private fun loadFromSession() {
        val student = SessionRepository.load() ?: return
        val parts = student.name.split(" ", limit = 2)
        _uiState.update {
            it.copy(
                firstName   = parts.getOrElse(0) { "" },
                lastName    = parts.getOrElse(1) { "" },
                displayName = student.name,
                email       = student.email,
                classCode   = student.classCode,
            )
        }
    }

    fun onFirstNameChange(value: String)   { _uiState.update { it.copy(firstName   = value) } }
    fun onLastNameChange(value: String)    { _uiState.update { it.copy(lastName    = value) } }
    fun onDisplayNameChange(value: String) { _uiState.update { it.copy(displayName = value) } }
    fun onEmailChange(value: String)       { _uiState.update { it.copy(email       = value) } }
    fun onBirthDateChange(value: String)   { _uiState.update { it.copy(birthDate   = value) } }
    fun onClassCodeChange(value: String)   { _uiState.update { it.copy(classCode   = value.uppercase().trim()) } }

    fun save() {
        val state = _uiState.value
        if (state.firstName.isBlank()) {
            _uiState.update { it.copy(result = EditProfileResult.Error("El nombre no puede estar vacío")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(result = EditProfileResult.Loading) }
            try {
                val current = SessionRepository.load()
                if (current != null) {
                    val fullName = "${state.firstName.trim()} ${state.lastName.trim()}".trim()
                    val updated = current.copy(
                        name      = fullName,
                        email     = state.email,
                        classCode = state.classCode.ifBlank { current.classCode },
                    )
                    SessionRepository.save(updated)
                }
                _uiState.update { it.copy(result = EditProfileResult.Success) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(result = EditProfileResult.Error(e.message ?: "Error al guardar"))
                }
            }
        }
    }

    fun resetResult() {
        _uiState.update { it.copy(result = EditProfileResult.Idle) }
    }
}
