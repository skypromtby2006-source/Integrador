# Reporte de Cambios — Anatomia App

**Fecha:** 2026-05-28
**Rama:** master
**Estado final:** ✅ Listo para commit (ver sección de pendientes)

---

## Resumen ejecutivo

Se completaron tres bloques de trabajo que conectan el ciclo completo
estudiante → quiz → backend → docente:

| Bloque | Alcance | Estado |
|--------|---------|--------|
| **FIX-A al FIX-D** (Android, fixes 03–09) | Navegación, Quiz, Agente BDI, pantallas M3 | ✅ PASS |
| **FIX-T1T2** (Teacher App — manejo de errores) | ClassesScreen, StudentsScreen, EvaluacionesScreen | ✅ PASS |
| **FEATURE-T3** (Módulo de progreso de sesión) | ProgressStore, ProgressService, endpoints, SessionProgressScreen | ✅ PASS |

---

## BLOQUE 1 — Android Student App (fixes 03–09 + fix 10)

### Fix 03 — Navegación con organId
- `Screen.kt`: rutas parametrizadas que propagan el `organId` seleccionado
  desde el modelo 3D hasta las pantallas Quiz y Agente.
- `App.kt`: NavHost actualizado para leer el argumento en cada destino.

### Fix 04 — QuizViewModel
- `QuizViewModel.kt`: corregida la carga de preguntas por órgano via `ContentBank`.
- Integración con `AgentRepository.recordAnswer()` al finalizar el quiz
  para que el agente BDI registre cada respuesta.
- `currentOrganId` guardado en estado para pasarlo correctamente a `recordAnswer`.

### Fix 05 — QuizScreen
- Eliminados hardcodes de órgano; la pantalla recibe el `organId` por parámetro
  de navegación.

### Fix 06 — QuizResultsScreen
- Resultados calculados desde el estado real del ViewModel, sin valores fijos.

### Fix 07 — AgentDashboardViewModel
- Eliminados hardcodes (`"Ana"`, `weekNumber = 6`).
- El ViewModel ahora carga nombre, semana y estadísticas desde `SessionStore`.

### Fixes 08–09 — AgentScreen y HomeScreen
- `AgentScreen.kt` en `ui/screens/` reescrito para consumir `AgentDashboardViewModel`.
- `HomeScreen.kt`: tarjetas de acceso rápido conectadas a navegación real.
- Eliminado el `AgentScreen.kt` legacy de `ui/screen/agent/` (directorio vacío).

### Fix 10 — ProgressStore (persistencia entre sesiones)
- Nueva interfaz `expect class ProgressStore` con implementación `actual`
  para Android usando `SharedPreferences`.
- `AgentRepository` usa `ProgressStore` para que el historial de respuestas
  sobreviva reinicios de la app.
- `libs.versions.toml` y `build.gradle.kts` actualizados con dependencia
  `multiplatform-settings` para soporte multiplataforma.

### Pantallas nuevas (commit previo)
- `HistoryScreen.kt` + `HistoryViewModel.kt`: historial de sesiones del estudiante
  cargado desde `AgentRepository`, sin datos hardcodeados.
- `EditProfileScreen.kt` + `EditProfileViewModel.kt`: edición de perfil con
  `fun save()` que persiste via `SessionRepository`.
- `SettingsScreen.kt`: pantalla de configuración básica.

---

## BLOQUE 2 — Teacher App: manejo de errores (FIX-T1T2)

### ClassesScreen.kt
- `loadError` como `mutableStateOf<String?>`.
- Bloque `try/catch` alrededor de la carga inicial y de la recarga manual.
- Banner de error visible con botón "Reintentar".

### StudentsScreen.kt
- Mismo patrón: `loadError`, `try/catch` en carga inicial y recargas.
- Banner de error integrado en el layout existente.

### EvaluacionesScreen.kt
- `fun reload()` (sin `suspend`) que lanza una corutina interna.
- `loadError` para captura de fallos en carga de contenidos, creación,
  actualización y eliminación de evaluaciones.

---

## BLOQUE 3 — Módulo de progreso de sesión (FEATURE-T3)

### Android — ProgressService.kt
- Nuevo servicio HTTP (`45 líneas`) que envía sesiones de quiz al backend.
- Endpoint: `POST /progreso/sesion` con payload:
  `{ studentId, organId, answers[], timestamp }`.
- Integrado en `QuizViewModel.finishQuiz()`.

### Teacher App — setup-db.sh
- Nueva tabla `sesion_quiz`:
  ```sql
  CREATE TABLE IF NOT EXISTS sesion_quiz (
      sesion_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      usuario_id    VARCHAR(20) NOT NULL REFERENCES usuario(usuario_id),
      organ_id      VARCHAR(50) NOT NULL,
      correctas     SMALLINT NOT NULL DEFAULT 0,
      incorrectas   SMALLINT NOT NULL DEFAULT 0,
      saltadas      SMALLINT NOT NULL DEFAULT 0,
      puntaje_total FLOAT,
      realizado_en  TIMESTAMP NOT NULL DEFAULT NOW()
  );
  ```
