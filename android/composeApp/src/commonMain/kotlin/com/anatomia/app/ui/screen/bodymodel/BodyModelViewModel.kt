package com.anatomia.app.ui.screen.bodymodel

import androidx.lifecycle.ViewModel
import com.anatomia.app.ui.model.AnatomySystem
import com.anatomia.app.ui.model.BodyModelUiState
import com.anatomia.app.ui.model.OrganCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BodyModelViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<BodyModelUiState>(BodyModelUiState.Idle)
    val uiState: StateFlow<BodyModelUiState> = _uiState.asStateFlow()

    private val _activeSystem = MutableStateFlow(AnatomySystem.CARDIOVASCULAR)
    val activeSystem: StateFlow<AnatomySystem> = _activeSystem.asStateFlow()

    fun onOrganSelectedFromUnity(organId: String) {
        println("[ANATOMIA] ViewModel recibió organId: $organId")
        val organ = OrganCatalog.findById(organId) ?: run {
            println("[ANATOMIA] Órgano NO encontrado para id: $organId")
            return
        }
        println("[ANATOMIA] Órgano encontrado: $organ")
        _uiState.value = BodyModelUiState.OrganFocused(
            organ = organ,
            activeSystem = _activeSystem.value,
        )
        println("[ANATOMIA] Nuevo estado: ${_uiState.value}")
    }

    fun onSheetDismissed() {
        _uiState.value = BodyModelUiState.Idle
    }

    fun onSystemSelected(system: AnatomySystem) {
        _activeSystem.value = system
    }
}
