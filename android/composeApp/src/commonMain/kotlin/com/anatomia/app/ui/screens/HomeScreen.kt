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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.anatomia.app.data.PlanRepository
import com.anatomia.app.data.model.DailyPlan
import com.anatomia.app.data.model.PlanTask
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.*

data class AppNotification(
    val id: Int,
    val icon: ImageVector,
    val title: String,
    val body: String,
    val time: String,
    val isUnread: Boolean
)

private val sampleNotifications = listOf(
    AppNotification(1, Icons.Rounded.AutoAwesome,
        "Agente", "Tienes una nueva sugerencia de repaso", "hace 5 min", true),
    AppNotification(2, Icons.Rounded.EmojiEvents,
        "Logro", "Completaste 3 días consecutivos 🔥", "hace 1 h", true),
    AppNotification(3, Icons.Rounded.Quiz,
        "Recordatorio", "Quiz de cámaras del corazón pendiente", "ayer", false)
)

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val plan = remember { PlanRepository.loadFromContext(context) }

    var tasks by remember { mutableStateOf(plan.tasks) }
    var taskToUncheck by remember { mutableStateOf<PlanTask?>(null) }
    var showNotifications by remember { mutableStateOf(false) }

    val completedCount = tasks.count { it.completed }
    val totalMin = tasks.sumOf { it.durationMin }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { HomeNavBar(navController) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                GreetingBar(
                    showNotifications = showNotifications,
                    onNotificationClick = { showNotifications = true },
                    onDismissNotifications = { showNotifications = false }
                )
            }
            item { HeroCard(navController, plan) }
            item {
                PlanSection(
                    tasks = tasks,
                    completedCount = completedCount,
                    totalMin = totalMin,
                    onTaskChecked = { index, task, isChecked ->
                        if (isChecked) {
                            tasks = tasks.toMutableList()
                                .also { it[index] = task.copy(completed = true) }
                        } else {
                            taskToUncheck = task
                        }
                    }
                )
            }
            item { AgentSuggestionCard(navController) }
        }
    }

    taskToUncheck?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToUncheck = null },
            icon = {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text("¿Desmarcar tarea?") },
            text = {
                Text("\"${task.title}\" quedará como pendiente. " +
                    "Puedes volver a marcarla cuando quieras.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val idx = tasks.indexOfFirst { it.id == task.id }
                    if (idx >= 0) {
                        tasks = tasks.toMutableList()
                            .also { it[idx] = task.copy(completed = false) }
                    }
                    taskToUncheck = null
                }) { Text("Desmarcar") }
            },
            dismissButton = {
                FilledTonalButton(onClick = { taskToUncheck = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun GreetingBar(
    showNotifications: Boolean,
    onNotificationClick: () -> Unit,
    onDismissNotifications: () -> Unit,
) {
    val successColors = LocalSuccessColors.current
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0f at 0
                14f at 240
                -8f at 480
                14f at 720
                -4f at 960
                10f at 1200
                0f at 1440
            }
        ),
        label = "waveRotation",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "JUEVES · 14 MAY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = MaterialTheme.colorScheme.primary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "buen día, Ana ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "👋",
                    fontSize = 22.sp,
                    modifier = Modifier.rotate(waveRotation),
                )
            }
        }

        Box {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(9.dp),
                    )
                }
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Rounded.Notifications, contentDescription = "Notificaciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (showNotifications) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = with(density) { IntOffset(x = 0, y = 56.dp.roundToPx()) },
                    onDismissRequest = onDismissNotifications,
                    properties = PopupProperties(focusable = true)
                ) {
                    NotificationPanel(
                        initialNotifications = sampleNotifications,
                        onDismiss = onDismissNotifications
                    )
                }
            }
        }

        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(3.5.dp, successColors.successContainer, CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                        start = androidx.compose.ui.geometry.Offset(0f, 40f),
                        end = androidx.compose.ui.geometry.Offset(40f, 0f),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("A", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondary)
        }
        Spacer(Modifier.width(12.dp))
    }
}

