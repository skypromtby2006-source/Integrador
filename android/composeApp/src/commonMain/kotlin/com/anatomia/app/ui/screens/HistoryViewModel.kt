package com.anatomia.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anatomia.app.db.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    object Empty   : HistoryUiState()
    data class Ready(val groups: List<HistoryGroup>) : HistoryUiState()
}

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            val groups = buildHistoryGroups()
            _uiState.value = if (groups.isEmpty()) HistoryUiState.Empty
                             else HistoryUiState.Ready(groups)
        }
    }

    private fun buildHistoryGroups(): List<HistoryGroup> {
        val allRows = ProgressRepository.getAllEntries()
        if (allRows.isEmpty()) return emptyList()

        data class SessionKey(val organId: String, val dateLabel: String)

        val sessions = allRows.groupBy { row ->
            val dateMs = parseAnsweredAt(row.answered_at)
            val label  = formatDateLabel(dateMs)
            SessionKey(organId = row.organ_id, dateLabel = label)
        }

        // Build (entry, maxTimestamp) pairs for sorting
        val entriesWithTs = sessions.entries
            .map { (key, rows) ->
                val maxTs     = rows.maxOf { parseAnsweredAt(it.answered_at) }
                val correct   = rows.count { it.was_correct == 1L }
                val total     = rows.size
                val xp        = correct * 20
                val scoreTier = when {
                    total == 0                      -> ScoreTier.GOOD
                    correct * 100 / total >= 70     -> ScoreTier.GOOD
                    correct * 100 / total >= 50     -> ScoreTier.MID
                    else                            -> ScoreTier.WARN
                }
                val entry = HistoryEntry(
                    id        = "${key.organId}_${key.dateLabel}",
                    type      = EntryType.QUIZ,
                    title     = "Quiz · ${organLabel(key.organId)}",
                    topic     = organLabel(key.organId),
                    meta      = "${key.organId.replaceFirstChar { it.uppercase() }} · $total preguntas",
                    time      = formatTime(maxTs),
                    xp        = xp,
                    score     = if (total > 0) "$correct/$total" else "",
                    scoreTier = scoreTier,
                    summary   = buildSummary(correct, total, key.organId),
                )
                Pair(entry, maxTs)
            }
            .sortedByDescending { (_, ts) -> ts }

        return entriesWithTs
            .groupBy { (_, ts) -> getDateGroup(ts) }
            .entries
            .sortedBy { (group, _) -> groupOrder(group) }
            .map { (groupLabel, pairs) ->
                val groupEntries = pairs.map { it.first }
                HistoryGroup(
                    label   = groupLabel,
                    count   = groupEntries.size,
                    entries = groupEntries,
                )
            }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun parseAnsweredAt(iso: String): Long {
        return try {
            Instant.parse(iso).toEpochMilliseconds()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun organLabel(organId: String): String = when (organId) {
        "heart"   -> "Corazón"
        "lungs"   -> "Pulmones"
        "kidneys" -> "Riñones"
        else      -> organId.replaceFirstChar { it.uppercase() }
    }

    private fun buildSummary(correct: Int, total: Int, organId: String): String {
        if (total == 0) return "Sin respuestas registradas."
        val pct = correct * 100 / total
        return when {
            pct >= 80 -> "Excelente sesión en ${organLabel(organId)}. ¡Sigue así!"
            pct >= 50 -> "Buen progreso. Repasa las respuestas incorrectas."
            else      -> "A mejorar. El agente preparará un repaso enfocado."
        }
    }

    private fun formatDateLabel(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ms))

    private fun formatTime(ms: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))

    private fun getDateGroup(ms: Long): String {
        val diffDays = ((System.currentTimeMillis() - ms) / (1000L * 60 * 60 * 24)).toInt()
        val sdf = SimpleDateFormat("EEE d MMM", Locale("es"))
        return when {
            diffDays == 0 -> "Hoy · ${sdf.format(Date(ms))}"
            diffDays == 1 -> "Ayer · ${sdf.format(Date(ms))}"
            diffDays < 7  -> "Esta semana"
            diffDays < 14 -> "Semana anterior"
            else          -> SimpleDateFormat("MMMM yyyy", Locale("es")).format(Date(ms))
                .replaceFirstChar { it.uppercase() }
        }
    }

    private fun groupOrder(label: String): Int = when {
        label.startsWith("Hoy")  -> 0
        label.startsWith("Ayer") -> 1
        label == "Esta semana"   -> 2
        label == "Semana anterior" -> 3
        else                     -> 4
    }
}
