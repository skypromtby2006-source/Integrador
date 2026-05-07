package com.anatomia.app.ui.screen.bodymodel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anatomia.app.ui.model.AnatomySystem
import com.anatomia.app.ui.model.BodyModelUiState
import com.anatomia.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyModelScreen(
    onNavigateToAgent: (organId: String) -> Unit,
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
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SystemTabsRow(
                activeSystem = activeSystem,
                onSystemSelected = { viewModel.onSystemSelected(it) },
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        val focusedState = uiState as? BodyModelUiState.OrganFocused
        AnimatedVisibility(
            visible = focusedState != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            if (focusedState != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.SheetBackground,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ) {
                    OrganInfoSheetContent(
                        organ = focusedState.organ,
                        onLearnWithAgent = { organId -> onNavigateToAgent(organId) },
                        onDismiss = { viewModel.onSheetDismissed() },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables privados
// ─────────────────────────────────────────────────────────────────────────────

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
                label = system.displayName,
                isActive = system == activeSystem,
                onClick = { onSystemSelected(system) },
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
    val containerColor = if (isActive) AppColors.TabActive else AppColors.TabInactive
    val textColor = if (isActive) AppColors.TabTextActive else AppColors.TabTextInactive
    val borderColor = if (isActive) AppColors.TabBorderActive else AppColors.TabBorderInactive

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 0.5.dp,
        ).let {
            androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
        },
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor,
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
expect fun UnityViewerSlot(onOrganSelected: (organId: String) -> Unit)