@Composable
private fun NotificationPanel(
    initialNotifications: List<AppNotification>,
    onDismiss: () -> Unit,
) {
    var items by remember { mutableStateOf(initialNotifications) }

    ElevatedCard(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { items = items.map { it.copy(isUnread = false) } }) {
                    Text("Marcar todo")
                }
            }
            HorizontalDivider()
            items.forEachIndexed { index, notif ->
                if (index > 0) HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (notif.isUnread)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        notif.icon,
                        contentDescription = null,
                        tint = if (notif.isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notif.title, style = MaterialTheme.typography.labelLarge)
                        Text(notif.body, style = MaterialTheme.typography.bodySmall)
                        Text(
                            notif.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(navController: NavHostController, plan: DailyPlan) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF5A3D7A)),
                    start = androidx.compose.ui.geometry.Offset(0f, 200f),
                    end = androidx.compose.ui.geometry.Offset(400f, 0f),
                )
            )
            .padding(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.30f))
                    .border(1.dp, Mint.copy(alpha = 0.28f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "3D",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = Mint.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                )
                Icon(Icons.Rounded.Favorite, contentDescription = null, tint = Mint, modifier = Modifier.size(40.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CONTINÚA CON",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = Mint,
                )
                Text(
                    plan.subject,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp,
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { plan.progressPct / 100f },
                        modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                        color = Mint,
                        trackColor = Color.White.copy(alpha = 0.18f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${plan.progressPct}%", style = MaterialTheme.typography.labelSmall, color = Mint)
                }
                Button(
                    onClick = { navController.navigate(Screen.Agent.route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Mint,
                        contentColor = Color(0xFF002417),
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Abrir", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun PlanSection(
    tasks: List<PlanTask>,
    completedCount: Int,
    totalMin: Int,
    onTaskChecked: (index: Int, task: PlanTask, checked: Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text("Tu plan de hoy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "$completedCount de ${tasks.size} · $totalMin min",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        Column {
            tasks.forEachIndexed { index, task ->
                if (index > 0) HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                PlanTaskRow(
                    task = task,
                    onChecked = { isChecked -> onTaskChecked(index, task, isChecked) }
                )
            }
        }
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun PlanTaskRow(task: PlanTask, onChecked: (Boolean) -> Unit) {
    val successColors = LocalSuccessColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = successColors.success,
                checkmarkColor = successColors.onSuccess,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null,
            )
            TagChip(task.type)
        }
        Text(
            "${task.durationMin} min",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun TagChip(type: String) {
    val successColors = LocalSuccessColors.current
    val (label, bg, fg) = when (type) {
        "lectura"       -> Triple("LECTURA",       MaterialTheme.colorScheme.secondaryContainer,  MaterialTheme.colorScheme.onSecondaryContainer)
        "video_3d"      -> Triple("VIDEO 3D",      MaterialTheme.colorScheme.tertiaryContainer,   MaterialTheme.colorScheme.onTertiaryContainer)
        "reto_creativo" -> Triple("RETO CREATIVO", MaterialTheme.colorScheme.primaryContainer,    MaterialTheme.colorScheme.onPrimaryContainer)
        "repaso"        -> Triple("REPASO",        successColors.successContainer,                successColors.onSuccessContainer)
        else            -> Triple(type.uppercase(), MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Text(
        label,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    )
}

@Composable
private fun AgentSuggestionCard(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "SUGERENCIA · AGENTE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f),
                )
                Text(
                    "¿saltamos al reto creativo? va bien con lo que aprendiste hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            TextButton(onClick = { navController.navigate(Screen.Agent.route) }) {
                Text("vamos", color = MaterialTheme.colorScheme.onTertiaryContainer)
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun HomeNavBar(navController: NavHostController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
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
            selected = false,
            onClick = { navController.navigate(Screen.History.route) },
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
