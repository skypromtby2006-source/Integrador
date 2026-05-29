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
import db.SesionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.Clase
import models.SesionResumenEstudiante

@Composable
fun SessionProgressScreen(docenteId: String) {
    val scope       = rememberCoroutineScope()
    var clases      by remember { mutableStateOf<List<Clase>>(emptyList()) }
    var selectedCls by remember { mutableStateOf<Clase?>(null) }
    var resumen     by remember { mutableStateOf<List<SesionResumenEstudiante>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var loadingData by remember { mutableStateOf(false) }
    var loadError   by remember { mutableStateOf<String?>(null) }
    var expanded    by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        try {
            clases    = withContext(Dispatchers.IO) { ClaseRepository.getAll(docenteId) }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            loadError = "Error al cargar clases: ${e.message}"
        }
    }

    LaunchedEffect(selectedCls) {
        val cls = selectedCls ?: return@LaunchedEffect
        loadingData = true
        expanded    = emptySet()
        try {
            resumen = withContext(Dispatchers.IO) {
                SesionRepository.getResumenByClase(cls.claseId)
            }
        } catch (e: Exception) {
            loadError = "Error al cargar sesiones: ${e.message}"
        }
        loadingData = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DColors.Background).padding(24.dp)
    ) {
        // ── Encabezado ────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Progreso por Sesión", fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold, color = DColors.OnSurface)
                Text("Quizzes libres completados por los alumnos",
                    fontSize = 13.sp, color = DColors.OnSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Selector de clase ─────────────────────────────────────────────────
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DColors.Primary)
            }
            return@Column
        }

        loadError?.let { msg ->
            Surface(shape = RoundedCornerShape(10.dp), color = DColors.ErrorContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Warning, null, tint = DColors.Error,
                        modifier = Modifier.size(18.dp))
                    Text(msg, fontSize = 13.sp, color = DColors.OnErrorContainer,
                        modifier = Modifier.weight(1f))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()) {
            Text("Clase:", fontSize = 13.sp, color = DColors.OnSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically))
            clases.forEach { cls ->
                Surface(
                    shape    = RoundedCornerShape(999.dp),
                    color    = if (selectedCls?.claseId == cls.claseId)
                                   DColors.Primary else DColors.SurfaceContainer,
                    modifier = Modifier.clickable { selectedCls = cls }
                ) {
                    Text("${cls.nombre} · ${cls.grado}",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = if (selectedCls?.claseId == cls.claseId)
                                    DColors.OnPrimary else DColors.OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Contenido ─────────────────────────────────────────────────────────
        when {
            selectedCls == null -> {
                EmptyState("📊", "Selecciona una clase",
                    "Elige una clase para ver el progreso por sesión de sus alumnos")
            }
            loadingData -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DColors.Primary)
                }
            }
            resumen.isEmpty() -> {
                EmptyState("📭", "Sin sesiones registradas",
                    "Los alumnos de esta clase aún no han completado quizzes desde la app")
            }
            else -> {
                val totalSesiones = resumen.sumOf { it.sesiones.size }
                Text("${resumen.size} alumnos · $totalSesiones sesiones registradas",
                    fontSize = 12.sp, color = DColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(resumen, key = { it.usuarioId }) { est ->
                        StudentSessionCard(
                            est      = est,
                            expanded = est.usuarioId in expanded,
                            onToggle = {
                                expanded = if (est.usuarioId in expanded)
                                    expanded - est.usuarioId
                                else
                                    expanded + est.usuarioId
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentSessionCard(
    est:      SesionResumenEstudiante,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val totalSesiones = est.sesiones.size
    val promedioTotal = if (totalSesiones > 0)
        est.sesiones.map { it.puntajeTotal }.average() else 0.0
    val promedioColor = when {
        promedioTotal >= 70 -> DColors.Success
        promedioTotal >= 40 -> DColors.Primary
        else                -> DColors.Error
    }

    Surface(shape = RoundedCornerShape(12.dp), color = DColors.SurfaceContainer,
        modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = RoundedCornerShape(999.dp),
                    color = DColors.Primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(est.nombre.take(1).uppercase(), fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, color = DColors.Primary)
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text("${est.nombre} ${est.apellido}", fontSize = 14.sp,
                        fontWeight = FontWeight.Medium, color = DColors.OnSurface)
                    Text("${totalSesiones} sesión${if (totalSesiones != 1) "es" else ""}",
                        fontSize = 11.sp, color = DColors.OnSurfaceVariant)
                }

                if (totalSesiones > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${promedioTotal.toInt()}%", fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, color = promedioColor)
                        Text("promedio", fontSize = 10.sp, color = DColors.OnSurfaceVariant)
                    }
                }

                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    null, tint = DColors.OnSurfaceVariant, modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded && totalSesiones > 0) {
                Column {
                    HorizontalDivider(color = DColors.OutlineVariant)

                    Row(
                        Modifier.fillMaxWidth()
                            .background(DColors.SurfaceContainerHigh)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Fecha", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                            color = DColors.OnSurfaceVariant, modifier = Modifier.weight(2f))
                        Text("Órgano", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                            color = DColors.OnSurfaceVariant, modifier = Modifier.weight(2f))
                        Text("Resultado", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                            color = DColors.OnSurfaceVariant, modifier = Modifier.weight(1.5f))
                        Text("%", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                            color = DColors.OnSurfaceVariant, modifier = Modifier.width(40.dp))
                    }

                    est.sesiones.forEachIndexed { idx, sesion ->
                        val pct = sesion.puntajeTotal.toInt()
                        val pctColor = when {
                            pct >= 70 -> DColors.Success
                            pct >= 40 -> DColors.Primary
                            else      -> DColors.Error
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(sesion.realizadoEn.take(10),
                                fontSize = 12.sp, color = DColors.OnSurfaceVariant,
                                modifier = Modifier.weight(2f))
                            Text(organLabel(sesion.organId),
                                fontSize = 12.sp, color = DColors.OnSurface,
                                modifier = Modifier.weight(2f))
                            Text("✓${sesion.correctas}  ✗${sesion.incorrectas}",
                                fontSize = 12.sp, color = DColors.OnSurfaceVariant,
                                modifier = Modifier.weight(1.5f))
                            Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = pctColor, modifier = Modifier.width(40.dp))
                        }
                        if (idx < est.sesiones.lastIndex)
                            HorizontalDivider(color = DColors.OutlineVariant,
                                modifier = Modifier.padding(start = 14.dp))
                    }
                }
            }
        }
    }
}

private fun organLabel(organId: String): String = when (organId) {
    "heart"   -> "Corazón"
    "lungs"   -> "Pulmones"
    "kidneys" -> "Riñones"
    else      -> organId.replaceFirstChar { it.uppercase() }
}
