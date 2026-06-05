package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.AuthRepository
import db.TotpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.DocenteSession

@Composable
fun PantallaCodigo2FA(
    usuarioId: String,
    onSuccess: (DocenteSession) -> Unit,
    onVolver:  () -> Unit
) {
    val scope        = rememberCoroutineScope()
    var codigo       by remember { mutableStateOf("") }
    var error        by remember { mutableStateOf<String?>(null) }
    var loading      by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier.fillMaxSize().background(DColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape    = RoundedCornerShape(20.dp),
            color    = DColors.SurfaceContainer,
            modifier = Modifier.width(360.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                        .background(DColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Lock, null,
                        tint = DColors.Mint, modifier = Modifier.size(28.dp))
                }

                Text("Verificación en dos pasos",
                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    color = DColors.OnSurface, textAlign = TextAlign.Center)

                Text(
                    "Abre Google Authenticator e ingresa\nel código de 6 dígitos para Didactai.",
                    fontSize = 13.sp, color = DColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                BasicTextField(
                    value         = codigo,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) codigo = it },
                    textStyle     = TextStyle(
                        color     = DColors.OnSurface,
                        fontSize  = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp
                    ),
                    cursorBrush   = SolidColor(DColors.Primary),
                    singleLine    = true,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DColors.SurfaceContainerHigh)
                        .padding(vertical = 14.dp)
                        .focusRequester(focusRequester)
                )

                error?.let {
                    Text(it, color = DColors.Error, fontSize = 13.sp,
                        textAlign = TextAlign.Center)
                }

                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error   = null
                            val valido = withContext(Dispatchers.IO) {
                                TotpRepository.verificarCodigo(usuarioId, codigo)
                            }
                            if (valido) {
                                val session = withContext(Dispatchers.IO) {
                                    AuthRepository.obtenerPorId(usuarioId)
                                }
                                if (session != null) onSuccess(session)
                                else error = "Error al obtener datos del usuario"
                            } else {
                                error   = "Código incorrecto. Intenta de nuevo."
                                codigo  = ""
                            }
                            loading = false
                        }
                    },
                    enabled  = codigo.length == 6 && !loading,
                    colors   = ButtonDefaults.buttonColors(containerColor = DColors.Primary),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (loading) CircularProgressIndicator(
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = DColors.OnPrimary)
                    else Text("Verificar", fontSize = 15.sp)
                }

                TextButton(
                    onClick  = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al login", color = DColors.OnSurfaceVariant, fontSize = 13.sp)
                }
            }
        }
    }
}
