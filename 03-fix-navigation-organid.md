# Fix 03 · Screen.kt + App.kt — Añadir `organId` a la ruta Quiz

**Depende de:** nada
**Requerido por:** Fix 04, 05, 06

---

## Por qué este cambio va primero

`QuizViewModel` necesita saber qué órgano cargar del JSON.
Sin `organId` en la ruta, Quiz siempre cargaría el mismo JSON hardcodeado.
Este fix es el único cambio de navegación de toda la fase lógica —
se hace primero para no tener que tocar `App.kt` de nuevo.

---

## Cambio 1 — `navigation/Screen.kt`

**Buscar:**
```kotlin
object Quiz        : Screen("quiz")
```

**Reemplazar con:**
```kotlin
object Quiz : Screen("quiz/{organId}") {
    fun createRoute(organId: String) = "quiz/$organId"
}
```

> **Por qué `createRoute()`:** Las rutas con argumentos necesitan interpolación.
> `createRoute("heart")` produce `"quiz/heart"`. Sin esta función los llamadores
> tendrían que construir el string manualmente, creando riesgo de typos.

---

## Cambio 2 — `App.kt` · composable de Quiz

**Buscar** el composable que registra la ruta de Quiz en el NavHost:
```kotlin
composable(Screen.Quiz.route) {
    QuizScreen(navController = navController)
}
```

**Reemplazar con:**
```kotlin
composable(
    route = Screen.Quiz.route,
    arguments = listOf(navArgument("organId") { type = NavType.StringType })
) { backStackEntry ->
    val organId = backStackEntry.arguments?.getString("organId") ?: "heart"
    QuizScreen(
        navController = navController,
        organId       = organId
    )
}
```

Import a añadir si no existe:
```kotlin
import androidx.navigation.NavType
import androidx.navigation.navArgument
```

---

## Cambio 3 — `App.kt` · todas las llamadas que navegan a Quiz

Busca cada lugar donde se navega a Quiz y añade el organId.

**Buscar** (puede aparecer en HomeScreen o directamente en App.kt):
```kotlin
navController.navigate(Screen.Quiz.route)
```

**Reemplazar con:**
```kotlin
navController.navigate(Screen.Quiz.createRoute("heart"))
```

> **MVP:** "heart" hardcodeado por ahora. En la siguiente iteración,
> HomeScreen recibirá el organId del plan diario y lo pasará aquí.

---

## Cambio 4 — `ui/screens/QuizScreen.kt` · firma de la función

**Buscar:**
```kotlin
@Composable
fun QuizScreen(navController: NavHostController) {
```

**Reemplazar con:**
```kotlin
@Composable
fun QuizScreen(
    navController: NavHostController,
    organId: String = "heart"
) {
```

> Solo la firma por ahora. Fix 05 conecta el organId al ViewModel.

---

## Verificación

Compilar y correr. El flujo Home → Quiz debe seguir funcionando.
Quiz aún muestra preguntas hardcodeadas — eso se resuelve en Fix 05.
Lo importante es que la app compile sin errores después de este fix.
