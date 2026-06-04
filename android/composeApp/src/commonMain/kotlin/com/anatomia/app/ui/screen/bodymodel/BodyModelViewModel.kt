package com.anatomia.app.ui.screen.bodymodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.agent.AgentRepository
import com.anatomia.app.ui.model.AnatomySystem
import com.anatomia.app.ui.model.BodyModelUiState
import com.anatomia.app.ui.model.OrganCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BodyModelViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<BodyModelUiState>(BodyModelUiState.Idle)
    val uiState: StateFlow<BodyModelUiState> = _uiState.asStateFlow()

    private val _activeSystem = MutableStateFlow(AnatomySystem.CARDIOVASCULAR)
    val activeSystem: StateFlow<AnatomySystem> = _activeSystem.asStateFlow()

    private var poiStartTime: Long = 0L

    fun onOrganSelectedFromUnity(organId: String) {
        val organ = OrganCatalog.findById(organId) ?: return
        _uiState.value = BodyModelUiState.OrganFocused(
            organ        = organ,
            activeSystem = _activeSystem.value,
        )
    }

    fun onPoiSelected(poiName: String, organId: String) {
        val organ = OrganCatalog.findById(organId) ?: return
        val poi   = organ.pois.find { it.name == poiName } ?: run {
            _uiState.value = BodyModelUiState.OrganFocused(organ, _activeSystem.value)
            return
        }
        poiStartTime = System.currentTimeMillis()
        _uiState.value = BodyModelUiState.PoiFocused(
            organ        = organ,
            poi          = poi,
            activeSystem = _activeSystem.value,
        )
    }

    fun onPoiSheetDismissed() {
        val state = _uiState.value as? BodyModelUiState.PoiFocused
        if (state != null && poiStartTime > 0L) {
            val elapsed = System.currentTimeMillis() - poiStartTime
            recordPoiExploration(
                organId      = state.organ.id,
                poiTopic     = state.poi.topic,
                explorationMs = elapsed,
            )
        }
        poiStartTime = 0L
        _uiState.value = BodyModelUiState.Idle
    }

    fun onSheetDismissed() {
        _uiState.value = BodyModelUiState.Idle
    }

    fun onSystemSelected(system: AnatomySystem) {
        _activeSystem.value = system
    }

    private fun recordPoiExploration(organId: String, poiTopic: String, explorationMs: Long) {
        if (explorationMs < 3000L) return
        viewModelScope.launch(Dispatchers.IO) {
            val syntheticId = "explore_${organId}_${poiTopic.replace(" ", "_").lowercase()}"
            AgentRepository().recordAnswer(
                organId    = organId,
                questionId = syntheticId,
                wasCorrect = true,
                topic      = poiTopic,
            )
        }
    }
}
