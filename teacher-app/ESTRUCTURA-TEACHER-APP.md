# Estructura teacher-app

> Generado: 2026-05-27

Aplicación de escritorio Compose Desktop (JVM) para docentes. Gestiona clases, alumnos, banco de preguntas y evaluaciones. Expone un servidor HTTP local (Ktor) para sincronizar datos con la app estudiante Android.

---

## Árbol de archivos

```
teacher-app/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── setup-db.sh                   # Script SQL para inicializar la base de datos
├── models/                       # Modelos 3D de órganos (referencia local)
│   ├── organ_heart.glb
│   ├── organ_kidneys.glb
│   └── organ_lungs.glb
└── src/main/kotlin/
    ├── Main.kt                   # Entry point: lanza la ventana Compose + HttpServer
    ├── models/
    │   └── Models.kt             # Todos los data classes del dominio
    ├── db/
    │   ├── DatabaseConfig.kt     # Configuración de conexión a base de datos
    │   └── Repositories.kt       # Repositorios de acceso a datos
    ├── server/
    │   └── HttpServer.kt         # Servidor HTTP Ktor (sincronización con Android)
    └── ui/
        ├── App.kt                # Raíz de la UI: login + shell + navegación
        ├── Theme.kt              # Tokens de color DColors + DidactaiTheme
        ├── ClassesScreen.kt      # Pantalla: Clases
        ├── StudentsScreen.kt     # Pantalla: Alumnos
        ├── QuestionsScreen.kt    # Pantalla: Banco de preguntas
        ├── EvaluacionesScreen.kt # Pantalla: Evaluaciones
        └── PdfExporter.kt        # Exportador de reportes PDF (iText)
```

---

## Capa `ui/`

### `App.kt` — Raíz y navegación

Punto de entrada de la interfaz. Controla si el docente está autenticado y decide qué mostrar.

```
App()
 ├── LoginDocenteScreen(onLogin)        # Formulario de login (CI + contraseña)
 └── MainShell(session, onLogout)
      ├── Sidebar(current, session, onSelect, onLogout)
      │    ├── Logo + nombre del docente (CI)
      │    ├── SidebarItem × 4 pantallas (con ícono y estado activo)
      │    ├── TextButton "Cerrar sesión"
      │    └── ServerStatusBadge (muestra puerto del servidor HTTP)
      └── Contenido principal (when currentScreen)
           ├── Screen.Classes      → ClassesScreen
           ├── Screen.Students     → StudentsScreen
           ├── Screen.Questions    → QuestionsScreen
           └── Screen.Evaluations  → EvaluacionesScreen
```

**Componentes compartidos definidos aquí:**

| Composable | Descripción |
|---|---|
| `DTextField` | Campo de texto estilizado con label y soporte para contraseña |
| `Chip` | Etiqueta coloreada pequeña para estados y categorías |
| `EmptyState` | Pantalla vacía con icono emoji, título y mensaje |

**Enum `Screen`:** define las 4 rutas — `Classes`, `Students`, `Questions`, `Evaluations` — cada una con label e ícono de Material Icons.

---

### `Theme.kt` — Sistema de diseño

Tema oscuro unificado para toda la app. El fondo oscuro (`#12151A`) fue elegido deliberadamente para reducir fatiga visual en sesiones largas de docentes.

**`object DColors`** — tokens de color:

| Token | Valor hex | Uso |
|---|---|---|
| `Primary` | `#40798C` | Azul teal principal, clases y alumnos |
| `Secondary` | `#82008C` | Violeta, evaluaciones |
| `Tertiary` | `#D23AAD` | Rosa, preguntas |
| `Error` | `#BA1A1A` | Errores, estados suspendido |
| `Success` / `Mint` | `#1B8A5A` / `#59FFCC` | Estados correctos y servidor activo |
| `Background` | `#12151A` | Fondo principal oscuro |
| `Sidebar` / `SidebarActive` | `#161920` / `#1F2A30` | Colores de la barra lateral |

