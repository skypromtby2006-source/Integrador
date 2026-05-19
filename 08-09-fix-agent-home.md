# Fix 08 · AgentScreen.kt (M3) — Conectar al AgentDashboardViewModel

**Depende de:** Fix 07

---

## Estrategia — qué conservar, qué reemplazar

AgentScreen M3 ya tiene toda la UI lista:
- ✅ Hero con gradiente, avatar del agente
- ✅ Sección "Lo que noté en ti" (beliefs)
- ✅ Sección "Tu meta" con barra de progreso
- ✅ Sección "Mi plan para ti" (steps)
- ✅ Sección "¿Cómo te sientes?" (mood)
- ✅ Dock "Comenzar Quiz"
- ❌ Todo son listas privadas hardcodeadas en el archivo

---

## Cambio 1 — Imports y ViewModel

**Añadir al bloque de imports:**
```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
```

**Buscar la firma:**
```kotlin
@Composable
fun DidactaiAgentScreen(navController: NavHostController) {
```

**Reemplazar con:**
```kotlin
@Composable
fun DidactaiAgentScreen(
    navController : NavHostController,
    viewModel     : AgentDashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    // continúa con el contenido actual pero usando `state`
```

---

## Cambio 2 — Hero: reemplazar datos hardcodeados

```kotlin
// ANTES
"Buenos días, Ana"
"RESUMEN · SEMANA 6"
"Esta semana avanzaste un 40%..."

// DESPUÉS
"Buenos días, ${state.userName}"
"RESUMEN · SEMANA ${state.weekNumber}"
state.weekSummary
```

---

## Cambio 3 — Beliefs: reemplazar lista estática

**Buscar** la lista hardcodeada de creencias (algo como):
```kotlin
val beliefs = listOf(
    Triple("Recuerdas las funciones...", "acertaste 8 de 8...", "fuerte"),
    ...
)
```

**Reemplazar** el forEach que la renderiza para que itere sobre `state.beliefs`:
```kotlin
state.beliefs.forEach { belief ->
    BeliefRow(
        title  = belief.title,
        detail = belief.detail,
        level  = belief.level   // BeliefLevel.STRONG | PATTERN | WEAK
    )
    Divider()
}
```

**Mapeo de colores:**
```kotlin
val bulletColor = when (belief.level) {
    BeliefLevel.STRONG  -> MaterialTheme.colorScheme.primary    // verde éxito
    BeliefLevel.PATTERN -> LocalSuccessColors.current.success   // azul patrón
    BeliefLevel.WEAK    -> MaterialTheme.colorScheme.tertiary   // morado a mejorar
}
```

---

## Cambio 4 — Meta semanal

```kotlin
// ANTES
val progressPct = 0.65f
"Dominar el ciclo circulatorio completo"
"2 días restantes"

// DESPUÉS (state.goal es nullable — mostrar solo si existe)
state.goal?.let { goal ->
    GoalCard(
        title         = goal.title,
        progressPct   = goal.progressPct,
        daysRemaining = goal.daysRemaining,
        rhythm        = goal.rhythm
    )
}
```

---

## Cambio 5 — Plan de pasos

```kotlin
// ANTES: lista de 4 StepRow hardcodeados

// DESPUÉS: iterar state.planSteps
state.planSteps.forEach { step ->
    StepRow(
        order       = step.order,
        title       = step.title,
        reason      = step.reason,
        durationMin = step.durationMin,
        status      = step.status   // StepStatus.DONE | CURRENT | PENDING
    )
    if (step != state.planSteps.last()) Divider()
}
```

**Mapeo de status a UI:**
```kotlin
val circleContent = when (step.status) {
    StepStatus.DONE    -> { /* ícono check verde */ }
    StepStatus.CURRENT -> { /* número morado */ }
    StepStatus.PENDING -> { /* número gris con borde */ }
}
```

---

## Cambio 6 — Mood selector

```kotlin
// ANTES: selectMood() local con remember

// DESPUÉS: delegar al ViewModel
MoodRow(
    selected = state.selectedMood,
    onSelect = { viewModel.selectMood(it) }
)
```

---

## Cambio 7 — Botón "Comenzar Quiz"

```kotlin
// ANTES
navController.navigate(Screen.Quiz.route)

// DESPUÉS: usar el organId del estado
navController.navigate(Screen.Quiz.createRoute(state.organId))
```

---

---

# Fix 09 · HomeScreen.kt — Fecha real y nombre del usuario

**Depende de:** nada (cambio independiente)

---

## Cambio 1 — Fecha dinámica

**Buscar:**
```kotlin
Text("JUEVES · 14 MAY")
// o similar hardcodeado
```

**Reemplazar con:**
```kotlin
// Añadir import
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// En el composable
val today = remember {
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
}
val dayNames = listOf("DOM","LUN","MAR","MIÉ","JUE","VIE","SÁB")
val monthNames = listOf("ENE","FEB","MAR","ABR","MAY","JUN",
                        "JUL","AGO","SEP","OCT","NOV","DIC")
val dateLabel = "${dayNames[today.dayOfWeek.ordinal]} · ${today.dayOfMonth} ${monthNames[today.monthNumber - 1]}"

Text(dateLabel)
```

**Añadir dependencia en `libs.versions.toml`:**
```toml
[versions]
kotlinx-datetime = "0.6.2"

[libraries]
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

**Añadir en `composeApp/build.gradle.kts` en `commonMain.dependencies`:**
```kotlin
implementation(libs.kotlinx.datetime)
```

---

## Cambio 2 — Nombre del usuario

**Buscar:**
```kotlin
"buen día, Ana 👋"
// o
Text("buen día, ${"Ana"} 👋")
```

**Reemplazar con** (solución temporal hasta tener UserRepository con SQLDelight):
```kotlin
// Añadir en el companion de HomeScreen o como constante del módulo
// hasta que SQLDelight provea el nombre real desde la BD
val userName = "Ana"   // TODO: leer de UserRepository cuando SQLDelight esté listo
Text("buen día, $userName 👋")
```

> **Por qué no un ViewModel completo para el nombre ahora:**
> El nombre del usuario requiere SQLDelight para persistir. Crear un `UserViewModel`
> antes de tener la BD local significaría escribirlo dos veces. El `TODO` documenta
> exactamente qué hay que conectar en el Fix SQLDelight.

---

## Verificación Fix 08

1. AgentScreen muestra nombre y resumen semanal real del StudentProgress
2. Las creencias varían según el historial de respuestas (después de hacer un quiz)
3. La barra de meta refleja el score real
4. Los pasos del plan se renderizan dinámicamente
5. El selector de humor funciona y no resetea al cambiar de tab

## Verificación Fix 09

1. HomeScreen muestra la fecha real del sistema
2. Al cambiar el día, la fecha cambia automáticamente
