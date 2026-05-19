package com.anatomia.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.*
import kotlin.math.roundToInt

private enum class QuestionResult { OK, BAD, SKIP }

private data class ReviewItem(
    val number       : Int,
    val questionText : String,
    val yourAnswer   : String,
    val correctAnswer: String,
    val result       : QuestionResult,
)

@Composable
fun QuizResultsScreen(
    navController: NavHostController,
    viewModel    : QuizViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val state = uiState as? QuizUiState.Finished ?: run {
        LaunchedEffect(Unit) { navController.navigate(Screen.Home.route) }
        return
    }

    val incorrectas = state.answers.entries.count { (idx, selected) ->
        selected != null && selected != state.questions[idx].correctIndex
    }
    val saltadas = state.answers.values.count { it == null }
    val xpGanada = state.score * 20
    val pctInt   = (state.pct * 100).roundToInt()

    val items = state.questions.mapIndexed { idx, question ->
        val selected  = state.answers[idx]
        val isCorrect = selected != null && selected == question.correctIndex
        val isSkipped = selected == null
        ReviewItem(
            number        = idx + 1,
            questionText  = question.stem,
            yourAnswer    = selected?.let { question.options[it] } ?: "—",
            correctAnswer = question.options[question.correctIndex],
            result        = when {
                isSkipped -> QuestionResult.SKIP
                isCorrect -> QuestionResult.OK
                else      -> QuestionResult.BAD
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar         = { ResultsTopBar(navController) },
        bottomBar      = {
            ResultsActionDock(
                onRetry    = {
                    viewModel.restart()
                    navController.popBackStack()
                },
                onContinue = {
                    navController.navigate(Screen.Agent.route) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding  = PaddingValues(bottom = 16.dp),
        ) {
            item { ScoreHeroCard(score = state.score, total = state.total, pct = state.pct, xp = xpGanada) }
            item { StatsRow(correctas = state.score, incorrectas = incorrectas, saltadas = saltadas) }
            item { AgentInsightCard(score = state.score, total = state.total) }
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom,
                ) {
                    Text("Revisión por pregunta", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("${items.size} preguntas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape    = RoundedCornerShape(16.dp),
                    color    = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column {
                        items.forEachIndexed { index, item ->
                            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ReviewRow(item)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultsTopBar(navController: NavHostController) {
    TopAppBar(
        title           = { Text("Resultados", style = MaterialTheme.typography.titleLarge) },
        navigationIcon  = {
            IconButton(onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.QuizResults.route) { inclusive = true }
                }
            }) { Icon(Icons.Rounded.Close, contentDescription = "Cerrar") }
        },
        actions         = { IconButton(onClick = {}) { Icon(Icons.Rounded.Share, contentDescription = "Compartir") } },
        colors          = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun ScoreHeroCard(score: Int, total: Int, pct: Float, xp: Int) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(pct) {
        animatedProgress.animateTo(pct, animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic))
    }
    val pctInt = (pct * 100).roundToInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF5A3D7A)), start = Offset(0f, 200f), end = Offset(400f, 0f)))
            .padding(22.dp, 22.dp, 18.dp, 18.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val radius      = (size.minDimension - strokeWidth) / 2
                    val center      = Offset(size.width / 2, size.height / 2)
                    drawCircle(color = Color.White.copy(alpha = 0.14f), radius = radius, center = center, style = Stroke(width = strokeWidth))
                    drawArc(
                        color       = Mint,
                        startAngle  = -90f,
                        sweepAngle  = 360f * animatedProgress.value,
                        useCenter   = false,
                        topLeft     = Offset(center.x - radius, center.y - radius),
                        size        = Size(radius * 2, radius * 2),
                        style       = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score/$total", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium, fontSize = 36.sp), color = MaterialTheme.colorScheme.onPrimary)
                    Text("$pctInt% acierto", style = MaterialTheme.typography.labelSmall, color = Mint)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                val headline = if (pctInt >= 70) "¡BIEN HECHO!" else "¡SIGUE ADELANTE!"
                val message  = if (pctInt >= 70) "Buen avance." else "Sigues mejorando."
                Text(headline, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp), color = Mint)
                Text(message, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(top = 4.dp))
                Text("Quiz completado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f), modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetaPill(Icons.Rounded.Bolt, "+$xp XP", Mint.copy(alpha = 0.22f), Mint.copy(alpha = 0.45f), Mint)
                }
            }
        }
    }
}

@Composable
private fun MetaPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, borderColor: Color, fg: Color) {
    Row(
        modifier              = Modifier.clip(CircleShape).background(bg).border(1.dp, borderColor, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

@Composable
private fun StatsRow(correctas: Int, incorrectas: Int, saltadas: Int) {
    val successColors = LocalSuccessColors.current
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatCard("$correctas",  "correctas",  Icons.Rounded.Check, successColors.successContainer,                    successColors.onSuccessContainer,                    modifier = Modifier.weight(1f))
        StatCard("$incorrectas","incorrecta", Icons.Rounded.Close, MaterialTheme.colorScheme.errorContainer,           MaterialTheme.colorScheme.onErrorContainer,           modifier = Modifier.weight(1f))
        StatCard("$saltadas",   "saltada",    Icons.Rounded.Redo,  MaterialTheme.colorScheme.surfaceContainerHighest,  MaterialTheme.colorScheme.onSurfaceVariant,           modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(num: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, iconFg: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(18.dp))
            }
            Text(num, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, fontSize = 22.sp), color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AgentInsightCard(score: Int, total: Int) {
    val pct     = if (total > 0) score * 100 / total else 0
    val insight = when {
        pct >= 85 -> "Excelente dominio del tema. Puedes avanzar al siguiente órgano."
        pct >= 70 -> "Buen avance. Repasa los temas donde fallaste para consolidar."
        pct >= 50 -> "Vas por buen camino. Te sugiero repasar los errores antes de continuar."
        else      -> "Necesitas más práctica en este tema. El agente te preparará un plan."
    }
    Surface(
        modifier       = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape          = RoundedCornerShape(16.dp),
        color          = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 1.dp,
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiary), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("INSIGHT · AGENTE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp), color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f))
                Text(insight, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.90f), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun ReviewRow(item: ReviewItem) {
    val successColors = LocalSuccessColors.current
    val (markBg, markFg, markIcon) = when (item.result) {
        QuestionResult.OK   -> Triple(successColors.success, successColors.onSuccess, Icons.Rounded.Check)
        QuestionResult.BAD  -> Triple(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError, Icons.Rounded.Close)
        QuestionResult.SKIP -> Triple(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Rounded.Redo)
    }
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(markBg), contentAlignment = Alignment.Center) {
            Icon(markIcon, contentDescription = null, tint = markFg, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${item.number} · ${item.questionText}",
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            val metaText = when (item.result) {
                QuestionResult.OK   -> "Tu respuesta: ${item.yourAnswer}"
                QuestionResult.BAD  -> "Tu respuesta: ${item.yourAnswer} · correcto: ${item.correctAnswer}"
                QuestionResult.SKIP -> "Saltaste · correcto: ${item.correctAnswer}"
            }
            Text(metaText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ResultsActionDock(onRetry: () -> Unit, onContinue: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onRetry, modifier = Modifier.height(52.dp), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Repasar errores", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
        }
        Button(onClick = onContinue, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) {
            Text("Continuar", style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
