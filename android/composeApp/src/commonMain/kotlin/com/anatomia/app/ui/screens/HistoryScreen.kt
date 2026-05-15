package com.anatomia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.LocalSuccessColors

// ── Data models ──────────────────────────────────────────────────────────────

enum class EntryType { QUIZ, AGENT, LESSON }
enum class ScoreTier { GOOD, MID, WARN }

data class HistoryEntry(
    val id: String,
    val type: EntryType,
    val title: String,
    val topic: String,
    val meta: String,
    val time: String,
    val xp: Int = 0,
    val score: String = "",
    val scoreTier: ScoreTier = ScoreTier.GOOD,
    val summary: String = "",
)

data class HistoryGroup(
    val label: String,
    val count: Int,
    val entries: List<HistoryEntry>,
)

private val historyGroups = listOf(
    HistoryGroup("Hoy · jue 14 may", 3, listOf(
        HistoryEntry("h1", EntryType.QUIZ, "Quiz · Cámaras del corazón",
            "Circulatorio", "5:32", "17:42", xp = 120, score = "6/8", scoreTier = ScoreTier.GOOD,
            summary = "Insight · aciertas en cámaras, mezclas arterias / venas."),
        HistoryEntry("h2", EntryType.AGENT, "Conversación · ¿Por qué late el corazón?",
            "Circulatorio", "12 turnos", "16:18",
            summary = "Resumen: comparaste el corazón con una bomba de doble cámara."),
        HistoryEntry("h3", EntryType.LESSON, "Lección · El viaje de la sangre",
            "Circulatorio", "5 min · completa", "15:47"),
    )),
    HistoryGroup("Ayer · mié 13 may", 2, listOf(
        HistoryEntry("h4", EntryType.AGENT, "Reflexión · ¿Cómo te sientes hoy?",
            "Auto-regulación", "🤓 enfocada", "18:05"),
        HistoryEntry("h5", EntryType.QUIZ, "Quiz · Flujo arterias / venas",
            "Circulatorio", "6:11", "17:30", xp = 60, score = "5/8", scoreTier = ScoreTier.MID,
            summary = "A mejorar · 3 errores en dirección de flujo."),
    )),
    HistoryGroup("Lun 11 may", 2, listOf(
        HistoryEntry("h6", EntryType.QUIZ, "Quiz · Funciones del corazón",
            "Circulatorio", "3:48 · perfecto", "18:22", xp = 160, score = "8/8", scoreTier = ScoreTier.GOOD,
            summary = "¡Récord! Dominas las funciones básicas. Subiste de nivel."),
        HistoryEntry("h7", EntryType.AGENT, "Plan semanal · Circulatorio",
            "Meta", "4 pasos", "17:10",
            summary = "Meta acordada: dominar el ciclo circulatorio en 7 días."),
    )),
)

