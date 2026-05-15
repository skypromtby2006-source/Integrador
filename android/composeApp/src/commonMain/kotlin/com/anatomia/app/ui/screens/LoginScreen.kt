package com.anatomia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.*

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("ana@escuela.mx") }
    var password by remember { mutableStateOf("anatomia2026") }
    var showPassword by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Blob mint — top-left
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-40).dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Mint.copy(alpha = 0.30f), Color.Transparent)
                    )
                )
        )
        // Blob magenta — bottom-right
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = (-180).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Magenta.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand mark
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Primary, Color(0xFF5A6FA0)),
                                start = androidx.compose.ui.geometry.Offset(0f, 80f),
                                end = androidx.compose.ui.geometry.Offset(80f, 0f),
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(42.dp)) {
                        val w = size.width
                        val h = size.height
                        val sx = w / 40f
                        val sy = h / 40f

                        // Heart outline
                        val heartPath = Path().apply {
                            moveTo(20 * sx, 33 * sy)
                            cubicTo(20 * sx, 33 * sy, 9 * sx, 26.4f * sy, 9 * sx, 17.8f * sy)
                            cubicTo(9 * sx, 13 * sy, 12.6f * sx, 10 * sy, 16.4f * sx, 10 * sy)
                            cubicTo(18.6f * sx, 10 * sy, 20.1f * sx, 11 * sy, 21 * sx, 12.4f * sy)
                            cubicTo(22 * sx, 11 * sy, 23.5f * sx, 10 * sy, 25.6f * sx, 10 * sy)
                            cubicTo(29.4f * sx, 10 * sy, 33 * sx, 13 * sy, 33 * sx, 17.8f * sy)
                            cubicTo(33 * sx, 26.4f * sy, 22 * sx, 33 * sy, 20 * sx, 33 * sy)
                            close()
                        }
                        drawPath(heartPath, color = Color.White.copy(alpha = 0.22f))
                        drawPath(heartPath, color = Color.White, style = Stroke(width = 1.6f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // EKG line
                        val ekgPath = Path().apply {
                            moveTo(6 * sx, 22 * sy)
                            lineTo(12 * sx, 22 * sy)
                            lineTo(14 * sx, 18 * sy)
                            lineTo(17 * sx, 29 * sy)
                            lineTo(20 * sx, 20 * sy)
                            lineTo(22 * sx, 26 * sy)
                            lineTo(34 * sx, 26 * sy)
                        }
                        drawPath(ekgPath, color = Mint, style = Stroke(width = 1.8f * sx, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = buildAnnotatedString {
                    append("didact")
                    withStyle(SpanStyle(color = Tertiary)) { append("ai") }
                },
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                color = OnSurface,
            )
            Text(
                text = "TU CUERPO · EXPLORADO",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(28.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Forgot password
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = {}) {
                    Text("¿Olvidaste tu contraseña?", color = Primary)
                }
            }

            Spacer(Modifier.height(4.dp))

            // Entrar button
            Button(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
            ) {
                Text("Entrar", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            // Divider "o continúa con"
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                Text(
                    "o continúa con",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
            }

            // Google button
            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                GoogleIcon()
                Spacer(Modifier.width(8.dp))
                Text("Continuar con Google")
            }

            Spacer(Modifier.height(12.dp))

            // Class code button
            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("#", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = OnTertiaryContainer)
                }
                Spacer(Modifier.width(8.dp))
                Text("Usar código de clase")
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(20.dp))

            // Footer
            Text(
                text = buildAnnotatedString {
                    append("¿nueva por aquí? ")
                    withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.Medium)) {
                        append("crear cuenta")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GoogleIcon() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(18.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val r = size.width * 0.44f
        drawArc(color = Color(0xFF4285F4), startAngle = -30f, sweepAngle = 120f, useCenter = false, style = Stroke(width = size.width * 0.18f))
        drawArc(color = Color(0xFF34A853), startAngle = 90f, sweepAngle = 120f, useCenter = false, style = Stroke(width = size.width * 0.18f))
        drawArc(color = Color(0xFFFBBC05), startAngle = 210f, sweepAngle = 60f, useCenter = false, style = Stroke(width = size.width * 0.18f))
        drawArc(color = Color(0xFFEA4335), startAngle = 270f, sweepAngle = 90f, useCenter = false, style = Stroke(width = size.width * 0.18f))
    }
}
