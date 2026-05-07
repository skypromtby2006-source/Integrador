package com.anatomia.app.ui.screen.bodymodel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anatomia.app.ui.model.OrganFact
import com.anatomia.app.ui.model.OrganUiModel
import com.anatomia.app.ui.theme.AppColors

/**
 * Contenido interno del bottom sheet: header del órgano + grilla de datos clínicos.
 *
 * Es un @Composable separado del sheet en sí por una razón importante:
 * nos permite hacer preview de este contenido en Android Studio sin necesitar
 * el scaffold completo. También facilita los tests de UI unitarios.
 *
 * @param organ El órgano actualmente seleccionado.
 * @param onLearnWithAgent Callback disparado cuando el estudiante toca "Aprender".
 *   Lleva el organId para que la pantalla del agente sepa el contexto.
 * @param onDismiss Callback para cerrar el sheet desde el botón de flecha.
 */
@Composable
fun OrganInfoSheetContent(
    organ: OrganUiModel,
    onLearnWithAgent: (organId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        // Handle visual del sheet
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 10.dp)
                .width(36.dp)
                .height(4.dp)
                .background(
                    color = AppColors.SheetHandle,
                    shape = RoundedCornerShape(2.dp),
                ),
        )

        // Header: nombre + badge de sistema
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organ.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = organ.systemName,
                    fontSize = 12.sp,
                    color = AppColors.TextMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            SystemBadge(label = "Seleccionado")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grilla 2x2 de datos clínicos
        // Usamos LazyVerticalGrid solo cuando hay muchos items. Con 4 fijos,
        // un simple Column+Row es más predecible dentro de un BottomSheet.
        val chunked = organ.facts.chunked(2)
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { fact ->
                    FactCard(
                        fact = fact,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Si la fila tiene solo 1 item, rellenamos el espacio
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Descripción breve
        Text(
            text = organ.description,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = AppColors.TextMuted,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onLearnWithAgent(organ.id) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryButton,
                    contentColor = AppColors.PrimaryButtonText,
                ),
            ) {
                Text(
                    text = "Aprender con el agente",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .background(
                        color = AppColors.SurfaceCard,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Cerrar panel",
                    tint = AppColors.TextMuted,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Tarjeta individual de un dato clínico.
 *
 * La separamos en su propio @Composable porque:
 * 1. Es fácil de hacer preview y testear en aislamiento.
 * 2. Si en el futuro queremos animarla al aparecer, solo tocamos este componente.
 */
@Composable
private fun FactCard(
    fact: OrganFact,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = AppColors.SurfaceCard,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        // Ícono usando Material Symbols (nombre como string → Icon lookup)
        Text(
            text = fact.label.uppercase(),
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            color = AppColors.TextMuted,
        )
        Text(
            text = fact.value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = fact.alertNote,
            fontSize = 10.sp,
            color = AppColors.TextHint,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}

/**
 * Badge de sistema activo ("Seleccionado").
 * Pequeño pero importante: le da feedback visual al estudiante
 * de que su toque en el modelo 3D fue registrado.
 */
@Composable
private fun SystemBadge(label: String) {
    Text(
        text = label,
        fontSize = 11.sp,
        color = AppColors.BadgeText,
        modifier = Modifier
            .background(
                color = AppColors.BadgeBackground,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
