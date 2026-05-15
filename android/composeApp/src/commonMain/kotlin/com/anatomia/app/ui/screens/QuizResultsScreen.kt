package com.anatomia.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavHostController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class QuestionResult { OK, BAD, SKIP }

private data class ReviewItem(
    val number: Int,
    val questionText: String,
    val yourAnswer: String,
    val correctAnswer: String,
    val result: QuestionResult,
)

private val reviewItems = listOf(
    ReviewItem(1, "¿Cuántas cámaras tiene el corazón?", "4", "4", QuestionResult.OK),
    ReviewItem(2, "La sangre sale del corazón hacia el cuerpo por…", "aorta", "aorta", QuestionResult.OK),
    ReviewItem(3, "¿Qué cámara bombea sangre oxigenada al cuerpo?", "ventrículo izquierdo", "ventrículo izquierdo", QuestionResult.OK),
    ReviewItem(4, "¿Qué llevan las arterias pulmonares?", "oxígeno", "CO₂", QuestionResult.BAD),
    ReviewItem(5, "Función principal de las válvulas cardíacas:", "—", "evitar retroceso", QuestionResult.SKIP),
    ReviewItem(6, "El pulso que sientes refleja…", "contracción del ventrículo", "contracción del ventrículo", QuestionResult.OK),
    ReviewItem(7, "Las venas regresan la sangre a…", "aurícula derecha", "aurícula derecha", QuestionResult.OK),
    ReviewItem(8, "¿Cuántos circuitos tiene la circulación?", "2 (mayor y menor)", "2 (mayor y menor)", QuestionResult.OK),
)

@Composable
fun QuizResultsScreen(navController: NavHostController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ResultsTopBar(navController) },
        bottomBar = { ResultsActionDock(navController) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item { ScoreHeroCard() }
            item { StatsRow() }
            item { AgentInsightCard() }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text("Revisión por pregunta", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("toca para expandir", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column {
                        reviewItems.forEachIndexed { index, item ->
                            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ReviewRow(item)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultsTopBar(navController: NavHostController) {
    TopAppBar(
        title = { Text("Resultados", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.QuizResults.route) { inclusive = true }
                }
            }) {
                Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.Share, contentDescription = "Compartir")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun ScoreHeroCard() {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 0.75f,
            animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF5A3D7A)),
                    start = Offset(0f, 200f),
                    end = Offset(400f, 0f),
                )
            )
            .padding(22.dp, 22.dp, 18.dp, 18.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Score ring
            Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    // Track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth),
                    )
                    // Fill arc
                    val circumference = 2 * PI.toFloat() * radius
                    val dashOffset = circumference * (1 - animatedProgress.value)
                    drawArc(
                        color = Mint,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress.value,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("6/8", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium, fontSize = 36.sp), color = MaterialTheme.colorScheme.onPrimary)
                    Text("75% acierto", style = MaterialTheme.typography.labelSmall, color = Mint)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("¡BIEN HECHO, ANA!", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp), color = Mint)
                Text("Buen avance.", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(top = 4.dp))
                Text("Circulatorio · Cámaras del corazón", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f), modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetaPill(Icons.Rounded.Schedule, "4:32", Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.22f), MaterialTheme.colorScheme.onPrimary)
                    MetaPill(Icons.Rounded.Bolt, "+120 XP", Mint.copy(alpha = 0.22f), Mint.copy(alpha = 0.45f), Mint)
                }
            }
        }
    }
}

@Composable
private fun MetaPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, borderColor: Color, fg: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

@Composable
private fun StatsRow() {
    val successColors = LocalSuccessColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatCard("6", "correctas", Icons.Rounded.Check, successColors.successContainer, successColors.onSuccessContainer, modifier = Modifier.weight(1f))
        StatCard("1", "incorrecta", Icons.Rounded.Close, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
        StatCard("1", "saltada", Icons.Rounded.Redo, MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(num: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, iconFg: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier.padding(12.dp, 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(18.dp))
            }
            Text(num, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, fontSize = 22.sp), color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AgentInsightCard() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("INSIGHT · AGENTE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp), color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f))
                Text("Dominaste las cámaras 🎯", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                Text(
                    "Pero mezclas arterias y venas en preguntas con dirección de flujo. Te sugiero un mini-reto visual de 5 min.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.90f),
                )
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(markBg), contentAlignment = Alignment.Center) {
            Icon(markIcon, contentDescription = null, tint = markFg, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${item.number} · ${item.questionText}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
private fun ResultsActionDock(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = { navController.navigate(Screen.Agent.route) },
            modifier = Modifier.height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Repasar errores", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
        }
        Button(
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.QuizResults.route) { inclusive = true }
                }
            },
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Volver al inicio", style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
