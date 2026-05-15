package com.anatomia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha as drawAlpha
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.theme.*

private data class QuizQuestion(
    val topic: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
)

private val quizQuestions = listOf(
    QuizQuestion("Corazón · Cámaras", "¿Cuántas cámaras tiene el corazón humano?",
        listOf("2", "3", "4", "5"), 2),
    QuizQuestion("Corazón · Flujo", "La sangre sale del corazón hacia el cuerpo por…",
        listOf("Vena cava", "Aorta", "Arteria pulmonar", "Vena pulmonar"), 1),
    QuizQuestion("Corazón · Cámaras", "¿Cuál cámara bombea sangre oxigenada al resto del cuerpo?",
        listOf("Aurícula izquierda", "Ventrículo izquierdo", "Aurícula derecha", "Ventrículo derecho"), 1),
    QuizQuestion("Circulación · Vasos", "¿Qué llevan las arterias pulmonares?",
        listOf("Sangre oxigenada", "Sangre con CO₂", "Linfa", "Plasma"), 1),
    QuizQuestion("Corazón · Válvulas", "Función principal de las válvulas cardíacas:",
        listOf("Bombear sangre", "Evitar retroceso de sangre", "Filtrar glóbulos", "Producir oxígeno"), 1),
    QuizQuestion("Circulación · Pulso", "El pulso que sientes refleja…",
        listOf("Respiración pulmonar", "Contracción del ventrículo", "Dilatación de venas", "Ritmo del nervio vago"), 1),
    QuizQuestion("Circulación · Retorno", "Las venas regresan la sangre a…",
        listOf("Ventrículo izquierdo", "Pulmones", "Aurícula derecha", "Aorta"), 2),
    QuizQuestion("Circulación · Circuitos", "¿Cuántos circuitos tiene la circulación sanguínea?",
        listOf("1 (general)", "2 (mayor y menor)", "3 (mayor, menor y portal)", "4"), 1),
)

