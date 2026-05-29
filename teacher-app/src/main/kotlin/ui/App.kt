package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.DocenteSession
import server.HttpServer

enum class Screen(val label: String, val icon: ImageVector) {
    Classes("Clases",           Icons.Rounded.School),
    Students("Alumnos",         Icons.Rounded.Group),
    Questions("Preguntas",      Icons.Rounded.Quiz),
    Evaluations("Evaluaciones", Icons.Rounded.Assignment),
    Progress("Progreso",        Icons.Rounded.BarChart),
}

@Composable
fun App() {
    DidactaiTheme {
        var session by remember { mutableStateOf<DocenteSession?>(null) }

        if (session == null) {
            LoginDocenteScreen(onLogin = { session = it })
        } else {
            MainShell(session = session!!, onLogout = { session = null })
        }
    }
}

// ── Login del docente ─────────────────────────────────────────────────────────
@Composable
fun LoginDocenteScreen(onLogin: (DocenteSession) -> Unit) {
    val scope   = rememberCoroutineScope()
    var ci      by remember { mutableStateOf("") }
    var pwd     by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(DColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape    = RoundedCornerShape(20.dp),
            color    = DColors.SurfaceContainer,
            modifier = Modifier.width(380.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                        .background(DColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Favorite, null,
                        tint = DColors.Mint, modifier = Modifier.size(28.dp))
                }

                Text("didactai", fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold, color = DColors.OnSurface)
                Text("Panel del Maestro", fontSize = 12.sp,
                    color = DColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp))

                DTextField(value = ci, onValueChange = { ci = it },
                    label = "Cédula de identidad",
                    modifier = Modifier.fillMaxWidth())

                DTextField(value = pwd, onValueChange = { pwd = it },
                    label = "Contraseña", isPassword = true,
                    modifier = Modifier.fillMaxWidth())

                error?.let {
                    Text(it, color = DColors.Error, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error   = null
                            val result = withContext(Dispatchers.IO) {
                                AuthRepository.loginDocente(ci.trim(), pwd)
                            }
                            if (result != null) onLogin(result)
                            else error = "CI o contraseña incorrectos"
                            loading = false
                        }
                    },
                    enabled  = ci.isNotBlank() && pwd.isNotBlank() && !loading,
                    colors   = ButtonDefaults.buttonColors(containerColor = DColors.Primary),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (loading) CircularProgressIndicator(
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = DColors.OnPrimary)
                    else Text("Entrar", fontSize = 15.sp)
                }
            }
        }
    }
}

// ── Shell principal (post-login) ──────────────────────────────────────────────
@Composable
fun MainShell(session: DocenteSession, onLogout: () -> Unit) {
    var currentScreen by remember { mutableStateOf(Screen.Classes) }

    Row(Modifier.fillMaxSize().background(DColors.Background)) {
        Sidebar(current = currentScreen, session = session,
            onSelect = { currentScreen = it }, onLogout = onLogout)
        Box(modifier = Modifier.fillMaxHeight().width(1.dp)
            .background(DColors.OutlineVariant))
        Box(Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Classes      -> ClassesScreen(docenteId = session.usuarioId)
                Screen.Students     -> StudentsScreen(docenteId = session.usuarioId)
                Screen.Questions    -> QuestionsScreen(docenteId = session.usuarioId)
                Screen.Evaluations  -> EvaluacionesScreen(docenteId = session.usuarioId)
                Screen.Progress     -> SessionProgressScreen(docenteId = session.usuarioId)
            }
        }
    }
}

// ── Sidebar ───────────────────────────────────────────────────────────────────
@Composable
private fun Sidebar(
    current:  Screen,
    session:  DocenteSession,
    onSelect: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.width(220.dp).fillMaxHeight()
            .background(DColors.Sidebar).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(DColors.Primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Favorite, null, tint = DColors.Mint,
                    modifier = Modifier.size(20.dp))
            }
            Column {
                Text("didactai", fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold, color = DColors.OnSurface)
                Text("panel maestro", fontSize = 10.sp,
                    color = DColors.OnSurfaceVariant, letterSpacing = 1.sp)
            }
        }

        Surface(shape = RoundedCornerShape(10.dp),
            color = DColors.SurfaceContainerHigh,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, null, tint = DColors.Primary,
                    modifier = Modifier.size(18.dp))
                Column {
                    Text("${session.nombre} ${session.apellido}",
                        fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        color = DColors.OnSurface)
                    Text("CI: ${session.usuarioId}", fontSize = 10.sp,
                        color = DColors.OnSurfaceVariant)
                }
            }
        }

        Text("MENÚ", fontSize = 10.sp, color = DColors.OnSurfaceVariant,
            letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))

        Screen.entries.forEach { screen ->
            SidebarItem(screen = screen, selected = current == screen,
                onClick = { onSelect(screen) })
        }

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Logout, null, tint = DColors.Error,
                modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Cerrar sesión", color = DColors.Error, fontSize = 13.sp)
        }

        ServerStatusBadge()
    }
}

@Composable
private fun SidebarItem(screen: Screen, selected: Boolean, onClick: () -> Unit) {
    val bgColor   = if (selected) DColors.SidebarActive else DColors.Sidebar
    val textColor = if (selected) DColors.Primary else DColors.OnSurfaceVariant

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        .clip(RoundedCornerShape(8.dp)).background(bgColor)
        .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(screen.icon, null, tint = textColor, modifier = Modifier.size(18.dp))
        Text(screen.label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun ServerStatusBadge() {
    Surface(shape = RoundedCornerShape(10.dp),
        color = DColors.SuccessContainer.copy(alpha = 0.25f),
        modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50))
                .background(DColors.Mint))
            Column {
                Text("Servidor activo", fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, color = DColors.OnSuccessContainer)
                Text("puerto ${HttpServer.PORT}", fontSize = 10.sp,
                    color = DColors.OnSurfaceVariant)
            }
        }
    }
}

// ── Componentes compartidos ───────────────────────────────────────────────────
@Composable
fun DTextField(value: String, onValueChange: (String) -> Unit, label: String,
               modifier: Modifier = Modifier, isPassword: Boolean = false, minLines: Int = 1) {
    val transformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = DColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(value = value, onValueChange = onValueChange,
            textStyle = TextStyle(color = DColors.OnSurface, fontSize = 14.sp),
            visualTransformation = transformation,
            cursorBrush = SolidColor(DColors.Primary), minLines = minLines,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(DColors.SurfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp))
    }
}

@Composable
fun Chip(label: String, color: androidx.compose.ui.graphics.Color,
         textColor: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = color) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
fun EmptyState(icon: String, title: String, message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(icon, fontSize = 40.sp)
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = DColors.OnSurface)
            Text(message, fontSize = 14.sp, color = DColors.OnSurfaceVariant)
        }
    }
}
