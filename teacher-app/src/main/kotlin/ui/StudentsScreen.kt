package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import EmailService
import EstudianteImportRow
import ExcelImporter
import db.ClaseRepository
import db.EstudianteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Clase
import models.CreateEstudianteRequest
import models.Estudiante
import models.UpdateEstudianteRequest

@Composable
fun StudentsScreen(docenteId: String) {
    val scope       = rememberCoroutineScope()
    var students    by remember { mutableStateOf<List<Estudiante>>(emptyList()) }
    var classes     by remember { mutableStateOf<List<Clase>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var showForm    by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }
    var loadError    by remember { mutableStateOf<String?>(null) }
    var filterClass      by remember { mutableStateOf<String?>(null) }
    var filterEstado     by remember { mutableStateOf<String?>(null) }
    var search           by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importRows       by remember { mutableStateOf<List<EstudianteImportRow>>(emptyList()) }
    var importProgress   by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            classes   = withContext(Dispatchers.IO) { ClaseRepository.getAll(docenteId) }
            students  = withContext(Dispatchers.IO) { EstudianteRepository.getAll() }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            loadError = "Error al cargar alumnos: ${e.message}"
            println("[ERROR] Carga fallida: ${e.message}")
        }
    }

    suspend fun reloadData() {
        try {
            students = withContext(Dispatchers.IO) { EstudianteRepository.getAll() }
            classes  = withContext(Dispatchers.IO) { ClaseRepository.getAll(docenteId) }
        } catch (e: Exception) {
            loadError = "Error al recargar: ${e.message}"
        }
    }

    val visible = students
        .filter { e ->
            search.isBlank() ||
            e.nombre.contains(search, ignoreCase = true) ||
            e.apellido.contains(search, ignoreCase = true) ||
            e.usuarioId.contains(search) ||
            e.claseNombre.contains(search, ignoreCase = true) ||
            e.fechaNacimiento.contains(search)
        }
        .filter { filterClass  == null || it.claseId == filterClass }
        .filter { filterEstado == null || it.estado  == filterEstado }

    val eliminados = students.filter { it.estado != "activo" }

    Column(
        modifier = Modifier.fillMaxSize().background(DColors.Background).padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Alumnos", fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold, color = DColors.OnSurface)
                Text("${visible.size} mostrado${if (visible.size != 1) "s" else ""} · ${students.size} total",
                    fontSize = 13.sp, color = DColors.OnSurfaceVariant)
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            PdfExporter.exportEstudiantes(visible,
                                buildString {
                                    if (search.isNotBlank()) append("Búsqueda: $search ")
                                    if (filterEstado != null) append("Estado: $filterEstado ")
                                    if (filterClass != null) {
                                        val cls = classes.find { it.claseId == filterClass }
                                        append("Clase: ${cls?.nombre}")
                                    }
                                })
                        }
                    }
                },
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Rounded.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("PDF", fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = {
                    scope.launch { withContext(Dispatchers.IO) { PdfExporter.exportEliminados(eliminados) } }
                },
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Rounded.RemoveCircleOutline, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Eliminados", fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = {
                    val chooser = javax.swing.JFileChooser().apply {
                        fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
                            "Excel (*.xlsx)", "xlsx"
                        )
                        dialogTitle = "Seleccionar archivo Excel de alumnos"
                    }
                    if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        importRows       = ExcelImporter.parse(chooser.selectedFile)
                        showImportDialog = true
                    }
                },
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Rounded.Upload, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Importar Excel", fontSize = 13.sp)
            }

            Button(
                onClick = { showForm = !showForm },
                colors  = ButtonDefaults.buttonColors(containerColor = DColors.Primary),
                shape   = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(if (showForm) Icons.Rounded.Close else Icons.Rounded.PersonAdd,
                    null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (showForm) "Cancelar" else "Nuevo alumno", fontSize = 14.sp)
            }
        }

        if (showImportDialog && importRows.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = {
                    Text("Importar ${importRows.size} alumnos",
                        fontWeight = FontWeight.Medium)
                },
                text = {
                    val valid   = importRows.filter { it.isValid }
                    val invalid = importRows.filter { !it.isValid }

                    Column(Modifier.heightIn(max = 400.dp)) {
                        if (valid.isNotEmpty()) {
                            Text("✅ ${valid.size} filas válidas listas para importar",
                                fontSize = 13.sp, color = DColors.Success)
                        }
                        if (invalid.isNotEmpty()) {
                            Text("❌ ${invalid.size} filas con errores (se omitirán):",
                                fontSize = 13.sp, color = DColors.Error,
                                modifier = Modifier.padding(top = 8.dp))
                            LazyColumn(Modifier.heightIn(max = 200.dp)) {
                                items(invalid) { row ->
                                    Text("• Fila ${row.rowNumber}: ${row.errors.joinToString(", ")}",
                                        fontSize = 11.sp, color = DColors.OnErrorContainer,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp))
                                }
                            }
                        }
                        importProgress?.let {
                            Text(it, fontSize = 12.sp, color = DColors.Primary,
                                modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val valid = importRows.filter { it.isValid }
                                var ok = 0; var fail = 0
                                valid.forEach { row ->
                                    try {
                                        val clase = classes.find {
                                            it.nombre.equals(row.claseNombre, ignoreCase = true)
                                        }
                                        if (clase == null) { fail++; return@forEach }

                                        val req = CreateEstudianteRequest(
                                            ciNumero        = row.ci,
                                            ciComplemento   = row.complementoCi,
                                            nombre          = "${row.nombre} ${row.segundoNombre}".trim(),
                                            apellido        = row.apellidoCompleto,
                                            password        = generateSecurePassword(),
                                            claseId         = clase.claseId,
                                            fechaNacimiento = row.fechaNacimiento,
                                            correoPersonal  = row.correoPersonal,
                                        )
                                        withContext(Dispatchers.IO) {
                                            val est = EstudianteRepository.create(req)
                                            if (row.correoPersonal.isNotBlank()) {
                                                EmailService.sendCredentials(
                                                    toEmail     = row.correoPersonal,
                                                    studentName = row.nombreCompleto,
                                                    loginEmail  = est.email,
                                                    password    = req.password,
                                                )
                                            }
                                        }
                                        ok++
                                        importProgress = "Procesando... $ok de ${valid.size}"
                                    } catch (_: Exception) { fail++ }
                                }
                                students         = withContext(Dispatchers.IO) { EstudianteRepository.getAll() }
                                classes          = withContext(Dispatchers.IO) { ClaseRepository.getAll(docenteId) }
                                showImportDialog = false
                                importProgress   = null
                            }
                        },
                        enabled = importRows.any { it.isValid },
                        colors  = ButtonDefaults.buttonColors(containerColor = DColors.Primary)
                    ) { Text("Importar ${importRows.count { it.isValid }} válidos") }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Spacer(Modifier.height(10.dp))

        DTextField(value = search, onValueChange = { search = it },
            label = "Buscar por nombre, C.I., curso o fecha de nacimiento",
            modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()) {
            item {
                FilterChip(
                    label    = "Todos",
                    count    = students.size,
                    selected = filterClass == null,
                    onClick  = { filterClass = null }
                )
            }
            items(classes) { cls ->
                FilterChip(
                    label    = cls.nombre,
                    count    = cls.totalAlumnos,
                    selected = filterClass == cls.claseId,
                    onClick  = { filterClass = cls.claseId }
                )
            }
            item { Spacer(Modifier.width(4.dp)) }
            items(listOf("activo", "inactivo", "suspendido")) { estado ->
                val (selColor, selContent) = when (estado) {
                    "inactivo"   -> DColors.PrimaryContainer  to DColors.OnPrimaryContainer
                    "suspendido" -> DColors.ErrorContainer    to DColors.OnErrorContainer
                    else         -> DColors.SuccessContainer  to DColors.OnSuccessContainer
                }
                FilterChip(
                    label            = estado.replaceFirstChar { it.uppercase() },
                    count            = students.count { it.estado == estado },
                    selected         = filterEstado == estado,
                    selectedColor    = selColor,
                    selectedContent  = selContent,
                    onClick          = { filterEstado = if (filterEstado == estado) null else estado }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        AnimatedVisibility(visible = showForm) {
            CreateStudentForm(
                classes  = classes,
                errorMsg = errorMsg,
                onCreated = { req ->
                    scope.launch {
                        try {
                            val estudiante = withContext(Dispatchers.IO) { EstudianteRepository.create(req) }

                            if (req.correoPersonal.isNotBlank()) {
                                withContext(Dispatchers.IO) {
                                    EmailService.sendCredentials(
                                        toEmail     = req.correoPersonal,
                                        studentName = "${req.nombre} ${req.apellido}",
                                        loginEmail  = estudiante.email,
                                        password    = req.password,
                                    )
                                }
                            }

                            students = withContext(Dispatchers.IO) { EstudianteRepository.getAll() }
                            classes  = withContext(Dispatchers.IO) { ClaseRepository.getAll(docenteId) }
                            showForm = false
                            errorMsg = null
                        } catch (e: Exception) {
                            errorMsg = "Error: ${e.message}"
                        }
                    }
                }
            )
        }

        loadError?.let { msg ->
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = DColors.ErrorContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Warning, null,
                        tint = DColors.Error, modifier = Modifier.size(18.dp))
                    Text(msg, fontSize = 13.sp, color = DColors.OnErrorContainer,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        loadError = null
                        isLoading = true
                        scope.launch {
                            try {
                                classes   = withContext(Dispatchers.IO) { ClaseRepository.getAll(docenteId) }
                                students  = withContext(Dispatchers.IO) { EstudianteRepository.getAll() }
                                isLoading = false
                            } catch (e: Exception) {
                                isLoading = false
                                loadError = "Error al cargar alumnos: ${e.message}"
                            }
                        }
                    }) {
                        Text("Reintentar", color = DColors.Error, fontSize = 12.sp)
                    }
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DColors.Primary)
            }
        } else if (visible.isEmpty()) {
            EmptyState("👤", "Sin alumnos",
                if (filterClass == null) "Crea el primer alumno con el botón de arriba"
                else "No hay alumnos en este curso todavía")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.usuarioId }) { student ->
                    StudentCard(
                        student     = student,
                        classes     = classes,
                        onDelete    = {
                            scope.launch {
                                withContext(Dispatchers.IO) { EstudianteRepository.delete(student.usuarioId) }
                                reloadData()
                            }
                        },
                        onUpdate    = { req ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    EstudianteRepository.update(
                                        ci              = student.usuarioId,
                                        nombre          = req.nombre,
                                        apellido        = req.apellido,
                                        claseId         = req.claseId,
                                        fechaNacimiento = req.fechaNacimiento,
                                        newPassword     = req.newPassword
                                    )
                                }
                                reloadData()
                            }
                        },
                        onSetEstado = { estado ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    EstudianteRepository.setEstado(student.usuarioId, estado)
                                }
                                reloadData()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String, count: Int, selected: Boolean, onClick: () -> Unit,
    selectedColor:   androidx.compose.ui.graphics.Color = DColors.Primary,
    selectedContent: androidx.compose.ui.graphics.Color = DColors.OnPrimary
) {
    Surface(
        shape    = RoundedCornerShape(999.dp),
        color    = if (selected) selectedColor else DColors.SurfaceContainer,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = if (selected) selectedContent else DColors.OnSurfaceVariant)
            Surface(shape = RoundedCornerShape(999.dp),
                color = if (selected) selectedContent.copy(alpha = 0.2f)
                        else DColors.SurfaceContainerHigh) {
                Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = if (selected) selectedContent else DColors.OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun DatePickerField(
    value         : String,
    onValueChange : (String) -> Unit,
    label         : String   = "Fecha de nacimiento",
    modifier      : Modifier = Modifier,
) {
    val currentYear = java.time.LocalDate.now().year
    val parts        = value.split("-")
    var selectedYear  by remember(value) { mutableStateOf(parts.getOrNull(0)?.toIntOrNull()) }
    var selectedMonth by remember(value) { mutableStateOf(parts.getOrNull(1)?.toIntOrNull()) }
    var selectedDay   by remember(value) { mutableStateOf(parts.getOrNull(2)?.toIntOrNull()) }

    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        val y = selectedYear; val m = selectedMonth; val d = selectedDay
        if (y != null && m != null && d != null)
            onValueChange("%04d-%02d-%02d".format(y, m, d))
    }

    val months = listOf(
        "Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    )
    val daysInMonth = if (selectedYear != null && selectedMonth != null)
        java.time.YearMonth.of(selectedYear!!, selectedMonth!!).lengthOfMonth()
    else 31
    val validYears = (currentYear - 5) downTo 1990

    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = DColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateDropdown(
                label    = "Día",
                value    = selectedDay?.toString() ?: "—",
                options  = (1..daysInMonth).map { it.toString() },
                onSelect = { selectedDay = it.toInt() },
                modifier = Modifier.weight(1f)
            )
            DateDropdown(
                label    = "Mes",
                value    = selectedMonth?.let { months[it - 1] } ?: "—",
                options  = months,
                onSelect = { selectedMonth = months.indexOf(it) + 1 },
                modifier = Modifier.weight(2f)
            )
            DateDropdown(
                label    = "Año",
                value    = selectedYear?.toString() ?: "—",
                options  = validYears.map { it.toString() },
                onSelect = { selectedYear = it.toInt() },
                modifier = Modifier.weight(1.5f)
            )
        }
        val isDateFuture = if (selectedYear != null && selectedMonth != null && selectedDay != null) {
            try {
                java.time.LocalDate.of(selectedYear!!, selectedMonth!!, selectedDay!!)
                    .isAfter(java.time.LocalDate.now())
            } catch (_: Exception) { false }
        } else false
        if (isDateFuture) {
            Text("La fecha no puede ser futura", color = DColors.Error, fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun DateDropdown(
    label    : String,
    value    : String,
    options  : List<String>,
    onSelect : (String) -> Unit,
    modifier : Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = DColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp))
        Box {
            Surface(
                shape    = RoundedCornerShape(8.dp),
                color    = DColors.SurfaceContainerHigh,
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(value, fontSize = 13.sp, color = DColors.OnSurface,
                        modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.ArrowDropDown, null,
                        tint = DColors.OnSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
            DropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
                modifier         = Modifier.background(DColors.SurfaceContainerHigh)
            ) {
                Box(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
                    Column {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text    = { Text(option, fontSize = 13.sp, color = DColors.OnSurface) },
                                onClick = { onSelect(option); expanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateSecurePassword(): String {
    val upper   = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val lower   = "abcdefghjkmnpqrstuvwxyz"
    val digits  = "23456789"
    val special = "@#\$%&*!?"
    val all     = upper + lower + digits + special
    val rng     = java.security.SecureRandom()
    val pwd     = StringBuilder()
    pwd.append(upper[rng.nextInt(upper.length)])
    pwd.append(lower[rng.nextInt(lower.length)])
    pwd.append(digits[rng.nextInt(digits.length)])
    pwd.append(special[rng.nextInt(special.length)])
    repeat(6) { pwd.append(all[rng.nextInt(all.length)]) }
    return pwd.toList().shuffled(rng).joinToString("")
}

@Composable
private fun CreateStudentForm(
    classes:  List<Clase>,
    errorMsg: String?,
    onCreated: (CreateEstudianteRequest) -> Unit
) {
    var ciNumero        by remember { mutableStateOf("") }
    var ciComplemento   by remember { mutableStateOf("") }
    var nombre          by remember { mutableStateOf("") }
    var apellido        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf(generateSecurePassword()) }
    var showPassword    by remember { mutableStateOf(false) }
    var correoPersonal  by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var selectedClass   by remember { mutableStateOf<Clase?>(null) }
    var expanded        by remember { mutableStateOf(false) }

    val emailPreview = if (nombre.isNotBlank() && apellido.isNotBlank() && selectedClass != null) {
        val base = "$nombre $apellido"
            .lowercase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n")
            .trim().split(" ").filter { it.isNotBlank() }.joinToString(".")
        "$base.${selectedClass!!.grado}@didactai.edu"
    } else "···"

    Surface(shape = RoundedCornerShape(14.dp), color = DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nuevo alumno", fontWeight = FontWeight.Medium, color = DColors.OnSurface)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DTextField(
                    value         = ciNumero,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) ciNumero = it },
                    label         = "C.I. (6-8 dígitos)",
                    modifier      = Modifier.weight(1.5f)
                )
                DTextField(
                    value         = ciComplemento,
                    onValueChange = { if (it.length <= 3) ciComplemento = it.uppercase() },
                    label         = "Compl. (ej: 1A)",
                    modifier      = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {
                DTextField(
                    value         = password,
                    onValueChange = { password = it },
                    label         = "Contraseña generada",
                    isPassword    = !showPassword,
                    modifier      = Modifier.weight(1f)
                )
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null, tint = DColors.Primary
                    )
                }
                IconButton(onClick = { password = generateSecurePassword() }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Regenerar", tint = DColors.Primary)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DTextField(value = nombre, onValueChange = { nombre = it },
                    label = "Nombre", modifier = Modifier.weight(1f))
                DTextField(value = apellido, onValueChange = { apellido = it },
                    label = "Apellido", modifier = Modifier.weight(1f))
            }

            val correoError = correoPersonal.isNotBlank() &&
                !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$").matches(correoPersonal)

            DTextField(
                value         = correoPersonal,
                onValueChange = { correoPersonal = it },
                label         = "Correo personal del alumno",
                modifier      = Modifier.fillMaxWidth()
            )
            if (correoError) {
                Text("Correo inválido", color = DColors.Error, fontSize = 11.sp)
            }

            DatePickerField(
                value         = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                modifier      = Modifier.fillMaxWidth()
            )

            Column {
                Text("Curso", fontSize = 11.sp, color = DColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp))
                Box {
                    Surface(
                        shape    = RoundedCornerShape(8.dp),
                        color    = DColors.SurfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                selectedClass?.let { "${it.nombre} · ${it.grado}" } ?: "Selecciona un curso",
                                fontSize = 14.sp,
                                color    = if (selectedClass != null) DColors.OnSurface else DColors.OnSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Rounded.ArrowDropDown, null,
                                tint = DColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier.background(DColors.SurfaceContainerHigh)) {
                        classes.forEach { cls ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(cls.nombre, fontSize = 14.sp, color = DColors.OnSurface)
                                        Text(cls.grado, fontSize = 11.sp, color = DColors.OnSurfaceVariant)
                                    }
                                },
                                onClick = { selectedClass = cls; expanded = false }
                            )
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(8.dp),
                color = DColors.Primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Mail, null, tint = DColors.Primary,
                        modifier = Modifier.size(16.dp))
                    Text("Correo generado: ", fontSize = 12.sp, color = DColors.OnSurfaceVariant)
                    Text(emailPreview, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        color = DColors.Primary)
                }
            }

            errorMsg?.let { Text(it, color = DColors.Error, fontSize = 13.sp) }

            val ciValida    = ciNumero.length in 6..8 && ciNumero.all { it.isDigit() }
            val correoOk    = correoPersonal.isBlank() ||
                Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$").matches(correoPersonal)
            val fechaValida = fechaNacimiento.isNotBlank() && try {
                !java.time.LocalDate.parse(fechaNacimiento).isAfter(java.time.LocalDate.now())
            } catch (_: Exception) { false }

            Button(
                onClick  = {
                    onCreated(CreateEstudianteRequest(
                        ciNumero        = ciNumero.trim(),
                        ciComplemento   = ciComplemento.trim(),
                        nombre          = nombre.trim(),
                        apellido        = apellido.trim(),
                        password        = password,
                        claseId         = selectedClass!!.claseId,
                        fechaNacimiento = fechaNacimiento.trim(),
                        correoPersonal  = correoPersonal.trim(),
                    ))
                },
                enabled  = ciValida && nombre.isNotBlank() && apellido.isNotBlank() &&
                           password.length >= 10 && selectedClass != null && correoOk && fechaValida,
                colors   = ButtonDefaults.buttonColors(containerColor = DColors.Primary),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Crear alumno") }
        }
    }
}

@Composable
private fun StudentCard(
    student:     Estudiante,
    classes:     List<Clase>,
    onDelete:    () -> Unit,
    onUpdate:    (UpdateEstudianteRequest) -> Unit,
    onSetEstado: (String) -> Unit
) {
    var showEdit      by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val estadoColor = when (student.estado) {
        "suspendido" -> DColors.Error
        "inactivo"   -> DColors.OnSurfaceVariant
        else         -> DColors.Success
    }
    val estadoBg = when (student.estado) {
        "suspendido" -> DColors.ErrorContainer
        "inactivo"   -> DColors.SurfaceContainerHigh
        else         -> DColors.SuccessContainer
    }

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = if (student.estado == "suspendido")
                       DColors.ErrorContainer.copy(alpha = 0.15f)
                   else DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                Box(modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(DColors.Primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center) {
                    Text(student.nombre.take(1).uppercase(),
                        color = DColors.Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Column(Modifier.weight(1f)) {
                    Text("${student.nombre} ${student.apellido}",
                        fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DColors.OnSurface)
                    Text(student.email, fontSize = 12.sp, color = DColors.OnSurfaceVariant)
                }

                Chip(label = student.claseNombre,
                    color = DColors.SecondaryContainer, textColor = DColors.OnSecondaryContainer)

                Surface(shape = RoundedCornerShape(4.dp), color = estadoBg) {
                    Text(student.estado.replaceFirstChar { it.uppercase() },
                        fontSize = 10.sp, fontWeight = FontWeight.Medium, color = estadoColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }

                if (confirmDelete) {
                    TextButton(onClick = { confirmDelete = false }) {
                        Text("Cancelar", color = DColors.OnSurfaceVariant, fontSize = 12.sp)
                    }
                    TextButton(onClick = { onDelete(); confirmDelete = false }) {
                        Text("Eliminar", color = DColors.Error, fontSize = 12.sp)
                    }
                } else {
                    IconButton(onClick = { showEdit = !showEdit }) {
                        Icon(Icons.Rounded.Edit, null,
                            tint = DColors.Primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        val nuevoEstado = when (student.estado) {
                            "activo"     -> "inactivo"
                            "inactivo"   -> "activo"
                            "suspendido" -> "activo"
                            else         -> "inactivo"
                        }
                        onSetEstado(nuevoEstado)
                    }) {
                        Icon(
                            if (student.estado == "activo") Icons.Rounded.Block
                            else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (student.estado == "activo") DColors.Error else DColors.Success,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (student.estado != "suspendido") {
                        IconButton(onClick = { onSetEstado("suspendido") }) {
                            Icon(Icons.Rounded.Warning, null,
                                tint = DColors.Tertiary, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Rounded.DeleteOutline, null,
                            tint = DColors.OnSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }

            AnimatedVisibility(visible = showEdit) {
                EditEstudianteForm(
                    student  = student,
                    classes  = classes,
                    onSave   = { req ->
                        onUpdate(req)
                        showEdit = false
                    },
                    onCancel = { showEdit = false }
                )
            }
        }
    }
}

@Composable
private fun EditEstudianteForm(
    student:  Estudiante,
    classes:  List<Clase>,
    onSave:   (UpdateEstudianteRequest) -> Unit,
    onCancel: () -> Unit
) {
    var nombre          by remember { mutableStateOf(student.nombre) }
    var apellido        by remember { mutableStateOf(student.apellido) }
    var newPassword     by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf(student.fechaNacimiento) }
    var selectedCls     by remember { mutableStateOf(classes.find { it.claseId == student.claseId }) }
    var expanded        by remember { mutableStateOf(false) }

    HorizontalDivider(color = DColors.OutlineVariant)
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Editar alumno", fontSize = 13.sp,
            fontWeight = FontWeight.Medium, color = DColors.Primary)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DTextField(value = nombre, onValueChange = { nombre = it },
                label = "Nombre", modifier = Modifier.weight(1f))
            DTextField(value = apellido, onValueChange = { apellido = it },
                label = "Apellido", modifier = Modifier.weight(1f))
        }

        DTextField(value = newPassword, onValueChange = { newPassword = it },
            label = "Nueva contraseña (vacío = no cambia)",
            isPassword = true, modifier = Modifier.fillMaxWidth())

        DatePickerField(
            value         = fechaNacimiento,
            onValueChange = { fechaNacimiento = it },
            modifier      = Modifier.fillMaxWidth()
        )

        Column {
            Text("Curso", fontSize = 11.sp, color = DColors.OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp))
            Box {
                Surface(shape = RoundedCornerShape(8.dp),
                    color = DColors.SurfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCls?.nombre ?: "Selecciona curso",
                            fontSize = 14.sp, modifier = Modifier.weight(1f),
                            color = if (selectedCls != null) DColors.OnSurface
                                    else DColors.OnSurfaceVariant)
                        Icon(Icons.Rounded.ArrowDropDown, null,
                            tint = DColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
                DropdownMenu(expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(DColors.SurfaceContainerHigh)) {
                    classes.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls.nombre, color = DColors.OnSurface) },
                            onClick = { selectedCls = cls; expanded = false }
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.End)) {
            TextButton(onClick = onCancel) {
                Text("Cancelar", color = DColors.OnSurfaceVariant)
            }
            Button(
                onClick = {
                    onSave(UpdateEstudianteRequest(
                        nombre          = nombre.trim(),
                        apellido        = apellido.trim(),
                        claseId         = selectedCls!!.claseId,
                        fechaNacimiento = fechaNacimiento.trim(),
                        newPassword     = newPassword.takeIf { it.isNotBlank() }
                    ))
                },
                enabled = nombre.isNotBlank() && apellido.isNotBlank() && selectedCls != null,
                colors  = ButtonDefaults.buttonColors(containerColor = DColors.Primary),
                shape   = RoundedCornerShape(8.dp)
            ) { Text("Guardar") }
        }
    }
}
