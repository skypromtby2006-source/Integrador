package ui

import ApiClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.CreateDocenteRequest
import models.DocenteListItem
import models.DocenteSession
import server.RequestLog
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.Duration
import java.time.Instant

// ── Colores del tema oscuro Didactai ─────────────────────────────────────────
private val BgDark      = Color(0xFF1C1B1F)
private val SurfaceDark = Color(0xFF2D2C31)
private val SurfaceHigh = Color(0xFF3D3C41)
private val Mint        = Color(0xFF59FFCC)
private val TextMuted   = Color(0xFF9E9E9E)
private val DividerClr  = Color(0xFF3A3A3D)

@Composable
fun PantallaDashboard(
    session:  DocenteSession,
    onLogout: () -> Unit,
    ngrokUrl: String = "http://localhost:8080"
) {
    val inicioApp = remember { Instant.now() }
    var tabIndex  by remember { mutableStateOf(0) }
    val tabs      = listOf("Servidor", "Docentes")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Encabezado ──────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🛡", fontSize = 24.sp)
                Column {
                    Text(
                        "Panel de Control",
                        fontWeight = FontWeight.Medium,
                        fontSize   = 20.sp,
                        color      = Color.White
                    )
                    Text(
                        "Hola, ${session.nombreCorto}",
                        fontSize = 13.sp,
                        color    = TextMuted
                    )
                }
            }
            OutlinedButton(
                onClick = onLogout,
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCF6679))
            ) {
                Text("Cerrar sesión")
            }
        }

        // ── Pestañas ────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor   = SurfaceDark,
            contentColor     = Mint,
            modifier         = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { i, titulo ->
                Tab(
                    selected = tabIndex == i,
                    onClick  = { tabIndex = i },
                    text     = {
                        Text(
                            titulo,
                            fontWeight = if (tabIndex == i) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // ── Contenido por pestaña ───────────────────────────────────────────
        when (tabIndex) {
            0 -> TabServidor(ngrokUrl = ngrokUrl, inicioApp = inicioApp)
            1 -> TabDocentes()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PESTAÑA 1 — SERVIDOR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TabServidor(ngrokUrl: String, inicioApp: Instant) {
    var uptime     by remember { mutableStateOf("00:00:00") }
    var logEntries by remember { mutableStateOf(RequestLog.obtenerTodos()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000)
            uptime     = formatUptime(Duration.between(inicioApp, Instant.now()))
            logEntries = RequestLog.obtenerTodos()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("SERVIDOR", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp, color = TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(8.dp).background(Mint, CircleShape))
                        Text("activo", fontSize = 12.sp, color = Mint)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("URL ngrok:", fontSize = 13.sp, color = TextMuted)
                    Text(ngrokUrl.ifEmpty { "No configurada" }, fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace, color = Mint, modifier = Modifier.weight(1f))
                    if (ngrokUrl.isNotEmpty()) {
                        TextButton(onClick = {
                            Toolkit.getDefaultToolkit().systemClipboard
                                .setContents(StringSelection(ngrokUrl), null)
                        }) { Text("Copiar", fontSize = 12.sp) }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LabelValor("Puerto", "8080")
                    LabelValor("Uptime", uptime, monospace = true)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("LOG DE REQUESTS", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp, color = TextMuted)
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("últimas ${logEntries.size}", fontSize = 12.sp, color = TextMuted)
                OutlinedButton(
                    onClick        = { RequestLog.limpiar(); logEntries = emptyList() },
                    modifier       = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) { Text("Limpiar", fontSize = 12.sp) }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape  = RoundedCornerShape(12.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().background(SurfaceHigh)
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HeaderCell("Hora",   90.dp)
                    HeaderCell("Método", 80.dp)
                    HeaderCell("Ruta",   null, weight = 1f)
                    HeaderCell("Status", 70.dp)
                }
                HorizontalDivider(color = Color(0xFF4D4C51))

                if (logEntries.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Sin requests aún.\nEspera a que la app Android se conecte.",
                            color = Color(0xFF666666), fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn {
                        items(logEntries) { entry ->
                            Row(modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                MonoCell(entry.hora,   90.dp, TextMuted)
                                MonoCell(entry.metodo, 80.dp, metodoColor(entry.metodo), bold = true)
                                MonoCell(entry.ruta,   null,  Color.White, weight = 1f)
                                MonoCell(entry.status.toString(), 70.dp, statusColor(entry.status), bold = true)
                            }
                            HorizontalDivider(color = DividerClr)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PESTAÑA 2 — DOCENTES
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TabDocentes() {
    val scope       = rememberCoroutineScope()
    var docentes    by remember { mutableStateOf<List<DocenteListItem>>(emptyList()) }
    var cargando    by remember { mutableStateOf(true) }
    var errorMsg    by remember { mutableStateOf("") }
    var mostrarForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        docentes = ApiClient.listarDocentes()
        cargando = false
    }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("DOCENTES REGISTRADOS", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp, color = TextMuted)
                Button(onClick = { mostrarForm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40798C))) {
                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar docente")
                }
            }

            Card(modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape  = RoundedCornerShape(12.dp)) {
                when {
                    cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Mint)
                    }
                    docentes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay docentes registrados.\nUsa el botón para agregar el primero.",
                            color = Color(0xFF666666), fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                    else -> LazyColumn {
                        item {
                            Row(modifier = Modifier.fillMaxWidth().background(SurfaceHigh)
                                .padding(horizontal = 16.dp, vertical = 8.dp)) {
                                HeaderCell("CI",              90.dp)
                                HeaderCell("Nombre completo", null, weight = 1f)
                                HeaderCell("Correo",          null, weight = 1f)
                                HeaderCell("2FA",             60.dp)
                                HeaderCell("Estado",          80.dp)
                            }
                            HorizontalDivider(color = Color(0xFF4D4C51))
                        }
                        items(docentes) { doc ->
                            Row(modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                MonoCell(doc.usuarioId,      90.dp, TextMuted)
                                TextCell(doc.nombreCompleto, null,  Color.White, weight = 1f)
                                TextCell(doc.email,          null,  TextMuted,   weight = 1f)
                                Box(modifier = Modifier.width(60.dp)) {
                                    Surface(shape = RoundedCornerShape(4.dp),
                                        color = if (doc.tiene2FA) Color(0xFF1B8A5A).copy(alpha = 0.3f)
                                                else Color(0xFF8A0000).copy(alpha = 0.3f)) {
                                        Text(if (doc.tiene2FA) "✓ ON" else "✗ OFF", fontSize = 11.sp,
                                            color = if (doc.tiene2FA) Mint else Color(0xFFFF6B6B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Box(modifier = Modifier.width(80.dp)) {
                                    Surface(shape = RoundedCornerShape(4.dp),
                                        color = if (doc.estado == "activo") Color(0xFF1B8A5A).copy(alpha = 0.2f)
                                                else Color(0xFF555555).copy(alpha = 0.4f)) {
                                        Text(doc.estado, fontSize = 11.sp,
                                            color = if (doc.estado == "activo") Mint else TextMuted,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            HorizontalDivider(color = DividerClr)
                        }
                    }
                }
            }
        }

        if (mostrarForm) {
            FormularioDocente(
                onGuardar = { req ->
                    scope.launch {
                        val result = ApiClient.crearDocente(req)
                        result.fold(
                            onSuccess = { nuevoDocente ->
                                docentes = docentes + DocenteListItem(
                                    usuarioId       = nuevoDocente.usuarioId,
                                    primerNombre    = nuevoDocente.primerNombre,
                                    segundoNombre   = nuevoDocente.segundoNombre,
                                    apellidoPaterno = nuevoDocente.apellidoPaterno,
                                    apellidoMaterno = nuevoDocente.apellidoMaterno,
                                    email           = nuevoDocente.email,
                                    especialidad    = nuevoDocente.especialidad,
                                    tituloAcademico = nuevoDocente.tituloAcademico,
                                    tiene2FA        = true
                                )
                                mostrarForm = false
                                errorMsg    = ""
                            },
                            onFailure = { e -> errorMsg = e.message ?: "Error al crear docente" }
                        )
                    }
                },
                errorExterno = errorMsg,
                onCancelar   = { mostrarForm = false; errorMsg = "" }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORMULARIO DE REGISTRO DE DOCENTE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FormularioDocente(
    onGuardar:    (CreateDocenteRequest) -> Unit,
    errorExterno: String,
    onCancelar:   () -> Unit
) {
    var ci              by remember { mutableStateOf("") }
    var primerNombre    by remember { mutableStateOf("") }
    var segundoNombre   by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var especialidad    by remember { mutableStateOf("") }
    var tituloAcademico by remember { mutableStateOf("") }

    var errCi              by remember { mutableStateOf("") }
    var errPrimerNombre    by remember { mutableStateOf("") }
    var errApellidoPaterno by remember { mutableStateOf("") }
    var errEmail           by remember { mutableStateOf("") }
    var errPassword        by remember { mutableStateOf("") }

    fun validar(): Boolean {
        var ok = true
        errCi = when {
            ci.isBlank()                    -> "Requerido"
            !ci.all { it.isDigit() }        -> "Solo dígitos"
            ci.length < 6 || ci.length > 10 -> "Entre 6 y 10 dígitos"
            else                             -> ""
        }
        if (errCi.isNotEmpty()) ok = false
        errPrimerNombre = when {
            primerNombre.isBlank()   -> "Requerido"
            primerNombre.length > 60 -> "Máximo 60 caracteres"
            !primerNombre.all { it.isLetter() || it.isWhitespace() } -> "Solo letras"
            else                     -> ""
        }
        if (errPrimerNombre.isNotEmpty()) ok = false
        errApellidoPaterno = when {
            apellidoPaterno.isBlank()   -> "Requerido"
            apellidoPaterno.length > 60 -> "Máximo 60 caracteres"
            !apellidoPaterno.all { it.isLetter() || it.isWhitespace() } -> "Solo letras"
            else                         -> ""
        }
        if (errApellidoPaterno.isNotEmpty()) ok = false
        errEmail = when {
            email.isBlank()                    -> "Requerido"
            !email.contains("@") || !email.contains(".") -> "Formato inválido"
            else                               -> ""
        }
        if (errEmail.isNotEmpty()) ok = false
        errPassword = when {
            password.isBlank()    -> "Requerido"
            password.length < 8   -> "Mínimo 8 caracteres"
            else                  -> ""
        }
        if (errPassword.isNotEmpty()) ok = false
        return ok
    }

    Card(modifier = Modifier.width(340.dp).fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape  = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Nuevo docente", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.White)
            HorizontalDivider(color = DividerClr)

            SectionLabel("IDENTIFICACIÓN")
            FormField(value = ci, onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 10) ci = it },
                label = "CI *", error = errCi, hint = "Solo dígitos, 6–10 caracteres")

            SectionLabel("NOMBRE")
            FormField(value = primerNombre, onValueChange = { primerNombre = it },
                label = "Primer nombre *", error = errPrimerNombre)
            FormField(value = segundoNombre, onValueChange = { segundoNombre = it },
                label = "Segundo nombre", error = "")

            SectionLabel("APELLIDOS")
            FormField(value = apellidoPaterno, onValueChange = { apellidoPaterno = it },
                label = "Apellido paterno *", error = errApellidoPaterno)
            FormField(value = apellidoMaterno, onValueChange = { apellidoMaterno = it },
                label = "Apellido materno", error = "")

            SectionLabel("CUENTA")
            FormField(value = email, onValueChange = { email = it },
                label = "Correo electrónico *", error = errEmail)
            FormField(value = password, onValueChange = { password = it },
                label = "Contraseña inicial *", error = errPassword,
                hint = "Mínimo 8 caracteres", isPassword = true)

            SectionLabel("DATOS ACADÉMICOS (opcional)")
            FormField(value = especialidad, onValueChange = { especialidad = it },
                label = "Especialidad", error = "")
            FormField(value = tituloAcademico, onValueChange = { tituloAcademico = it },
                label = "Título académico", error = "")

            if (errorExterno.isNotEmpty()) {
                Surface(color = Color(0xFF8A0000).copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)) {
                    Text(errorExterno, color = Color(0xFFFF6B6B), fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            Surface(color = Color(0xFF40798C).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top) {
                    Text("🔐", fontSize = 14.sp)
                    Text("Al guardar, se enviará automáticamente el código QR de 2FA al correo del docente.",
                        fontSize = 11.sp, color = Color(0xFF59FFCC).copy(alpha = 0.85f), lineHeight = 16.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(
                    onClick = {
                        if (validar()) {
                            onGuardar(CreateDocenteRequest(
                                ci              = ci.trim(),
                                primerNombre    = primerNombre.trim(),
                                segundoNombre   = segundoNombre.trim().takeIf { it.isNotBlank() },
                                apellidoPaterno = apellidoPaterno.trim(),
                                apellidoMaterno = apellidoMaterno.trim().takeIf { it.isNotBlank() },
                                email           = email.trim().lowercase(),
                                password        = password,
                                especialidad    = especialidad.trim(),
                                tituloAcademico = tituloAcademico.trim()
                            ))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF40798C))
                ) { Text("Guardar") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES AUXILIARES
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp, color = Color(0xFF40798C))
}

@Composable
private fun FormField(
    value: String, onValueChange: (String) -> Unit, label: String,
    error: String, hint: String = "", isPassword: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) }, singleLine = true, isError = error.isNotEmpty(),
            visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation()
                                   else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            colors   = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Color(0xFF40798C), unfocusedBorderColor = Color(0xFF555555),
                focusedLabelColor    = Color(0xFF40798C), cursorColor          = Color(0xFF40798C)
            )
        )
        if (error.isNotEmpty()) Text(error, color = Color(0xFFFF6B6B), fontSize = 11.sp)
        else if (hint.isNotEmpty()) Text(hint, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, width: Dp?, weight: Float = 0f) {
    val mod = if (width != null) Modifier.width(width) else Modifier.weight(weight)
    Text(text, modifier = mod, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFBBBBBB))
}

@Composable
private fun RowScope.MonoCell(text: String, width: Dp?, color: Color, weight: Float = 0f, bold: Boolean = false) {
    val mod = if (width != null) Modifier.width(width) else Modifier.weight(weight)
    Text(text, modifier = mod, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
        fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal, color = color)
}

@Composable
private fun RowScope.TextCell(text: String, width: Dp?, color: Color, weight: Float = 0f) {
    val mod = if (width != null) Modifier.width(width) else Modifier.weight(weight)
    Text(text, modifier = mod, fontSize = 12.sp, color = color)
}

@Composable
private fun LabelValor(label: String, valor: String, monospace: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label:", fontSize = 13.sp, color = TextMuted)
        Text(valor, fontSize = 13.sp,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default, color = Color.White)
    }
}

private fun metodoColor(metodo: String) = when (metodo) {
    "GET"    -> Color(0xFF59FFCC)
    "POST"   -> Color(0xFF82B4FF)
    "DELETE" -> Color(0xFFFF6B6B)
    "PUT"    -> Color(0xFFFFCC59)
    else     -> Color(0xFFBBBBBB)
}

private fun statusColor(status: Int) = when {
    status in 200..299 -> Color(0xFF59FFCC)
    status in 400..499 -> Color(0xFFFFCC59)
    status >= 500       -> Color(0xFFFF6B6B)
    else                -> Color(0xFF9E9E9E)
}

private fun formatUptime(d: Duration): String {
    val h = d.toHours()
    val m = d.toMinutes() % 60
    val s = d.seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
