package com.anatomia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.anatomia.app.ui.theme.LocalSuccessColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    var showPhotoMenu by remember { mutableStateOf(false) }
    var nombre       by remember { mutableStateOf("Ana") }
    var apellido     by remember { mutableStateOf("Rivera") }
    var displayName  by remember { mutableStateOf("Ana R.") }
    var birthDate    by remember { mutableStateOf("14/03/2009") }
    var email        by remember { mutableStateOf("ana.rivera@escuela.mx") }
    var touchId      by remember { mutableStateOf(true) }
    var classCode    by remember { mutableStateOf("BIO-3B-2026") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
                    }
                },
                actions = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Guardar", style = MaterialTheme.typography.labelLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Cancelar") }
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(2f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar cambios", style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { AvatarBlock(onEditClick = { showPhotoMenu = true }) }
            item {
                DatosPersonalesSection(
                    nombre = nombre, apellido = apellido,
                    displayName = displayName, birthDate = birthDate,
                    onNombreChange = { nombre = it }, onApellidoChange = { apellido = it },
                    onDisplayNameChange = { displayName = it }, onBirthDateChange = { birthDate = it },
                )
            }
            item {
                CuentaSection(
                    email = email, touchId = touchId,
                    onEmailChange = { email = it }, onTouchIdChange = { touchId = it },
                )
            }
            item {
                ClaseAsignadaSection(classCode = classCode, onCodeChange = { classCode = it })
            }
            item { AgentHintCard() }
            item { DangerZone() }
        }
    }

    if (showPhotoMenu) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoMenu = false },
            sheetState = rememberModalBottomSheetState(),
        ) {
            PhotoActionMenuContent(onDismiss = { showPhotoMenu = false })
        }
    }
}

@Composable
private fun AvatarBlock(onEditClick: () -> Unit) {
    val successColors = LocalSuccessColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Stacked circles for double-border effect
            Box(
                modifier = Modifier.size(122.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(117.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                                    start = androidx.compose.ui.geometry.Offset(0f, 112f),
                                    end = androidx.compose.ui.geometry.Offset(112f, 0f),
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "A",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium, fontSize = 44.sp),
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    }
                }
            }
            // Camera FAB
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Rounded.PhotoCamera, contentDescription = "Cambiar foto", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ana Rivera", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(successColors.successContainer)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(Icons.Rounded.School, contentDescription = null, tint = successColors.onSuccessContainer, modifier = Modifier.size(14.dp))
                Text("Estudiante · 3°B", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp), color = successColors.onSuccessContainer)
            }
        }
    }
}

@Composable
private fun PhotoActionMenuContent(onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        ListItem(
            headlineContent = { Text("Tomar foto") },
            leadingContent = { Icon(Icons.Rounded.PhotoCamera, contentDescription = null) },
            modifier = androidx.compose.ui.Modifier.clickable { onDismiss() },
        )
        ListItem(
            headlineContent = { Text("Elegir de galería") },
            leadingContent = { Icon(Icons.Rounded.Image, contentDescription = null) },
            modifier = androidx.compose.ui.Modifier.clickable { onDismiss() },
        )
        ListItem(
            headlineContent = { Text("Elegir avatar") },
            leadingContent = { Icon(Icons.Rounded.Mood, contentDescription = null) },
            modifier = androidx.compose.ui.Modifier.clickable { onDismiss() },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        ListItem(
            headlineContent = { Text("Quitar foto", color = MaterialTheme.colorScheme.error) },
            leadingContent = { Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            modifier = androidx.compose.ui.Modifier.clickable { onDismiss() },
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun DatosPersonalesSection(
    nombre: String, apellido: String, displayName: String, birthDate: String,
    onNombreChange: (String) -> Unit, onApellidoChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit, onBirthDateChange: (String) -> Unit,
) {
    SectionCard(title = "DATOS PERSONALES") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = nombre, onValueChange = onNombreChange,
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = apellido, onValueChange = onApellidoChange,
                label = { Text("Apellido") },
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = displayName, onValueChange = onDisplayNameChange,
            label = { Text("Nombre que se muestra") },
            supportingText = { Text("lo que ven tus compañeros") },
            leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
            shape = RoundedCornerShape(4.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = birthDate, onValueChange = onBirthDateChange,
            label = { Text("Fecha de nacimiento") },
            leadingIcon = { Icon(Icons.Rounded.Cake, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Elegir fecha")
                }
            },
            shape = RoundedCornerShape(4.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CuentaSection(email: String, touchId: Boolean, onEmailChange: (String) -> Unit, onTouchIdChange: (Boolean) -> Unit) {
    val successColors = LocalSuccessColors.current
    SectionCard(title = "CUENTA") {
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            label = { Text("Correo escolar") },
            supportingText = { Text("lo verifica tu maestra") },
            leadingIcon = { Icon(Icons.Rounded.Mail, contentDescription = null) },
            trailingIcon = { Icon(Icons.Rounded.Verified, contentDescription = null, tint = successColors.success) },
            shape = RoundedCornerShape(4.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ProfileInlineRow(
            iconBg = MaterialTheme.colorScheme.primaryContainer,
            iconFg = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Rounded.Lock,
            title = "Contraseña",
            subtitle = "cambiada hace 23 días",
        ) {
            TextButton(onClick = {}) {
                Text("Cambiar")
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        ProfileInlineRow(
            iconBg = MaterialTheme.colorScheme.tertiaryContainer,
            iconFg = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Rounded.Fingerprint,
            title = "Iniciar con Touch ID",
            subtitle = "acceso rápido en este dispositivo",
        ) {
            Switch(checked = touchId, onCheckedChange = onTouchIdChange)
        }
    }
}

@Composable
private fun ClaseAsignadaSection(classCode: String, onCodeChange: (String) -> Unit) {
    SectionCard(title = "CLASE ASIGNADA") {
        // Class pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp, 12.dp, 4.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary),
                            start = androidx.compose.ui.geometry.Offset(0f, 40f),
                            end = androidx.compose.ui.geometry.Offset(40f, 0f),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("MP", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onTertiary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Biología · 3°B", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("Prof. Marta Peña · 28 estudiantes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Opciones", tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        // Class code row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = classCode, onValueChange = onCodeChange,
                label = { Text("Código de clase") },
                leadingIcon = { Icon(Icons.Rounded.Tag, contentDescription = null) },
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = {},
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(4.dp),
            ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Escanear")
            }
        }
        // School row
        ProfileInlineRow(
            iconBg = MaterialTheme.colorScheme.primaryContainer,
            iconFg = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Rounded.School,
            title = "Escuela",
            subtitle = "Secundaria 14 · Cuauhtémoc",
        ) {
            TextButton(onClick = {}) {
                Text("Cambiar")
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AgentHintCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(12.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(16.dp))
        }
        Text(
            "Tip del agente · agrega tu nombre real para que tus reportes se vinculen correctamente con tu clase.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DangerZone() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = {}) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Eliminar mi cuenta", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ProfileInlineRow(
    iconBg: Color,
    iconFg: Color,
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}
