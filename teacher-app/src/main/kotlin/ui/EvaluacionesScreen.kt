package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.ContenidoRepository
import db.EvaluacionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.*

@Composable
fun EvaluacionesScreen(docenteId: String) {
    val scope        = rememberCoroutineScope()
    var evaluaciones by remember { mutableStateOf<List<Evaluacion>>(emptyList()) }
    var contenidos   by remember { mutableStateOf<List<ContenidoBiologico>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var showForm     by remember { mutableStateOf(false) }
    var search       by remember { mutableStateOf("") }
    var filterNivel  by remember { mutableStateOf<Int?>(null) }
    var loadError    by remember { mutableStateOf<String?>(null) }

    val visible = evaluaciones.filter { e ->
        (search.isBlank() ||
         e.titulo.contains(search, ignoreCase = true) ||
         e.tituloContenido.contains(search, ignoreCase = true)) &&
        (filterNivel == null || e.nivelDificultad == filterNivel)
    }

    fun reload() {
        scope.launch {
            try {
                evaluaciones = withContext(Dispatchers.IO) { EvaluacionRepository.getAll() }
            } catch (e: Exception) {
                loadError = "Error al recargar evaluaciones: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            contenidos = withContext(Dispatchers.IO) { ContenidoRepository.getAll() }
        } catch (e: Exception) {
            loadError = "Error al cargar contenidos: ${e.message}"
        }
        reload()
        isLoading = false
    }

    Column(Modifier.fillMaxSize().background(DColors.Background).padding(24.dp)) {

        // ── Encabezado ───────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Evaluaciones", fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold, color = DColors.OnSurface)
                Text("${visible.size} de ${evaluaciones.size} evaluaciones",
                    fontSize = 13.sp, color = DColors.OnSurfaceVariant)
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            PdfExporter.exportEvaluaciones(visible,
                                buildString {
                                    if (search.isNotBlank()) append("Búsqueda: $search ")
                                    if (filterNivel != null) append("Nivel: $filterNivel")
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
            Button(
                onClick = { showForm = !showForm },
                colors  = ButtonDefaults.buttonColors(containerColor = DColors.Secondary),
                shape   = RoundedCornerShape(10.dp)
            ) {
                Icon(if (showForm) Icons.Rounded.Close else Icons.Rounded.Add,
                    null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (showForm) "Cancelar" else "Nueva evaluación", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Búsqueda ─────────────────────────────────────────────────────────
        DTextField(value = search, onValueChange = { search = it },
            label = "Buscar por título o contenido",
            modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))

        // ── Filtro por nivel ──────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("Nivel:", fontSize = 12.sp, color = DColors.OnSurfaceVariant)
            listOf(null, 1, 2, 3, 4, 5).forEach { lvl ->
                EvalFilterChip(
                    label    = lvl?.toString() ?: "Todos",
                    count    = if (lvl == null) evaluaciones.size
                               else evaluaciones.count { it.nivelDificultad == lvl },
                    selected = filterNivel == lvl,
                    onClick  = { filterNivel = lvl }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Formulario de creación ────────────────────────────────────────────
        AnimatedVisibility(visible = showForm) {
            CreateEvaluacionForm(
                contenidos = contenidos,
                docenteId  = docenteId,
                onCreated  = { req ->
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) { EvaluacionRepository.create(req) }
                            showForm = false
                        } catch (e: Exception) {
                            loadError = "Error al crear evaluación: ${e.message}"
                        }
                        reload()
                    }
                }
            )
        }

        // ── Lista ─────────────────────────────────────────────────────────────
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
                                contenidos = withContext(Dispatchers.IO) { ContenidoRepository.getAll() }
                            } catch (e: Exception) {
                                loadError = "Error al cargar contenidos: ${e.message}"
                            }
                            reload()
                            isLoading = false
                        }
                    }) {
                        Text("Reintentar", color = DColors.Error, fontSize = 12.sp)
                    }
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DColors.Secondary)
            }
        } else if (visible.isEmpty()) {
            EmptyState("📋", "Sin evaluaciones", "Crea la primera evaluación arriba")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.evaluacionId }) { eval ->
                    EvaluacionCard(
                        eval       = eval,
                        contenidos = contenidos,
                        onDelete   = {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) { EvaluacionRepository.delete(eval.evaluacionId) }
                                } catch (e: Exception) {
                                    loadError = "Error al eliminar evaluación: ${e.message}"
                                }
                                reload()
                            }
                        },
                        onUpdate   = { req ->
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) { EvaluacionRepository.update(eval.evaluacionId, req) }
                                } catch (e: Exception) {
                                    loadError = "Error al actualizar evaluación: ${e.message}"
                                }
                                reload()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateEvaluacionForm(
    contenidos: List<ContenidoBiologico>,
    docenteId:  String,
    onCreated:  (CreateEvaluacionRequest) -> Unit
) {
    var titulo       by remember { mutableStateOf("") }
    var nivel        by remember { mutableStateOf(1) }
    var selectedCont by remember { mutableStateOf<ContenidoBiologico?>(null) }
    var expanded     by remember { mutableStateOf(false) }

    Surface(shape = RoundedCornerShape(14.dp), color = DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nueva evaluación", fontWeight = FontWeight.Medium, color = DColors.OnSurface)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DTextField(value = titulo, onValueChange = { titulo = it },
                    label = "Título de la evaluación", modifier = Modifier.weight(1f))

                Column(Modifier.weight(1f)) {
                    Text("Contenido biológico", fontSize = 11.sp,
                        color = DColors.OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        Surface(shape = RoundedCornerShape(8.dp),
                            color = DColors.SurfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedCont?.titulo ?: "Selecciona",
                                    fontSize = 14.sp, modifier = Modifier.weight(1f),
                                    color = if (selectedCont != null) DColors.OnSurface
                                            else DColors.OnSurfaceVariant)
                                Icon(Icons.Rounded.ArrowDropDown, null,
                                    tint = DColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(DColors.SurfaceContainerHigh)) {
                            contenidos.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.titulo, color = DColors.OnSurface) },
                                    onClick = { selectedCont = c; expanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nivel de dificultad:", fontSize = 12.sp, color = DColors.OnSurfaceVariant)
                (1..5).forEach { lvl ->
                    Surface(shape = RoundedCornerShape(6.dp),
                        color = if (nivel == lvl) DColors.Secondary else DColors.SurfaceContainerHigh,
                        modifier = Modifier.size(36.dp).clickable { nivel = lvl }) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$lvl", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = if (nivel == lvl) DColors.OnSecondary else DColors.OnSurfaceVariant)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onCreated(CreateEvaluacionRequest(
                        contenidoId     = selectedCont!!.contenidoId,
                        titulo          = titulo.trim(),
                        nivelDificultad = nivel,
                        docenteId       = docenteId
                    ))
                },
                enabled  = titulo.isNotBlank() && selectedCont != null,
                colors   = ButtonDefaults.buttonColors(containerColor = DColors.Secondary),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Crear evaluación") }
        }
    }
}