`DidactaiTheme` envuelve `MaterialTheme` con `darkColorScheme` configurado con los tokens anteriores.

---

### `ClassesScreen.kt` — Gestión de clases

Ruta: `Screen.Classes`

Muestra la lista de clases del docente. Cada clase es expandible para ver el progreso de sus alumnos.

**Composables internos:**

| Composable | Descripción |
|---|---|
| `ClassesScreen(docenteId)` | Pantalla principal, carga clases con `ClaseRepository` |
| `ClassCard(cls, isExpanded, progress, onToggle)` | Tarjeta expandible de una clase |
| `StudentProgressRow(prg)` | Fila de progreso individual por alumno dentro de la tarjeta |

**Lógica de progreso por clase:**
- Se carga bajo demanda (lazy) al expandir la tarjeta.
- Muestra `totalCorrectas/totalPreguntas` con color según porcentaje: verde ≥70%, azul ≥40%, rojo <40%.
- Sin actividad → muestra "sin actividad" en gris.

---

### `StudentsScreen.kt` — Gestión de alumnos

Ruta: `Screen.Students`

CRUD completo de alumnos con filtros, búsqueda y exportación PDF.

**Composables internos:**

| Composable | Descripción |
|---|---|
| `StudentsScreen(docenteId)` | Pantalla principal con filtros y lista |
| `FilterChip(label, count, selected, onClick)` | Chip de filtro con contador (por clase y por estado) |
| `CreateStudentForm(classes, errorMsg, onCreated)` | Formulario de creación de alumno (AnimatedVisibility) |
| `StudentCard(student, classes, onDelete, onUpdate, onSetEstado)` | Tarjeta de alumno con acciones inline |
| `EditEstudianteForm(student, classes, onSave, onCancel)` | Formulario de edición inline expandible |

**Funcionalidades:**
- Búsqueda por nombre, apellido, C.I., curso o fecha de nacimiento.
- Filtro por clase (chips con contador) y por estado (`activo`, `inactivo`, `suspendido`).
- Estados con colores diferenciados: verde = activo, rojo = suspendido, gris = inactivo.
- Acciones por alumno: editar, activar/desactivar, suspender, eliminar (con confirmación).
- Email generado automáticamente al crear: `nombre.apellido.grado@didactai.edu`.
- Exportar lista filtrada a PDF + exportar lista de eliminados (baja lógica) a PDF.

---

### `QuestionsScreen.kt` — Banco de preguntas

Ruta: `Screen.Questions`

CRUD del banco de preguntas de opción múltiple con 4 opciones por pregunta.

**Composables internos:**

| Composable | Descripción |
|---|---|
| `QuestionsScreen(docenteId)` | Pantalla principal con filtros y lista |
| `QFilterChip(label, selected, onClick)` | Chip de filtro por contenido biológico (color Tertiary) |
| `CreateQuestionForm(contenidos, docenteId, errorMsg, onCreated)` | Formulario de creación con selector de contenido, dificultad y opciones |
| `QuestionCard(pregunta, contenidos, onDelete, onUpdate)` | Tarjeta de pregunta expandible para ver opciones |
| `EditPreguntaForm(pregunta, contenidos, onSave, onCancel)` | Formulario de edición inline |

**Funcionalidades:**
- Búsqueda por enunciado, subtema o contenido biológico.
- Filtro por órgano/contenido biológico (chips horizontales).
- Nivel de dificultad: creación con 3 niveles (Fácil/Medio/Difícil), edición con escala 1–5.
- Opciones A/B/C/D con radio button para marcar la correcta (resaltada en verde al expandir).
- Exportar preguntas filtradas a PDF.

---

### `EvaluacionesScreen.kt` — Evaluaciones

Ruta: `Screen.Evaluations`

CRUD de evaluaciones. Cada evaluación agrupa preguntas de un contenido biológico y registra intentos de los alumnos.

**Composables internos:**

