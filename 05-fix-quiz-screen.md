# Fix 05 · QuizScreen.kt — Conectar al QuizViewModel

**Depende de:** Fix 03, Fix 04
**Requerido por:** Fix 06

---

## Estrategia — qué conservar, qué reemplazar

QuizScreen ya tiene:
- ✅ UI completa (progress bar, opciones, feedback visual correcto/incorrecto)
- ✅ Lógica de color por estado (selected, correct, wrong)
- ✅ Navegación a QuizResults
- ❌ `quizQuestions` hardcodeada (lista privada de 8 preguntas de circulatorio)
- ❌ `selectedAnswers: Map<Int,Int>` local con `remember`
- ❌ No reporta resultados a nadie

El trabajo es reemplazar el estado local por el ViewModel, conservando toda la UI.

---

## Cambio 1 — Imports a añadir

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anatomia.app.agent.Question
```

---

## Cambio 2 — Firma de QuizScreen y obtención del ViewModel

**Buscar:**
```kotlin
@Composable
fun QuizScreen(
    navController: NavHostController,
    organId: String = "heart"
) {
    // estado local con remember...
    val questions = quizQuestions   // lista hardcodeada
    var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    // etc.
```

**Reemplazar con:**
```kotlin
@Composable
fun QuizScreen(
    navController : NavHostController,
    organId       : String = "heart",
    viewModel     : QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cargar preguntas al entrar a la pantalla
    LaunchedEffect(organId) {
        viewModel.loadQuiz(organId)
    }

    when (val state = uiState) {
        is QuizUiState.Loading  -> QuizLoadingContent()
        is QuizUiState.Error    -> QuizErrorContent(state.message)
        is QuizUiState.Active   -> QuizActiveContent(
            state       = state,
            onSelect    = { viewModel.selectAnswer(it) },
            onNext      = {
                if (state.isLastQuestion) {
                    viewModel.nextQuestion()
                    // La navegación ocurre en el siguiente when() cuando el estado cambia a Finished
                } else {
                    viewModel.nextQuestion()
                }
            },
            onSkip      = { viewModel.skipQuestion() }
        )
        is QuizUiState.Finished -> {
            // Estado Finished → navegar a resultados
            LaunchedEffect(state) {
                navController.navigate(Screen.QuizResults.route) {
                    popUpTo(Screen.Quiz.route) { inclusive = false }
                }
            }
        }
    }
}
```

---

## Cambio 3 — Extraer la UI existente a `QuizActiveContent`

Toda la UI actual de QuizScreen (top bar, progress, pregunta, opciones, dock)
pasa a ser un composable privado `QuizActiveContent` que recibe el estado y lambdas.

**Crear función privada:**
```kotlin
@Composable
private fun QuizActiveContent(
    state   : QuizUiState.Active,
    onSelect: (Int) -> Unit,
    onNext  : () -> Unit,
    onSkip  : () -> Unit
) {
    // Aquí va TODO el contenido actual de QuizScreen:
    // - Top bar con contador "Pregunta X de Y"
    // - LinearProgressIndicator con state.progress
    // - Texto de la pregunta: state.currentQuestion.text
    // - Lista de opciones: state.currentQuestion.options
    // - Feedback bar correcto/incorrecto
    // - Dock con botón Saltar y Siguiente

    // Mapeos clave del hardcoded al ViewModel:
    // questions[currentIndex].topic  → state.currentQuestion.topic
    // questions[currentIndex].text   → state.currentQuestion.text
    // questions[currentIndex].options → state.currentQuestion.options
    // questions[currentIndex].correctIndex → state.currentQuestion.correctIndex
    // selectedAnswers[currentIndex]  → state.selectedAnswer
    // isAnswered                     → state.isAnswered
    // currentIndex                   → state.currentIndex
    // questions.size                 → state.questions.size
    // progress                       → state.progress
}
```

---

## Cambio 4 — Añadir pantallas de Loading y Error

```kotlin
@Composable
private fun QuizLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun QuizErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = "Error al cargar el quiz: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
```

---

## Cambio 5 — Lógica de color de opciones (adaptar al nuevo estado)

La UI actual ya calcula los colores. Solo hay que cambiar las fuentes de datos:

```kotlin
// ANTES (variables locales)
val isSelected  = selectedAnswers[currentIndex] == index
val isCorrect   = isAnswered && index == questions[currentIndex].correctIndex
val isWrong     = isAnswered && isSelected && !isCorrect

// DESPUÉS (desde state)
val isSelected  = state.selectedAnswer == index
val isCorrect   = state.isAnswered && index == state.currentQuestion.correctIndex
val isWrong     = state.isAnswered && isSelected && !isCorrect
```

---

## Eliminar

- La lista `val quizQuestions = listOf(...)` hardcodeada
- Todas las variables `remember { mutableStateOf }` de estado de quiz
- El `mutableStateMapOf<Int, Int>()` de selectedAnswers
- Cualquier lógica de score calculada localmente

---

## Verificación

1. Compilar
2. Navegar a Quiz desde Home
3. Las preguntas deben venir de `questions_heart.json` (8 preguntas reales)
4. Seleccionar respuesta → feedback visual correcto/incorrecto funciona
5. Siguiente → avanza pregunta, barra de progreso se actualiza
6. Última pregunta → navega automáticamente a QuizResults