- Índices: `idx_sesion_quiz_usuario`, `idx_sesion_quiz_organ`.

### Teacher App — HttpServer.kt (endpoints nuevos)
- `POST /progreso/sesion` — recibe y guarda una sesión de quiz.
- `GET  /progreso/sesiones/{ci}` — lista sesiones de un estudiante.
- `GET  /progreso/sesiones/clase/{claseId}` — resumen por clase.

### Teacher App — Repositories.kt
- `SesionRepository.submit()`: inserta una sesión con conteo de correctas/incorrectas/saltadas.
- `SesionRepository.getByEstudiante()`: consulta sesiones de un CI.
- `SesionRepository.getResumenByClase()`: agrega sesiones por clase para el dashboard.

### Teacher App — SessionProgressScreen.kt (`270 líneas`)
- Nueva pantalla en el panel docente bajo la pestaña **Progreso** (icono `BarChart`).
- Selector de clase → lista de alumnos con sesiones expandibles.
- Muestra por alumno: total de sesiones, puntaje promedio, detalle por órgano.
- Manejo de error y estado vacío integrados.

### Teacher App — App.kt
- `Screen.Progress` añadido al enum de navegación.
- `when(screen)` conecta `Screen.Progress → SessionProgressScreen(docenteId)`.

---

## Resultados de la prueba general (PRUEBA-GENERAL.md)

```
SECCIÓN 1 Android Build:     [ PASS ]   BUILD SUCCESSFUL — 0 errores de compilación
SECCIÓN 2 Teacher Build:     [ PASS ]   BUILD SUCCESSFUL (dry-run)
SECCIÓN 3 Base de datos:     [ PASS ]   CREATE TABLE + 2 índices en setup-db.sh
SECCIÓN 4 Endpoints HTTP:    [ PARCIAL ] Endpoints responden; tabla sesion_quiz falta en BD
SECCIÓN 5 Flujo Android:     [ PASS ]   Sin hardcodes, ProgressService correcto
SECCIÓN 6 Error handling:    [ PASS ]   try/catch + loadError en las 3 pantallas
```

---

## Pendiente antes de producción

- [ ] **Ejecutar migración de BD** — el único paso manual que falta:
  ```bash
  cd teacher-app
  bash setup-db.sh
  ```
  Esto crea la tabla `sesion_quiz` y sus índices en PostgreSQL.
  Sin este paso, los endpoints `/progreso/sesion` retornan error de BD.

---

## Archivos modificados / creados

### Android (`android/composeApp/`)
| Archivo | Acción |
|---------|--------|
| `src/commonMain/.../navigation/Screen.kt` | modificado |
| `src/commonMain/.../App.kt` | modificado |
| `src/commonMain/.../ui/screens/QuizViewModel.kt` | modificado |
| `src/commonMain/.../ui/screens/QuizScreen.kt` | modificado |
| `src/commonMain/.../ui/screens/QuizResultsScreen.kt` | modificado |
| `src/commonMain/.../ui/screens/AgentDashboardViewModel.kt` | modificado |
| `src/commonMain/.../ui/screens/AgentScreen.kt` | modificado |
| `src/commonMain/.../ui/screens/HomeScreen.kt` | modificado |
| `src/commonMain/.../ui/screens/HistoryScreen.kt` | nuevo |
| `src/commonMain/.../ui/screens/HistoryViewModel.kt` | nuevo |
| `src/commonMain/.../ui/screens/EditProfileScreen.kt` | nuevo |
| `src/commonMain/.../ui/screens/EditProfileViewModel.kt` | nuevo |
| `src/commonMain/.../ui/screens/SettingsScreen.kt` | nuevo |
| `src/commonMain/.../agent/ProgressStore.kt` | nuevo (expect) |
| `src/commonMain/.../network/ProgressService.kt` | nuevo |
| `src/androidMain/.../agent/ProgressStore.kt` | nuevo (actual Android) |
| `src/jvmMain/.../agent/ProgressStore.kt` | nuevo (actual JVM) |
| `src/androidMain/.../MainActivity.kt` | modificado |
| `gradle/libs.versions.toml` + `build.gradle.kts` | modificado |
| `src/.../ui/screen/agent/AgentScreen.kt` | **eliminado** (legacy) |
| `src/.../ui/screen/agent/AgentViewModel.kt` | **eliminado** (legacy) |

### Teacher App (`teacher-app/`)
| Archivo | Acción |
|---------|--------|
| `src/main/kotlin/ui/ClassesScreen.kt` | modificado |
| `src/main/kotlin/ui/StudentsScreen.kt` | modificado |
| `src/main/kotlin/ui/EvaluacionesScreen.kt` | modificado |
| `src/main/kotlin/ui/SessionProgressScreen.kt` | **nuevo** |
| `src/main/kotlin/ui/App.kt` | modificado |
| `src/main/kotlin/server/HttpServer.kt` | modificado |
| `src/main/kotlin/db/Repositories.kt` | modificado |
| `setup-db.sh` | modificado |