@Composable
private fun EvaluacionCard(
    eval:       Evaluacion,
    contenidos: List<ContenidoBiologico>,
    onDelete:   () -> Unit,
    onUpdate:   (UpdateEvaluacionRequest) -> Unit
) {
    var showEdit      by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showIntentos  by remember { mutableStateOf(false) }
    val intentos      = remember { mutableStateOf<List<IntentoResumen>>(emptyList()) }
    val scope         = rememberCoroutineScope()

    val nivelColors = listOf(
        DColors.SuccessContainer   to DColors.OnSuccessContainer,
        DColors.PrimaryContainer   to DColors.OnPrimaryContainer,
        DColors.SecondaryContainer to DColors.OnSecondaryContainer,
        DColors.TertiaryContainer  to DColors.OnTertiaryContainer,
        DColors.ErrorContainer     to DColors.OnErrorContainer
    )
    val nc = nivelColors.getOrElse(eval.nivelDificultad - 1) { nivelColors[0] }

    Surface(shape = RoundedCornerShape(12.dp), color = DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                Column(Modifier.weight(1f)) {
                    Text(eval.titulo, fontSize = 15.sp,
                        fontWeight = FontWeight.Medium, color = DColors.OnSurface)
                    Text(eval.tituloContenido, fontSize = 12.sp, color = DColors.OnSurfaceVariant)
                }

                Chip(label = "Nivel ${eval.nivelDificultad}", color = nc.first, textColor = nc.second)

                Surface(shape = RoundedCornerShape(4.dp), color = DColors.SurfaceContainerHigh) {
                    Text("${eval.totalIntentos} intentos", fontSize = 11.sp,
                        color = DColors.OnSurfaceVariant,
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
                    IconButton(onClick = {
                        showIntentos = !showIntentos
                        if (showIntentos && intentos.value.isEmpty()) {
                            scope.launch {
                                intentos.value = withContext(Dispatchers.IO) {
                                    EvaluacionRepository.getIntentos(evaluacionId = eval.evaluacionId)
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Rounded.BarChart, null,
                            tint = DColors.Primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showEdit = !showEdit }) {
                        Icon(Icons.Rounded.Edit, null,
                            tint = DColors.Tertiary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Rounded.DeleteOutline, null,
                            tint = DColors.OnSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Panel de intentos
            AnimatedVisibility(visible = showIntentos) {
                Column {
                    HorizontalDivider(color = DColors.OutlineVariant)
                    if (intentos.value.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            Text("Sin intentos registrados", fontSize = 13.sp,
                                color = DColors.OnSurfaceVariant)
                        }
                    } else {
                        intentos.value.forEach { intento ->
                            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(intento.nombreEstudiante, fontSize = 13.sp,
                                    color = DColors.OnSurface, modifier = Modifier.weight(1f))
                                Text("✓${intento.correctas} ✗${intento.incorrectas}",
                                    fontSize = 12.sp, color = DColors.OnSurfaceVariant)
                                Text("${intento.puntajeTotal.toInt()}%",
                                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                    color = when {
                                        intento.puntajeTotal >= 70 -> DColors.Success
                                        intento.puntajeTotal >= 40 -> DColors.Primary
                                        else -> DColors.Error
                                    })
                            }
                            HorizontalDivider(color = DColors.OutlineVariant,
                                modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }

            // Panel de edición
            AnimatedVisibility(visible = showEdit) {
                EditEvaluacionForm(
                    eval       = eval,
                    contenidos = contenidos,
                    onSave     = { req -> onUpdate(req); showEdit = false },
                    onCancel   = { showEdit = false }
                )
            }
        }
    }
}

@Composable
private fun EditEvaluacionForm(
    eval:       Evaluacion,
    contenidos: List<ContenidoBiologico>,
    onSave:     (UpdateEvaluacionRequest) -> Unit,
    onCancel:   () -> Unit
) {
    var titulo   by remember { mutableStateOf(eval.titulo) }
    var nivel    by remember { mutableStateOf(eval.nivelDificultad) }
    var selCont  by remember { mutableStateOf(contenidos.find { it.contenidoId == eval.contenidoId }) }
    var expanded by remember { mutableStateOf(false) }

    HorizontalDivider(color = DColors.OutlineVariant)
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Editar evaluación", fontSize = 13.sp,
            fontWeight = FontWeight.Medium, color = DColors.Tertiary)

        DTextField(value = titulo, onValueChange = { titulo = it },
            label = "Título", modifier = Modifier.fillMaxWidth())

        Column {
            Text("Contenido", fontSize = 11.sp, color = DColors.OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp))
            Box {
                Surface(shape = RoundedCornerShape(8.dp), color = DColors.SurfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(selCont?.titulo ?: "Selecciona",
                            fontSize = 14.sp, modifier = Modifier.weight(1f),
                            color = if (selCont != null) DColors.OnSurface else DColors.OnSurfaceVariant)
                        Icon(Icons.Rounded.ArrowDropDown, null,
                            tint = DColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                    modifier = Modifier.background(DColors.SurfaceContainerHigh)) {
                    contenidos.forEach { c ->
                        DropdownMenuItem(text = { Text(c.titulo, color = DColors.OnSurface) },
                            onClick = { selCont = c; expanded = false })
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nivel:", fontSize = 12.sp, color = DColors.OnSurfaceVariant)
            (1..5).forEach { lvl ->
                Surface(shape = RoundedCornerShape(6.dp),
                    color = if (nivel == lvl) DColors.Secondary else DColors.SurfaceContainerHigh,
                    modifier = Modifier.size(34.dp).clickable { nivel = lvl }) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$lvl", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (nivel == lvl) DColors.OnSecondary else DColors.OnSurfaceVariant)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.End)) {
            TextButton(onClick = onCancel) { Text("Cancelar", color = DColors.OnSurfaceVariant) }
            Button(
                onClick  = { onSave(UpdateEvaluacionRequest(titulo.trim(), nivel, selCont!!.contenidoId)) },
                enabled  = titulo.isNotBlank() && selCont != null,
                colors   = ButtonDefaults.buttonColors(containerColor = DColors.Secondary),
                shape    = RoundedCornerShape(8.dp)
            ) { Text("Guardar") }
        }
    }
}

@Composable
private fun EvalFilterChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(999.dp),
        color    = if (selected) DColors.Secondary else DColors.SurfaceContainer,
        modifier = Modifier.clickable { onClick() }) {
        Text("$label ($count)", fontSize = 12.sp,
            color    = if (selected) DColors.OnSecondary else DColors.OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}