@Composable
fun QuizScreen(navController: NavHostController) {
    var currentQuestion by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    val totalQuestions = quizQuestions.size

    val selectedForCurrent = selectedAnswers[currentQuestion]
    val hasAnswered = selectedForCurrent != null
    val isLastQuestion = currentQuestion == totalQuestions - 1

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            QuizTopBar(
                currentQuestion = currentQuestion,
                total = totalQuestions,
                navController = navController,
            )
        },
        bottomBar = {
            QuizActionDock(
                hasAnswered = hasAnswered,
                isLastQuestion = isLastQuestion,
                onNext = {
                    if (isLastQuestion) {
                        navController.navigate(Screen.QuizResults.route) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    } else {
                        currentQuestion++
                    }
                },
                onSkip = {
                    if (isLastQuestion) {
                        navController.navigate(Screen.QuizResults.route) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    } else {
                        currentQuestion++
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SegmentedProgressBar(current = currentQuestion, total = totalQuestions)
            QuestionCard(question = quizQuestions[currentQuestion])
            Spacer(Modifier.height(14.dp))
            AnswerOptions(
                question = quizQuestions[currentQuestion],
                selectedIndex = selectedForCurrent,
                onSelect = { idx ->
                    if (!hasAnswered) selectedAnswers = selectedAnswers + (currentQuestion to idx)
                },
            )
            if (hasAnswered) {
                Spacer(Modifier.height(14.dp))
                FeedbackPanel(
                    correct = selectedForCurrent == quizQuestions[currentQuestion].correctIndex,
                    correctAnswer = quizQuestions[currentQuestion].options[quizQuestions[currentQuestion].correctIndex],
                )
                Spacer(Modifier.height(14.dp))
                AgentHint()
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(currentQuestion: Int, total: Int, navController: NavHostController) {
    TopAppBar(
        title = {
            Column {
                Text("Quiz · Circulatorio", style = MaterialTheme.typography.titleMedium.copy(lineHeight = 20.sp))
                Text(
                    "CÁMARAS DEL CORAZÓN",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
            }
        },
        actions = {
            IconButton(onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Quiz.route) { inclusive = true }
                }
            }) {
                Icon(Icons.Rounded.Close, contentDescription = "Cerrar quiz")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun SegmentedProgressBar(current: Int, total: Int) {
    val filledColor = MaterialTheme.colorScheme.primary
    val unfilledColor = MaterialTheme.colorScheme.surfaceContainerHighest
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxWidth().height(8.dp)
        ) {
            val segmentWidth = size.width / total
            val gap = 3.dp.toPx()
            repeat(total) { i ->
                val x = i * segmentWidth
                val filled = i < current + 1
                drawRoundRect(
                    color = if (filled) filledColor else unfilledColor,
                    topLeft = Offset(x + gap / 2, 0f),
                    size = androidx.compose.ui.geometry.Size(segmentWidth - gap, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Pregunta ${current + 1} de $total",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text("1:24", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QuestionCard(question: QuizQuestion) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Topic chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Rounded.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                Text(
                    question.topic.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                question.text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp, bottom = 0.dp),
            )
            // Heart illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(top = 14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.radialGradient(colors = listOf(Mint.copy(alpha = 0.12f), MaterialTheme.colorScheme.surfaceContainer))),
                contentAlignment = Alignment.Center,
            ) {
                HeartIllustration()
            }
        }
    }
}

@Composable
private fun HeartIllustration() {
    val primaryColor = MaterialTheme.colorScheme.primary
    androidx.compose.foundation.Canvas(modifier = Modifier.size(80.dp, 90.dp)) {
        val sx = size.width / 100f
        val sy = size.height / 110f

        val heartPath = Path().apply {
            moveTo(50 * sx, 86 * sy)
            cubicTo(50 * sx, 86 * sy, 24 * sx, 71 * sy, 24 * sx, 50 * sy)
            cubicTo(24 * sx, 39 * sy, 32 * sx, 32 * sy, 41 * sx, 32 * sy)
            cubicTo(45 * sx, 32 * sy, 48 * sx, 34 * sy, 50 * sx, 37 * sy)
            cubicTo(52 * sx, 34 * sy, 55 * sx, 32 * sy, 59 * sx, 32 * sy)
            cubicTo(68 * sx, 32 * sy, 76 * sx, 39 * sy, 76 * sx, 50 * sy)
            cubicTo(76 * sx, 71 * sy, 50 * sx, 86 * sy, 50 * sx, 86 * sy)
            close()
        }
        drawPath(heartPath, color = primaryColor.copy(alpha = 0.18f))
        drawPath(heartPath, color = primaryColor, style = Stroke(width = 1.6f * sx, cap = StrokeCap.Round))
        // Ventricle highlight
        val vPath = Path().apply {
            moveTo(50 * sx, 56 * sy)
            lineTo(72 * sx, 56 * sy)
            lineTo(70 * sx, 76 * sy)
            lineTo(54 * sx, 78 * sy)
            close()
        }
        drawPath(vPath, color = Mint.copy(alpha = 0.35f))
        drawPath(vPath, color = Mint, style = Stroke(width = 1.5f * sx))
    }
}

@Composable
private fun AnswerOptions(question: QuizQuestion, selectedIndex: Int?, onSelect: (Int) -> Unit) {
    val successColors = LocalSuccessColors.current
    val letters = listOf("A", "B", "C", "D")
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        question.options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            val isCorrect = selectedIndex != null && index == question.correctIndex
            val isWrong = selectedIndex != null && selectedIndex == index && index != question.correctIndex
            val isDisabled = selectedIndex != null && !isSelected && !isCorrect

            val (bgColor, borderColor, letterBg, letterFg, textColor) = when {
                isCorrect  -> listOf(successColors.successContainer, successColors.success, successColors.success, successColors.onSuccess, successColors.onSuccessContainer)
                isWrong    -> listOf(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError, MaterialTheme.colorScheme.onErrorContainer)
                isSelected -> listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.onPrimaryContainer)
                else       -> listOf(MaterialTheme.colorScheme.surfaceContainerLow, Color.Transparent, MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.onSurface)
            }

            Surface(
                onClick = { if (!isDisabled && selectedIndex == null) onSelect(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isDisabled) Modifier.then(Modifier) else Modifier),
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                enabled = !isDisabled,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).drawAlpha(if (isDisabled) 0.6f else 1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(CircleShape).background(letterBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(letters[index], style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = letterFg)
                    }
                    Text(option, style = MaterialTheme.typography.bodyLarge, color = textColor, modifier = Modifier.weight(1f))
                    if (isCorrect) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = successColors.success, modifier = Modifier.size(22.dp))
                    if (isWrong) Icon(Icons.Rounded.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun FeedbackPanel(correct: Boolean, correctAnswer: String) {
    val successColors = LocalSuccessColors.current
    val errorContainer = MaterialTheme.colorScheme.errorContainer
    val error = MaterialTheme.colorScheme.error
    val onErrorContainer = MaterialTheme.colorScheme.onErrorContainer
    val onError = MaterialTheme.colorScheme.onError
    val bg = if (correct) successColors.successContainer else errorContainer
    val borderColor = if (correct) successColors.success else error
    val fg = if (correct) successColors.onSuccessContainer else onErrorContainer
    val glyphBg = if (correct) successColors.success else error
    val glyphFg = if (correct) successColors.onSuccess else onError
    val title = if (correct) "¡Correcto!" else "Incorrecto"
    val body = if (correct)
        "El ventrículo izquierdo tiene paredes gruesas y bombea sangre oxigenada por la aorta hacia todo el cuerpo."
    else
        "La respuesta correcta es \"$correctAnswer\". Repasa el flujo de las cámaras cardíacas."

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(width = 0.dp, color = Color.Transparent, shape = RoundedCornerShape(16.dp))
            .padding(start = 0.dp),
    ) {
        Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(borderColor))
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(glyphBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (correct) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = null,
                    tint = glyphFg,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = fg)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = fg.copy(alpha = 0.88f), modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun AgentHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(10.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(16.dp))
        }
        Text(
            buildAnnotatedString {
                withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Medium)) { append("Pista del agente · ") }
                append("recuerda: ")
                withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Medium)) { append("ventrículos") }
                append(" bombean, ")
                withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Medium)) { append("aurículas") }
                append(" reciben.")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuizActionDock(
    hasAnswered: Boolean,
    isLastQuestion: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = {},
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(Icons.Rounded.Flag, contentDescription = "Reportar", modifier = Modifier.size(20.dp))
        }
        Button(
            onClick = if (hasAnswered) onNext else onSkip,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                if (isLastQuestion) "Ver resultados" else if (hasAnswered) "Siguiente pregunta" else "Saltar",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
            )
            if (hasAnswered || isLastQuestion) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
