package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import db.PreguntaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.ContenidoBiologico
import models.CreateOpcionRequest
import models.CreatePreguntaRequest
import models.Pregunta
import models.UpdatePreguntaRequest

@Composable
fun QuestionsScreen(docenteId: String) {
    val scope      = rememberCoroutineScope()
    var preguntas  by remember { mutableStateOf<List<Pregunta>>(emptyList()) }
    var contenidos by remember { mutableStateOf<List<ContenidoBiologico>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var showForm    by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf<String?>(null) }
    var search      by remember { mutableStateOf("") }
    var filterNivel by remember { mutableStateOf<Int?>(null) }
    var filterCont  by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        contenidos = withContext(Dispatchers.IO) { ContenidoRepository.getAll() }
        preguntas  = withContext(Dispatchers.IO) { PreguntaRepository.getAll() }
        isLoading  = false
    }

    val visible = preguntas.filter { q ->
        (search.isBlank() ||
         q.enunciado.contains(search, ignoreCase = true) ||
         q.subtema.contains(search, ignoreCase = true) ||
         q.tituloContenido.contains(search, ignoreCase = true)) &&
        (filterNivel == null || q.nivelDificultad == filterNivel) &&
        (filterCont  == null || q.contenidoId     == filterCont)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DColors.Background)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Banco de preguntas", fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold, color = DColors.OnSurface)
                Text("${visible.size} de ${preguntas.size} pregunta${if (preguntas.size != 1) "s" else ""}",
                    fontSize = 13.sp, color = DColors.OnSurfaceVariant)
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            PdfExporter.exportPreguntas(visible,
                                if (search.isNotBlank()) "Búsqueda: $search" else "")
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
                colors  = ButtonDefaults.buttonColors(containerColor = DColors.Tertiary),
                shape   = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(if (showForm) Icons.Rounded.Close else Icons.Rounded.Add,
                    null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (showForm) "Cancelar" else "Nueva pregunta", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        DTextField(value = search, onValueChange = { search = it },
            label = "Buscar por enunciado, subtema o contenido",
            modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                QFilterChip(label = "Todos los órganos", selected = filterCont == null,
                    onClick = { filterCont = null })
            }
            items(contenidos) { cont ->
                QFilterChip(label = cont.titulo.take(20),
                    selected = filterCont == cont.contenidoId,
                    onClick  = { filterCont = if (filterCont == cont.contenidoId) null else cont.contenidoId })
            }
        }

        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(visible = showForm) {
            CreateQuestionForm(
                contenidos = contenidos,
                docenteId  = docenteId,
                errorMsg   = errorMsg,
                onCreated  = { req ->
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) { PreguntaRepository.create(req) }
                            preguntas = withContext(Dispatchers.IO) { PreguntaRepository.getAll() }
                            showForm  = false
                            errorMsg  = null
                        } catch (e: Exception) {
                            errorMsg = "Error: ${e.message}"
                        }
                    }
                }
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DColors.Tertiary)
            }
        } else if (visible.isEmpty()) {
            EmptyState("❓", "Sin preguntas",
                if (search.isNotBlank() || filterCont != null) "Prueba con otros filtros"
                else "Agrega la primera pregunta con el botón de arriba")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.preguntaId }) { q ->
                    QuestionCard(
                        pregunta   = q,
                        contenidos = contenidos,
                        onDelete   = {
                            scope.launch {
                                withContext(Dispatchers.IO) { PreguntaRepository.delete(q.preguntaId) }
                                preguntas = withContext(Dispatchers.IO) { PreguntaRepository.getAll() }
                            }
                        },
                        onUpdate   = { req ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    PreguntaRepository.update(
                                        preguntaId      = q.preguntaId,
                                        enunciado       = req.enunciado,
                                        subtema         = req.subtema,
                                        nivelDificultad = req.nivelDificultad,
                                        contenidoId     = req.contenidoId,
                                        opciones        = req.opciones
                                    )
                                }
                                preguntas = withContext(Dispatchers.IO) { PreguntaRepository.getAll() }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateQuestionForm(
    contenidos: List<ContenidoBiologico>,
    docenteId:  String,
    errorMsg:   String?,
    onCreated:  (CreatePreguntaRequest) -> Unit
) {
    var subtema          by remember { mutableStateOf("") }
    var enunciado        by remember { mutableStateOf("") }
    var opt0             by remember { mutableStateOf("") }
    var opt1             by remember { mutableStateOf("") }
    var opt2             by remember { mutableStateOf("") }
    var opt3             by remember { mutableStateOf("") }
    var correctIndex     by remember { mutableStateOf(0) }
    var nivelDificultad  by remember { mutableStateOf(1) }
    var selectedContenido by remember { mutableStateOf<ContenidoBiologico?>(null) }
    var contenidoExpanded by remember { mutableStateOf(false) }

    val opts       = listOf(opt0, opt1, opt2, opt3)
    val optSetters = listOf<(String) -> Unit>({ opt0 = it }, { opt1 = it }, { opt2 = it }, { opt3 = it })
    val letters    = listOf("A", "B", "C", "D")
    val diffLabels = listOf("Fácil", "Medio", "Difícil")

    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nueva pregunta", fontWeight = FontWeight.Medium, color = DColors.OnSurface)

            if (contenidos.isEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp),
                    color = DColors.TertiaryContainer,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("No hay contenido biológico disponible. Crea contenido primero.",
                        fontSize = 13.sp, color = DColors.OnTertiaryContainer,
                        modifier = Modifier.padding(12.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Selector de contenido
                Column(Modifier.weight(2f)) {
                    Text("Contenido biológico", fontSize = 11.sp, color = DColors.OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        Surface(
                            shape    = RoundedCornerShape(8.dp),
                            color    = if (selectedContenido != null) DColors.TertiaryContainer
                                       else DColors.SurfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth().clickable { contenidoExpanded = true }
                        ) {
                            Text(
                                selectedContenido?.titulo ?: "Seleccionar",
                                fontSize = 13.sp,
                                color    = if (selectedContenido != null) DColors.OnTertiaryContainer
                                           else DColors.OnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                            )
                        }
                        DropdownMenu(expanded = contenidoExpanded,
                            onDismissRequest = { contenidoExpanded = false }) {
                            contenidos.forEach { c ->
                                DropdownMenuItem(
                                    text    = { Text(c.titulo) },
                                    onClick = { selectedContenido = c; contenidoExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Dificultad
                Column(Modifier.weight(1f)) {
                    Text("Dificultad", fontSize = 11.sp, color = DColors.OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        diffLabels.forEachIndexed { i, label ->
                            val active = nivelDificultad == i + 1
                            Surface(
                                shape    = RoundedCornerShape(6.dp),
                                color    = if (active) DColors.Primary else DColors.SurfaceContainerHigh,
                                modifier = Modifier.clickable { nivelDificultad = i + 1 }.weight(1f)
                            ) {
                                Text(label, fontSize = 11.sp,
                                    color    = if (active) DColors.OnPrimary else DColors.OnSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            DTextField(value = subtema, onValueChange = { subtema = it },
                label = "Subtema (ej: Cámaras del corazón)", modifier = Modifier.fillMaxWidth())

            DTextField(value = enunciado, onValueChange = { enunciado = it },
                label = "Texto de la pregunta", modifier = Modifier.fillMaxWidth(), minLines = 2)

            Text("Opciones (marca la correcta →)", fontSize = 12.sp, color = DColors.OnSurfaceVariant)
            opts.forEachIndexed { i, opt ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioButton(
                        selected = correctIndex == i,
                        onClick  = { correctIndex = i },
                        colors   = RadioButtonDefaults.colors(selectedColor = DColors.Success)
                    )
                    Surface(
                        shape    = RoundedCornerShape(6.dp),
                        color    = if (correctIndex == i) DColors.SuccessContainer
                                   else DColors.SurfaceContainerHigh,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(letters[i], fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = if (correctIndex == i) DColors.OnSuccessContainer
                                        else DColors.OnSurfaceVariant)
                        }
                    }
                    DTextField(value = opt, onValueChange = optSetters[i],
                        label = "Opción ${letters[i]}", modifier = Modifier.weight(1f))
                }
            }

            errorMsg?.let { Text(it, color = DColors.Error, fontSize = 13.sp) }

            Button(
                onClick = {
                    onCreated(CreatePreguntaRequest(
                        contenidoId     = selectedContenido!!.contenidoId,
                        enunciado       = enunciado.trim(),
                        subtema         = subtema.trim(),
                        nivelDificultad = nivelDificultad,
                        docenteId       = docenteId,
                        opciones        = opts.mapIndexed { i, texto ->
                            CreateOpcionRequest(
                                letra      = letters[i],
                                texto      = texto.trim(),
                                esCorrecta = correctIndex == i
                            )
                        }
                    ))
                },
                enabled  = enunciado.isNotBlank() && opts.all { it.isNotBlank() } &&
                           selectedContenido != null,
                colors   = ButtonDefaults.buttonColors(containerColor = DColors.Tertiary),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Guardar pregunta") }
        }
    }
}

@Composable
private fun QuestionCard(
    pregunta:   Pregunta,
    contenidos: List<ContenidoBiologico>,
    onDelete:   () -> Unit,
    onUpdate:   (UpdatePreguntaRequest) -> Unit
) {
    var expanded      by remember { mutableStateOf(false) }
    var showEdit      by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val diffColors    = listOf(DColors.SuccessContainer to DColors.OnSuccessContainer,
                               DColors.PrimaryContainer  to DColors.OnPrimaryContainer,
                               DColors.TertiaryContainer to DColors.OnTertiaryContainer)
    val dc            = diffColors.getOrElse(pregunta.nivelDificultad - 1) { diffColors[0] }
    val diffLabels    = listOf("Fácil", "Medio", "Difícil")

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Column(
                Modifier.padding(14.dp).clickable { expanded = !expanded }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                    if (pregunta.tituloContenido.isNotBlank()) {
                        Chip(label = pregunta.tituloContenido,
                            color     = DColors.PrimaryContainer,
                            textColor = DColors.OnPrimaryContainer)
                    }
                    if (pregunta.subtema.isNotBlank()) {
                        Chip(label = pregunta.subtema,
                            color     = DColors.TertiaryContainer,
                            textColor = DColors.OnTertiaryContainer)
                    }
                    Chip(label = diffLabels.getOrElse(pregunta.nivelDificultad - 1) { "Fácil" },
                        color = dc.first, textColor = dc.second)
                    Spacer(Modifier.weight(1f))

                    if (confirmDelete) {
                        TextButton(onClick = { confirmDelete = false }) {
                            Text("Cancelar", color = DColors.OnSurfaceVariant, fontSize = 12.sp)
                        }
                        TextButton(onClick = { onDelete(); confirmDelete = false }) {
                            Text("Eliminar", color = DColors.Error, fontSize = 12.sp)
                        }
                    } else {
                        IconButton(onClick = { showEdit = !showEdit; expanded = false },
                            modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Edit, null,
                                tint = DColors.Tertiary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { confirmDelete = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.DeleteOutline, null,
                                tint = DColors.OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(pregunta.enunciado, fontSize = 15.sp, color = DColors.OnSurface,
                    fontWeight = FontWeight.Medium)

                AnimatedVisibility(visible = expanded) {
                    Column(Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pregunta.opciones.forEach { opcion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (opcion.esCorrecta) DColors.SuccessContainer
                                        else DColors.SurfaceContainerHigh
                            ) {
                                Row(Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(opcion.letra, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = if (opcion.esCorrecta) DColors.OnSuccessContainer
                                                else DColors.OnSurfaceVariant)
                                    Text(opcion.texto, fontSize = 14.sp,
                                        color = if (opcion.esCorrecta) DColors.OnSuccessContainer
                                                else DColors.OnSurface)
                                    if (opcion.esCorrecta) {
                                        Spacer(Modifier.weight(1f))
                                        Icon(Icons.Rounded.Check, null, tint = DColors.Success,
                                            modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showEdit) {
                EditPreguntaForm(
                    pregunta   = pregunta,
                    contenidos = contenidos,
                    onSave     = { req ->
                        onUpdate(req)
                        showEdit = false
                    },
                    onCancel   = { showEdit = false }
                )
            }
        }
    }
}

@Composable
private fun EditPreguntaForm(
    pregunta:   Pregunta,
    contenidos: List<ContenidoBiologico>,
    onSave:     (UpdatePreguntaRequest) -> Unit,
    onCancel:   () -> Unit
) {
    var enunciado       by remember { mutableStateOf(pregunta.enunciado) }
    var subtema         by remember { mutableStateOf(pregunta.subtema) }
    var nivelDificultad by remember { mutableStateOf(pregunta.nivelDificultad) }
    var selectedCont    by remember {
        mutableStateOf(contenidos.find { it.contenidoId == pregunta.contenidoId })
    }
    var expanded        by remember { mutableStateOf(false) }

    val opcionStates = remember {
        androidx.compose.runtime.mutableStateListOf(*pregunta.opciones.map {
            Triple(it.letra, mutableStateOf(it.texto), mutableStateOf(it.esCorrecta))
        }.toTypedArray())
    }
    var correctaIdx by remember {
        mutableStateOf(pregunta.opciones.indexOfFirst { it.esCorrecta }.coerceAtLeast(0))
    }

    HorizontalDivider(color = DColors.OutlineVariant)
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Editar pregunta", fontSize = 13.sp,
            fontWeight = FontWeight.Medium, color = DColors.Tertiary)

        Column {
            Text("Contenido biológico", fontSize = 11.sp,
                color = DColors.OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            Box {
                Surface(shape = RoundedCornerShape(8.dp),
                    color = DColors.SurfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCont?.titulo ?: "Selecciona contenido",
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
                    contenidos.forEach { cont ->
                        DropdownMenuItem(
                            text = { Text(cont.titulo, color = DColors.OnSurface) },
                            onClick = { selectedCont = cont; expanded = false }
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DTextField(value = subtema, onValueChange = { subtema = it },
                label = "Subtema", modifier = Modifier.weight(1f))

            Column(Modifier.weight(1f)) {
                Text("Dificultad", fontSize = 11.sp, color = DColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { lvl ->
                        val active = nivelDificultad == lvl
                        Surface(shape = RoundedCornerShape(6.dp),
                            color = if (active) DColors.Tertiary
                                    else DColors.SurfaceContainerHigh,
                            modifier = Modifier.weight(1f).clickable { nivelDificultad = lvl }) {
                            Text("$lvl", fontSize = 11.sp,
                                color = if (active) DColors.OnTertiary
                                        else DColors.OnSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }
                }
            }
        }

        DTextField(value = enunciado, onValueChange = { enunciado = it },
            label = "Enunciado de la pregunta",
            modifier = Modifier.fillMaxWidth(), minLines = 2)

        Text("Opciones", fontSize = 11.sp, color = DColors.OnSurfaceVariant)
        opcionStates.forEachIndexed { i, (letra, textoState, _) ->
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(selected = correctaIdx == i,
                    onClick = { correctaIdx = i },
                    colors = RadioButtonDefaults.colors(selectedColor = DColors.Success))
                Surface(shape = RoundedCornerShape(6.dp),
                    color = if (correctaIdx == i) DColors.SuccessContainer
                            else DColors.SurfaceContainerHigh,
                    modifier = Modifier.size(28.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(letra, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (correctaIdx == i) DColors.OnSuccessContainer
                                    else DColors.OnSurfaceVariant)
                    }
                }
                DTextField(value = textoState.value,
                    onValueChange = { textoState.value = it },
                    label = "Opción $letra",
                    modifier = Modifier.weight(1f))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.End)) {
            TextButton(onClick = onCancel) {
                Text("Cancelar", color = DColors.OnSurfaceVariant)
            }
            Button(
                onClick = {
                    onSave(UpdatePreguntaRequest(
                        enunciado       = enunciado.trim(),
                        subtema         = subtema.trim(),
                        nivelDificultad = nivelDificultad,
                        contenidoId     = selectedCont!!.contenidoId,
                        opciones        = opcionStates.mapIndexed { i, (letra, textoState, _) ->
                            CreateOpcionRequest(
                                letra      = letra,
                                texto      = textoState.value.trim(),
                                esCorrecta = i == correctaIdx
                            )
                        }
                    ))
                },
                enabled = enunciado.isNotBlank() && selectedCont != null &&
                          opcionStates.all { it.second.value.isNotBlank() },
                colors  = ButtonDefaults.buttonColors(containerColor = DColors.Tertiary),
                shape   = RoundedCornerShape(8.dp)
            ) { Text("Guardar cambios") }
        }
    }
}

@Composable
private fun QFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(999.dp),
        color    = if (selected) DColors.Tertiary else DColors.SurfaceContainer,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(label, fontSize = 12.sp,
            color    = if (selected) DColors.OnTertiary else DColors.OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}
