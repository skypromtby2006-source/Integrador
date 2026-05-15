package com.anatomia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.*
import kotlin.math.roundToInt

data class TextSizeLevel(
    val label: String,
    val titleSp: Int,
    val bodySp: Int
)

val textSizeLevels = listOf(
    TextSizeLevel("Pequeño",  13, 11),
    TextSizeLevel("Compacto", 15, 12),
    TextSizeLevel("Mediano",  17, 13),
    TextSizeLevel("Cómodo",   20, 15),
    TextSizeLevel("Grande",   24, 17)
)

@Composable
fun SettingsScreen(
    navController : NavHostController,
    appTheme      : AppTheme,
    fontSizeIndex : Int,
    onThemeChange : (AppTheme) -> Unit,
    onFontChange  : (Int) -> Unit,
) {
    val currentSize = textSizeLevels[fontSizeIndex]
    var notifications by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { SettingsTopBar(navController) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewCard(currentSize = currentSize)

            // Accesibilidad — slider de tamaño de texto
            SettingsSection(title = "ACCESIBILIDAD") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tamaño del texto", style = MaterialTheme.typography.titleSmall)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentSize.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Slider(
                        value = fontSizeIndex.toFloat(),
                        onValueChange = { onFontChange(it.roundToInt()) },
                        valueRange = 0f..4f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "a",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "A",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tema — selector de 3 tarjetas visuales
            SettingsSection(title = "TEMA") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ThemeOptionCard(
                            theme = AppTheme.LIGHT,
                            label = "claro",
                            isSelected = appTheme == AppTheme.LIGHT,
                            swatchContent = {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(14.dp)
                                            .align(Alignment.TopCenter)
                                            .background(Color(0xFFE0E0E0))
                                    )
                                }
                            },
                            onClick = { onThemeChange(AppTheme.LIGHT) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ThemeOptionCard(
                            theme = AppTheme.DARK,
                            label = "oscuro",
                            isSelected = appTheme == AppTheme.DARK,
                            swatchContent = {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C1B1F))) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(14.dp)
                                            .align(Alignment.TopCenter)
                                            .background(Color(0xFF2D2C31))
                                    )
                                }
                            },
                            onClick = { onThemeChange(AppTheme.DARK) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ThemeOptionCard(
                            theme = AppTheme.AUTO,
                            label = "automático",
                            isSelected = appTheme == AppTheme.AUTO,
                            swatchContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colorStops = arrayOf(
                                                    0.5f to Color(0xFFF5F5F5),
                                                    0.5f to Color(0xFF1C1B1F)
                                                )
                                            )
                                        )
                                )
                            },
                            onClick = { onThemeChange(AppTheme.AUTO) }
                        )
                    }
                }
            }

            // Notificaciones
            SettingsSection(title = "NOTIFICACIONES") {
                SettingsSwitchRow(
                    icon = Icons.Rounded.Notifications,
                    title = "Notificaciones",
                    subtitle = "Recordatorios de estudio diarios",
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                )
            }

            // Privacidad
            SettingsSection(title = "PRIVACIDAD") {
                SettingsChevronRow(
                    icon = Icons.Rounded.Lock,
                    title = "Privacidad y datos",
                    subtitle = "Gestiona tus datos personales",
                )
                HorizontalDivider(color = OutlineVariant)
                SettingsChevronRow(
                    icon = Icons.Rounded.Security,
                    title = "Seguridad",
                    subtitle = "Contraseña y autenticación",
                )
            }

            // Cuenta
            SettingsSection(title = "CUENTA") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.EditProfile.route) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, LocalSuccessColors.current.successContainer, CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Secondary, Primary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 48f),
                                    end = androidx.compose.ui.geometry.Offset(48f, 0f),
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("A", style = MaterialTheme.typography.titleMedium, color = OnSecondary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ana García", style = MaterialTheme.typography.titleSmall, color = OnSurface)
                        Text("ana@escuela.mx", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
                }
                HorizontalDivider(color = OutlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cerrar sesión", color = Error, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            Text(
                "Didactai v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    theme: AppTheme,
    label: String,
    isSelected: Boolean,
    swatchContent: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .shadow(
                    elevation = if (isSelected) 4.dp else 1.dp,
                    shape = MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.large
                ),
            contentAlignment = Alignment.Center
        ) {
            swatchContent()

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(navController: NavHostController) {
    TopAppBar(
        title = { Text("Ajustes", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
            }
        },
        actions = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Listo", color = Primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun PreviewCard(currentSize: TextSizeLevel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Primary, Color(0xFF5A3D7A)),
                    start = androidx.compose.ui.geometry.Offset(0f, 200f),
                    end = androidx.compose.ui.geometry.Offset(400f, 0f),
                )
            )
            .padding(16.dp, 16.dp, 16.dp, 16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("VISTA PREVIA", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp), color = Mint)
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Mint.copy(alpha = 0.22f))
                        .border(1.dp, Mint.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Mint))
                    Text("EN VIVO", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), color = Mint)
                }
            }
            Text(
                text = "El corazón es un músculo",
                fontSize = currentSize.titleSp.sp,
                fontWeight = FontWeight.Medium,
                color = OnPrimary,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
            Text(
                text = "bombea sangre por todo tu cuerpo, sin descanso, día y noche.",
                fontSize = currentSize.bodySp.sp,
                color = OnPrimary.copy(alpha = 0.75f)
            )
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip(label = "♥ corazón", bg = Tertiary.copy(alpha = 0.25f), border = Tertiary.copy(alpha = 0.5f), fg = OnPrimary)
                Chip(label = "↻ ciclo circulatorio", bg = Mint.copy(alpha = 0.20f), border = Mint.copy(alpha = 0.5f), fg = Mint)
            }
        }
    }
}

@Composable
private fun Chip(label: String, bg: Color, border: Color, fg: Color) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = Primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Surface(shape = RoundedCornerShape(16.dp), color = SurfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsSwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) },
        leadingContent = {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(20.dp))
            }
        },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SettingsChevronRow(icon: ImageVector, title: String, subtitle: String) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) },
        leadingContent = {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(20.dp))
            }
        },
        trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = OnSurfaceVariant) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
