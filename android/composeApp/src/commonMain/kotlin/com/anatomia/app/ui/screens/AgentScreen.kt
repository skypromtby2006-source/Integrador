package com.anatomia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.*

@Composable
fun DidactaiAgentScreen(navController: NavHostController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AgentTopBar(navController) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface))
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Button(
                    onClick = { navController.navigate(Screen.Quiz.route) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Comenzar Quiz", style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AgentHeroCard()
            BeliefsSection()
            GoalSection()
            PlanSection()
            ReflectionSection()
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentTopBar(navController: NavHostController) {
    val successColors = LocalSuccessColors.current
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Agente", style = MaterialTheme.typography.titleLarge)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(successColors.successContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(successColors.success))
                        Text("activo", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp), color = successColors.onSuccessContainer)
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.Tune, contentDescription = "Ajustes del agente")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun AgentHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.tertiary, Color(0xFF8A0070)),
                    start = androidx.compose.ui.geometry.Offset(0f, 200f),
                    end = androidx.compose.ui.geometry.Offset(400f, 0f),
                )
            )
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Mint.copy(alpha = 0.40f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Mint, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Buenos días, Ana", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiary)
                    Text("RESUMEN · SEMANA 6", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp), color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.75f))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Esta semana avanzaste un 40% en circulatorio. Vi que te concentras mejor por las tardes — vamos a aprovecharlo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, bdiLabel: String, iconBg: Color, iconTint: Color, icon: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides iconTint) { icon() }
        }
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(bdiLabel, style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BeliefsSection() {
    val successColors = LocalSuccessColors.current
    SectionHeader("Lo que noté en ti", "creencias", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer) {
        Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            BeliefRow(
                bulletColor = successColors.success,
                tagText = "↑ fuerte",
                tagBg = successColors.successContainer,
                tagFg = successColors.onSuccessContainer,
                title = "Recuerdas las funciones del corazón",
                hint = "acertaste 8 de 8 preguntas en tu último quiz",
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            BeliefRow(
                bulletColor = MaterialTheme.colorScheme.primary,
                tagText = "patrón",
                tagBg = MaterialTheme.colorScheme.primaryContainer,
                tagFg = MaterialTheme.colorScheme.onPrimaryContainer,
                title = "Aprendes mejor por las tardes",
                hint = "tu retención mejora un 23% entre 5–7 PM",
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            BeliefRow(
                bulletColor = MaterialTheme.colorScheme.tertiary,
                tagText = "a mejorar",
                tagBg = MaterialTheme.colorScheme.tertiaryContainer,
                tagFg = MaterialTheme.colorScheme.onTertiaryContainer,
                title = "Te cuesta el flujo en arterias vs venas",
                hint = "2 errores seguidos · te sugiero un mini-reto visual",
            )
        }
    }
}

@Composable
private fun BeliefRow(bulletColor: Color, tagText: String, tagBg: Color, tagFg: Color, title: String, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(bulletColor).padding(top = 9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text(
                    tagText,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = tagFg,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(tagBg).padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun GoalSection() {
    SectionHeader("Tu meta", "objetivo", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer) {
        Icon(Icons.Rounded.TrackChanges, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ESTA SEMANA", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            Text("Dominar el ciclo circulatorio completo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                GoalPill(Icons.Rounded.Schedule, "2 días restantes")
                GoalPill(Icons.Rounded.TrendingUp, "buen ritmo")
            }
            LinearProgressIndicator(
                progress = { 0.65f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("65% completado", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onPrimaryContainer)
                TextButton(onClick = {}) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ajustar meta", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun GoalPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f)).padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

private data class PlanStep(val num: Int, val title: String, val reason: String, val time: String, val state: StepState)
private enum class StepState { DONE, CURRENT, PENDING }

private val agentPlanSteps = listOf(
    PlanStep(1, "Revisar latido del corazón", "base para entender el ciclo", "3 min", StepState.DONE),
    PlanStep(2, "Reto: arterias vs venas", "te cuesta esta distinción — práctica visual", "8 min", StepState.CURRENT),
    PlanStep(3, "Diseñar una válvula", "creas conocimiento al diseñar", "10 min", StepState.PENDING),
    PlanStep(4, "Quiz de cierre", "consolida lo aprendido hoy", "4 min", StepState.PENDING),
)

@Composable
private fun PlanSection() {
    SectionHeader("Mi plan para ti", "próximos pasos", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer) {
        Icon(Icons.Rounded.Route, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            agentPlanSteps.forEachIndexed { index, step ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PlanStepRow(step)
            }
        }
    }
}

@Composable
private fun PlanStepRow(step: PlanStep) {
    val successColors = LocalSuccessColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val (circleBg, circleFg) = when (step.state) {
            StepState.DONE    -> successColors.success to successColors.onSuccess
            StepState.CURRENT -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
            StepState.PENDING -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(circleBg)
                .then(if (step.state == StepState.PENDING) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (step.state == StepState.DONE) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = circleFg, modifier = Modifier.size(16.dp))
            } else {
                Text(step.num.toString(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = circleFg)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                step.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (step.state == StepState.DONE) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (step.state == StepState.DONE) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                Text(step.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            step.time,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ReflectionSection() {
    val successColors = LocalSuccessColors.current
    SectionHeader("¿Cómo te sientes hoy?", "reflexión", successColors.successContainer, successColors.onSuccessContainer) {
        Icon(Icons.Rounded.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(8.dp))
    var selectedMood by remember { mutableStateOf(3) }
    val moods = listOf("😵‍💫", "😴", "🙂", "🤓", "🚀")

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = successColors.successContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Antes de seguir, cuéntame", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = successColors.onSuccessContainer)
            Text("esto me ayuda a ajustar el ritmo y los retos", style = MaterialTheme.typography.bodySmall, color = successColors.onSuccessContainer.copy(alpha = 0.8f), modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                moods.forEachIndexed { index, emoji ->
                    Surface(
                        onClick = { selectedMood = index },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        border = if (selectedMood == index)
                            androidx.compose.foundation.BorderStroke(2.dp, successColors.success)
                        else
                            androidx.compose.foundation.BorderStroke(1.dp, successColors.onSuccessContainer.copy(alpha = 0.18f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }
}