private fun matchesFilter(entry: HistoryEntry, filter: String, query: String): Boolean {
    val matchType = when (filter) {
        "quiz"    -> entry.type == EntryType.QUIZ
        "agente"  -> entry.type == EntryType.AGENT
        "lección" -> entry.type == EntryType.LESSON
        else      -> true
    }
    val matchQuery = query.isBlank() ||
        entry.title.contains(query, ignoreCase = true) ||
        entry.topic.contains(query, ignoreCase = true)
    return matchType && matchQuery
}

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("todo") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HistoryTopBar(navController) },
        bottomBar = { HistoryNavBar(navController) },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { StatsStrip() }
            item { SearchRow(query = searchQuery, onQueryChange = { searchQuery = it }) }
            item { FilterChipsRow(active = activeFilter, onSelect = { activeFilter = it }) }
            historyGroups.forEach { group ->
                val filtered = group.entries.filter { matchesFilter(it, activeFilter, searchQuery) }
                if (filtered.isNotEmpty()) {
                    item { DateGroupHeader(label = group.label, count = group.count) }
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            Column {
                                filtered.forEachIndexed { index, entry ->
                                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    HistoryEntryRow(entry)
                                }
                            }
                        }
                    }
                }
            }
            item { EndNote() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTopBar(navController: NavHostController) {
    TopAppBar(
        title = { Text("Historial", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = "Calendario")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.Tune, contentDescription = "Filtros")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun StatsStrip() {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val mint = Color(0xFF59FFCC)
    val gradientEnd = lerp(primary, secondary, 0.45f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(primary, gradientEnd),
                    start = androidx.compose.ui.geometry.Offset(0f, 200f),
                    end = androidx.compose.ui.geometry.Offset(600f, 0f),
                )
            )
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCell(num = "24", unit = "", label = "sesiones", onPrimary = onPrimary, mint = mint, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.width(1.dp).height(52.dp).background(Color.White.copy(alpha = 0.18f)).align(Alignment.CenterVertically))
            StatCell(num = "12", unit = "quiz", label = "promedio 78%", onPrimary = onPrimary, mint = mint, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.width(1.dp).height(52.dp).background(Color.White.copy(alpha = 0.18f)).align(Alignment.CenterVertically))
            StatCell(num = "6", unit = "h", label = "esta semana", onPrimary = onPrimary, mint = mint, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(num: String, unit: String, label: String, onPrimary: Color, mint: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                num,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium, fontSize = 26.sp),
                color = onPrimary,
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(unit, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = mint)
            }
        }
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
            color = onPrimary.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun SearchRow(query: String, onQueryChange: (String) -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(
                Modifier.run {
                    // border via outline
                    this
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = onSurfaceVariant, modifier = Modifier.size(20.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text("Buscar en historial…", style = MaterialTheme.typography.bodyMedium, color = onSurfaceVariant)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = onSurface),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FilterChipsRow(active: String, onSelect: (String) -> Unit) {
    val filters = listOf("todo" to "Todo", "quiz" to "Quiz", "agente" to "Agente", "lección" to "Lección")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { (key, label) ->
            item {
                FilterChip(
                    selected = active == key,
                    onClick = { onSelect(key) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun DateGroupHeader(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            "$count actividades",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HistoryEntryRow(entry: HistoryEntry) {
    val successColors = LocalSuccessColors.current
    val (iconBg, iconFg, entryIcon) = when (entry.type) {
        EntryType.QUIZ   -> Triple(MaterialTheme.colorScheme.primaryContainer,   MaterialTheme.colorScheme.onPrimaryContainer,   Icons.Rounded.Quiz)
        EntryType.AGENT  -> Triple(MaterialTheme.colorScheme.tertiaryContainer,  MaterialTheme.colorScheme.onTertiaryContainer,  Icons.Rounded.Forum)
        EntryType.LESSON -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Icons.Rounded.MenuBook)
    }
    val (tagLabel, tagBg, tagFg) = when (entry.type) {
        EntryType.QUIZ   -> Triple("QUIZ",    MaterialTheme.colorScheme.primaryContainer,   MaterialTheme.colorScheme.onPrimaryContainer)
        EntryType.AGENT  -> Triple("AGENTE",  MaterialTheme.colorScheme.tertiaryContainer,  MaterialTheme.colorScheme.onTertiaryContainer)
        EntryType.LESSON -> Triple("LECCIÓN", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Column 1 — icon + badge
        Box(modifier = Modifier.width(44.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(entryIcon, contentDescription = null, tint = iconFg, modifier = Modifier.size(22.dp))
            }
            if (entry.score.isNotEmpty()) {
                val (badgeBg, badgeFg) = when (entry.scoreTier) {
                    ScoreTier.GOOD -> successColors.success to successColors.onSuccess
                    ScoreTier.MID  -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
                    ScoreTier.WARN -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(badgeBg)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        entry.score,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                        color = badgeFg,
                    )
                }
            }
        }

        // Column 2 — body
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.padding(top = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    tagLabel,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp, fontWeight = FontWeight.Medium),
                    color = tagFg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                Text(entry.topic, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                Text(entry.meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (entry.summary.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp),
                    )
                    Text(entry.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Column 3 — time + XP
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(top = 2.dp)) {
            Text(entry.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (entry.xp > 0) {
                Text(
                    "+${entry.xp} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun EndNote() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "— hace una semana —",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HistoryNavBar(navController: NavHostController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.History.route) { inclusive = true }
                }
            },
            icon = { Icon(Icons.Rounded.Today, contentDescription = "Hoy") },
            label = { Text("Hoy") },
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.ViewInAr, contentDescription = "Atlas 3D") },
            label = { Text("Atlas 3D") },
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Rounded.History, contentDescription = "Historial") },
            label = { Text("Historial") },
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.Person, contentDescription = "Yo") },
            label = { Text("Yo") },
        )
    }
}
