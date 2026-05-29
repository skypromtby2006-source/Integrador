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
import db.ClaseRepository
import db.EstudianteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Clase
import models.EstudianteProgreso

@Composable
fun ClassesScreen(docenteId: String) {
    val scope       = rememberCoroutineScope()
    var classes     by remember { mutableStateOf<List<Clase>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var progressMap by remember { mutableStateOf<Map<String, List<EstudianteProgreso>>>(emptyMap()) }
    var expanded    by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loadError   by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            classes   = withContext(Dispatchers.IO) { ClaseRepository.getAll(docenteId) }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            loadError = "Error al cargar datos: ${e.message}"
            println("[ERROR] Carga fallida: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DColors.Background)
            .padding(24.dp)
    ) {
        Column(Modifier.padding(bottom = 20.dp)) {
            Text("Clases", fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold, color = DColors.OnSurface)
            Text("${classes.size} cursos · 2025-2026",
                fontSize = 13.sp, color = DColors.OnSurfaceVariant)
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
                                isLoading = false
                            } catch (e: Exception) {
                                isLoading = false
                                loadError = "Error al cargar clases: ${e.message}"
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
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(classes, key = { it.claseId }) { cls ->
                    val isExpanded   = cls.claseId in expanded
                    val clsProgress  = progressMap[cls.claseId]
                    val isLoadingPrg = isExpanded && clsProgress == null

                    ClassCard(
                        cls          = cls,
                        isExpanded   = isExpanded,
                        isLoadingPrg = isLoadingPrg,
                        progress     = clsProgress ?: emptyList(),
                        onToggle     = {
                            if (isExpanded) {
                                expanded = expanded - cls.claseId
                            } else {
                                expanded = expanded + cls.claseId
                                if (clsProgress == null) {
                                    scope.launch {
                                        val prg = withContext(Dispatchers.IO) {
                                            EstudianteRepository.getProgresoByClase(cls.claseId)
                                        }
                                        progressMap = progressMap + (cls.claseId to prg)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassCard(
    cls:          Clase,
    isExpanded:   Boolean,
    isLoadingPrg: Boolean,
    progress:     List<EstudianteProgreso>,
    onToggle:     () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = DColors.Primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.School, null,
                            tint = DColors.Primary, modifier = Modifier.size(22.dp))
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(cls.nombre, fontSize = 16.sp,
                        fontWeight = FontWeight.Medium, color = DColors.OnSurface)
                    Text("${cls.grado} · ${cls.turno}", fontSize = 12.sp,
                        color = DColors.OnSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = DColors.SecondaryContainer
                ) {
                    Text(
                        "${cls.totalAlumnos} alumno${if (cls.totalAlumnos != 1) "s" else ""}",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color      = DColors.OnSecondaryContainer,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Icon(
                    if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint     = DColors.OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(color = DColors.OutlineVariant)

                    if (isLoadingPrg) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color       = DColors.Primary
                            )
                        }
                    } else if (progress.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin alumnos en este curso",
                                fontSize = 13.sp, color = DColors.OnSurfaceVariant)
                        }
                    } else {
                        progress.forEachIndexed { idx, prg ->
                            StudentProgressRow(prg)
                            if (idx < progress.lastIndex) {
                                HorizontalDivider(
                                    color    = DColors.OutlineVariant,
                                    modifier = Modifier.padding(start = 56.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentProgressRow(prg: EstudianteProgreso) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape    = RoundedCornerShape(999.dp),
            color    = DColors.Primary.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    prg.nombre.take(1).uppercase(),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = DColors.Primary
                )
            }
        }

        Column(Modifier.weight(1f)) {
            Text("${prg.nombre} ${prg.apellido}", fontSize = 14.sp,
                fontWeight = FontWeight.Medium, color = DColors.OnSurface)
            Text(prg.email, fontSize = 11.sp, color = DColors.OnSurfaceVariant)
        }

        Column(horizontalAlignment = Alignment.End) {
            val pct = if (prg.totalPreguntas > 0)
                (prg.totalCorrectas.toFloat() / prg.totalPreguntas * 100).toInt() else 0

            Text(
                "${prg.totalCorrectas}/${prg.totalPreguntas}",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = when {
                    prg.totalPreguntas == 0 -> DColors.OnSurfaceVariant
                    pct >= 70               -> DColors.Success
                    pct >= 40               -> DColors.Primary
                    else                    -> DColors.Error
                }
            )
            Text(
                if (prg.totalPreguntas == 0) "sin actividad" else "$pct% correcto",
                fontSize = 10.sp,
                color    = DColors.OnSurfaceVariant
            )
        }
    }
}