| Composable | Descripción |
|---|---|
| `EvaluacionesScreen(docenteId)` | Pantalla principal con filtros y lista |
| `EvalFilterChip(label, count, selected, onClick)` | Chip de filtro por nivel (1–5 + Todos), color Secondary |
| `CreateEvaluacionForm(contenidos, docenteId, onCreated)` | Formulario de creación (título, contenido, nivel 1–5) |
| `EvaluacionCard(eval, contenidos, onDelete, onUpdate)` | Tarjeta con panel de intentos y edición inline |
| `EditEvaluacionForm(eval, contenidos, onSave, onCancel)` | Formulario de edición inline |

**Funcionalidades:**
- Búsqueda por título o contenido biológico.
- Filtro por nivel de dificultad 1–5.
- Panel de intentos cargado bajo demanda: muestra alumno, correctas/incorrectas y porcentaje con color.
- Exportar evaluaciones filtradas a PDF.
- Nivel de dificultad con 5 valores, colores por nivel: verde→azul→violeta→rosa→rojo.

---

### `PdfExporter.kt` — Exportación PDF

Objeto singleton que genera reportes PDF usando la librería **iText** y los abre automáticamente con el visor del sistema operativo.

Los archivos se guardan en `~/Didactai-Reportes/` con timestamp en el nombre.

| Método | Genera |
|---|---|
| `exportEstudiantes(estudiantes, filtros)` | Tabla con C.I., nombre, apellido, email, curso, estado |
| `exportEliminados(eliminados)` | Lista de alumnos con baja lógica (filas resaltadas en rosa) |
| `exportPreguntas(preguntas, filtros)` | Tabla con enunciado, contenido, subtema, nivel, respuesta correcta |
| `exportEvaluaciones(evaluaciones, filtros)` | Tabla con título, contenido, nivel, intentos, fecha de creación |

---

## Capa `models/Models.kt`

Todos los data classes del dominio, anotados con `@Serializable` (kotlinx.serialization).

| Grupo | Data classes |
|---|---|
| Auth docente | `LoginDocenteRequest`, `DocenteSession` |
| Auth estudiante | `LoginEstudianteRequest`, `LoginEstudianteResponse` |
| Clase | `Clase` |
| Estudiante | `Estudiante`, `CreateEstudianteRequest`, `UpdateEstudianteRequest`, `SetEstadoRequest` |
| Contenido | `ContenidoBiologico` |
| Pregunta | `Pregunta`, `Opcion`, `CreatePreguntaRequest`, `UpdatePreguntaRequest`, `CreateOpcionRequest` |
| Evaluación | `Evaluacion`, `CreateEvaluacionRequest`, `UpdateEvaluacionRequest`, `IntentoResumen` |
| Progreso | `SubmitIntentoRequest`, `ResultadoRequest`, `EstudianteProgreso` |
| Wrapper API | `ApiResponse<T>` |

---

## Capa `db/`

| Archivo | Responsabilidad |
|---|---|
| `DatabaseConfig.kt` | Configuración de la conexión (driver, URL, credenciales) |
| `Repositories.kt` | `AuthRepository`, `ClaseRepository`, `EstudianteRepository`, `ContenidoRepository`, `PreguntaRepository`, `EvaluacionRepository` — operaciones JDBC directas |

---

## Capa `server/`

| Archivo | Responsabilidad |
|---|---|
| `HttpServer.kt` | Servidor Ktor embebido. Expone endpoints REST para que la app Android consulte preguntas, envíe intentos y autentique estudiantes. La constante `HttpServer.PORT` es visible en el `ServerStatusBadge` del sidebar. |

---

## Flujo de datos

```
Android App (estudiante)
        │  HTTP (LAN local)
        ▼
 HttpServer.kt (Ktor)
        │
        ▼
 Repositories.kt (JDBC)
        │
        ▼
 Base de datos (PostgreSQL / SQLite)
        │
        ▼
 UI Compose Desktop (docente ve resultados en tiempo real)
```
