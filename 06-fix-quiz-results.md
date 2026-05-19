# Fix 06 · QuizResultsScreen.kt — Resultados reales desde QuizViewModel

**Depende de:** Fix 04, Fix 05

---

## El problema

QuizResultsScreen tiene score y lista de respuestas completamente hardcodeados.
Los datos reales están en `QuizUiState.Finished` que genera el QuizViewModel
cuando el estudiante termina el quiz.

## Cómo compartir el ViewModel entre Quiz y QuizResults

En Compose Navigation, dos composables pueden compartir el mismo ViewModel
si se obtiene usando el `NavBackStackEntry` de la ruta **padre** (o de la ruta
de origen). La forma correcta en KMP:

En `App.kt`, dentro del `NavHost`, **antes** de los composables de Quiz y QuizResults:

```kotlin
// En App.kt — dentro del NavHost, ANTES de los composables de Quiz y QuizResults
composable(
    route = Screen.Quiz.route,
    arguments = listOf(navArgument("organId") { type = NavType.StringType })
) { backStackEntry ->
    val organId = backStackEntry.arguments?.getString("organId") ?: "heart"

    // ViewModel scoped al backStackEntry de Quiz
    val quizViewModel: QuizViewModel = viewModel(backStackEntry)

    QuizScreen(
        navController = navController,
        organId       = organId,
        viewModel     = quizViewModel
    )
}

composable(Screen.QuizResults.route) {
    // Obtener el backStackEntry de Quiz (que sigue en el back stack)
    val quizBackStackEntry = remember(it) {
        navController.getBackStackEntry(Screen.Quiz.route)
    }
    val quizViewModel: QuizViewModel = viewModel(quizBackStackEntry)

    QuizResultsScreen(
        navController = navController,
        viewModel     = quizViewModel
    )
}
```

> **Por qué funciona:** Quiz sigue en el back stack cuando navegamos a QuizResults
> (usamos `popUpTo(Screen.Quiz.route) { inclusive = false }`).
> `getBackStackEntry` recupera ese entry y con él el mismo ViewModel instance.

---

## Cambio 1 — Firma de QuizResultsScreen

**Buscar:**
```kotlin
@Composable
fun QuizResultsScreen(navController: NavHostController) {
```

**Reemplazar con:**
```kotlin
@Composable
fun QuizResultsScreen(
    navController : NavHostController,
    viewModel     : QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Guardia: si el estado no es Finished (ej. acceso directo por deep link),
    // redirigir a Home
    val state = uiState as? QuizUiState.Finished ?: run {
        LaunchedEffect(Unit) { navController.navigate(Screen.Home.route) }
        return
    }

    QuizResultsContent(
        state         = state,
        onRetry       = {
            viewModel.restart()
            navController.popBackStack()   // vuelve a QuizScreen
        },
        onContinue    = {
            navController.navigate(Screen.Agent.route) {
                popUpTo(Screen.Quiz.route) { inclusive = true }
            }
        },
        onBack        = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }
    )
}
```

---

## Cambio 2 — Extraer UI a `QuizResultsContent`

```kotlin
@Composable
private fun QuizResultsContent(
    state      : QuizUiState.Finished,
    onRetry    : () -> Unit,
    onContinue : () -> Unit,
    onBack     : () -> Unit
) {
    // Mapeos del hardcoded al estado real:

    // Score ring:       state.pct           (era 0.75f hardcodeado)
    // Porcentaje texto: "${(state.pct * 100).roundToInt()}%"
    // Correctas:        state.score         (era 6 hardcodeado)
    // Incorrectas:      state.answers.values.count { it != null &&
    //                     it != state.questions[idx].correctIndex }
    // Saltadas:         state.answers.values.count { it == null }
    // XP ganada:        state.score * 20    (regla: 20 XP por correcta)

    // Lista de revisión por pregunta:
    // state.questions.mapIndexed { idx, question ->
    //   val selected = state.answers[idx]
    //   val isCorrect = selected == question.correctIndex
    //   val isSaltada = selected == null
    //   ReviewItem(
    //     number     = idx + 1,
    //     question   = question.text,
    //     status     = when { isCorrect -> ok; isSaltada -> skip; else -> bad },
    //     yourAnswer = selected?.let { question.options[it] } ?: "Saltada",
    //     correct    = question.options[question.correctIndex]
    //   )
    // }
}
```

---

## Cambio 3 — Calcular incorrectas desde el estado

```kotlin
// Helper para calcular incorrectas
val incorrectas = state.answers.entries.count { (idx, selected) ->
    selected != null && selected != state.questions[idx].correctIndex
}
val saltadas = state.answers.values.count { it == null }
val xpGanada = state.score * 20
```

---

## Eliminar

- La lista `reviewItems` hardcodeada
- El `val score = 0.75f` hardcodeado
- Los números 6, 1, 1, 120 hardcodeados en los stat cards

---

## Verificación

1. Completar un quiz
2. QuizResults muestra el score real del intento
3. La lista de revisión muestra las preguntas reales con el resultado real
4. "Repasar errores" → vuelve a Quiz y reinicia desde cero
5. "Continuar" → navega a Agent
