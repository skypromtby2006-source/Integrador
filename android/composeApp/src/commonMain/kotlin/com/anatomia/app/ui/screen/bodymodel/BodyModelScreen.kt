package com.anatomia.app.ui.screen.bodymodel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anatomia.app.ui.model.AnatomySystem
import com.anatomia.app.ui.model.BodyModelUiState
import com.anatomia.app.ui.model.OrganPoi
import com.anatomia.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyModelScreen(
    onNavigateToAgent: (organId: String) -> Unit,
    onNavigateToQuiz:  (organId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BodyModelViewModel = viewModel { BodyModelViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeSystem by viewModel.activeSystem.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize().background(AppColors.ScreenBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onNavigateBack = onNavigateBack)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .background(AppColors.ViewerBackground, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                UnityViewerSlot(
                    onOrganSelected = { organId ->
                        viewModel.onOrganSelectedFromUnity(organId)
                    },
                    onPoiSelected = { poiName, organId ->
                        viewModel.onPoiSelected(poiName, organId)
                    },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SystemTabsRow(
                activeSystem = activeSystem,
                onSystemSelected = { viewModel.onSystemSelected(it) },
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Sheet: órgano completo (comportamiento anterior)
        val organFocused = uiState as? BodyModelUiState.OrganFocused
        AnimatedVisibility(
            visible = organFocused != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            if (organFocused != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.SheetBackground,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ) {
                    OrganInfoSheetContent(
                        organ            = organFocused.organ,
                        onLearnWithAgent = { organId -> onNavigateToAgent(organId) },
                        onStartQuiz      = { organId -> onNavigateToQuiz(organId) },
                        onDismiss        = { viewModel.onSheetDismissed() },
                    )
                }
            }
        }

        // Sheet: POI específico con timer de exploración
        val poiFocused = uiState as? BodyModelUiState.PoiFocused
        if (poiFocused != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onPoiSheetDismissed() },
                containerColor   = MaterialTheme.colorScheme.surface,
            ) {
                PoiDetailSheet(
                    poi        = poiFocused.poi,
                    onStartQuiz = {
                        viewModel.onPoiSheetDismissed()
                        onNavigateToQuiz(poiFocused.organ.id)
                    },
                    onDismiss  = { viewModel.onPoiSheetDismissed() },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables privados
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoiDetailSheet(
    poi        : OrganPoi,
    onStartQuiz: () -> Unit,
    onDismiss  : () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: label + topic chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    poi.label,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary,
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        poi.topic,
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }

        // Descripción
        Text(
            poi.description,
            style      = MaterialTheme.typography.bodyLarge,
            color      = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp,
        )

        // Facts
        if (poi.facts.isNotEmpty()) {
            HorizontalDivider()
            Text(
                "Datos clave",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface,
            )
            poi.facts.forEach { fact ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Text("•", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    Text(
                        fact,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Botón quiz
        Button(
            onClick  = onStartQuiz,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Rounded.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Quiz sobre ${poi.label}", fontSize = 15.sp)
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .background(AppColors.SurfaceCard, RoundedCornerShape(10.dp))
                .size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = AppColors.TextMuted,
            )
        }

        Text(
            text = "Cuerpo humano",
            fontSize = 15.sp,
            color = AppColors.TextPrimary,
        )

        IconButton(
            onClick = { /* TODO: abrir filtros */ },
            modifier = Modifier
                .background(AppColors.SurfaceCard, RoundedCornerShape(10.dp))
                .size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = AppColors.TextMuted,
            )
        }
    }
}

@Composable
private fun SystemTabsRow(
    activeSystem: AnatomySystem,
    onSystemSelected: (AnatomySystem) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnatomySystem.entries.forEach { system ->
            SystemTab(
                label    = system.displayName,
                isActive = system == activeSystem,
                onClick  = { onSystemSelected(system) },
            )
        }
    }
}

@Composable
private fun SystemTab(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isActive) AppColors.TabActive    else AppColors.TabInactive
    val textColor      = if (isActive) AppColors.TabTextActive else AppColors.TabTextInactive
    val borderColor    = if (isActive) AppColors.TabBorderActive else AppColors.TabBorderInactive

    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(20.dp),
        color   = containerColor,
        border  = ButtonDefaults.outlinedButtonBorder.copy(width = 0.5.dp).let {
            androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
        },
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = textColor,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
        )
    }
}

/**
 * Slot donde vive el visor de Unity.
 *
 * En commonMain es un placeholder visual.
 * En androidMain (carpeta kotlin/) se hace expect/actual para inyectar
 * el AndroidView real con UnityPlayer.
 *
 * ¿Por qué expect/actual y no simplemente poner el AndroidView aquí?
 * Porque AndroidView es una API de Android. Si lo pusiéramos en commonMain,
 * el módulo Desktop no compilaría — no sabe qué es un Android View.
 */
@Composable
expect fun UnityViewerSlot(
    onOrganSelected: (organId: String) -> Unit,
    onPoiSelected  : (poiName: String, organId: String) -> Unit,
)
