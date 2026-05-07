package com.anatomia.app.ui.screen.bodymodel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anatomia.app.ui.theme.AppColors

/**
 * Implementación Android del slot del visor de Unity.
 *
 * ESTADO ACTUAL: Placeholder visual mientras integramos Unity as a Library (UaaL).
 * Muestra un recuadro oscuro con instrucciones para que la pantalla sea funcional
 * desde ya, sin bloquear el desarrollo del resto de la UI.
 *
 * CUANDO TENGAMOS UNITY INTEGRADO, este archivo se reemplaza con:
 *
 * ```kotlin
 * @Composable
 * actual fun UnityViewerSlot(onOrganSelected: (organId: String) -> Unit) {
 *     val context = LocalContext.current
 *     AndroidView(
 *         factory = { ctx ->
 *             UnityPlayerCustomFrameLayout(ctx).apply {
 *                 // Registrar callback para eventos de Unity
 *                 UnityBridge.setOrganSelectedListener { organId ->
 *                     onOrganSelected(organId)
 *                 }
 *             }
 *         },
 *         modifier = Modifier.fillMaxSize(),
 *         update = { /* Unity maneja su propio ciclo de vida */ }
 *     )
 * }
 * ```
 *
 * ¿Por qué el callback onOrganSelected vive aquí y no en el ViewModel?
 * Porque AndroidView necesita un contexto Android para existir. El ViewModel
 * no debe saber nada de Android — eso lo hace testeable en JVM puro.
 * El slot recibe el callback y el ViewModel recibe el resultado.
 */
@Composable
actual fun UnityViewerSlot(onOrganSelected: (organId: String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Placeholder hasta integrar UaaL
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Modelo 3D Unity",
                fontSize = 14.sp,
                color = AppColors.TextMuted,
            )
            Text(
                text = "Pendiente: Unity as a Library",
                fontSize = 11.sp,
                color = AppColors.TextHint,
            )
        }

        // Botones de prueba en parte superior — SOLO para desarrollo.
        // Simulan el evento que Unity dispararía al tocar un órgano.
        // Se eliminarán cuando Unity esté integrado.
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .background(AppColors.SurfaceCard, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                "heart" to "Corazón",
                "lungs" to "Pulmones",
                "kidneys" to "Riñones",
            ).forEach { (id, label) ->
                androidx.compose.material3.Button(
                    onClick = {
                        android.util.Log.d("ANATOMIA", "Botón tocado: $id")
                        onOrganSelected(id)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryButton,
                        contentColor = AppColors.PrimaryButtonText,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(text = label, fontSize = 12.sp)
                }
            }
        }
    }
}
