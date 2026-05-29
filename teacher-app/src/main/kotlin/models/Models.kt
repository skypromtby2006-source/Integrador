package models

import kotlinx.serialization.Serializable

// ── Auth ──────────────────────────────────────────────────────────────────────
@Serializable
data class LoginDocenteRequest(
    val ci:       String,
    val password: String
)

@Serializable
data class DocenteSession(
    val usuarioId: String,
    val nombre:    String,
    val apellido:  String,
    val email:     String
)

// ── Clase ─────────────────────────────────────────────────────────────────────
@Serializable
data class Clase(
    val claseId:      String,
    val nombre:       String,
    val grado:        String,
    val turno:        String,
    val anioEscolar:  String,
    val totalAlumnos: Int = 0
)

// ── Estudiante ────────────────────────────────────────────────────────────────
@Serializable
data class Estudiante(
    val usuarioId:       String,
    val nombre:          String,
    val apellido:        String,
    val email:           String,
    val claseId:         String,
    val claseNombre:     String = "",
    val grado:           String = "",
    val nivelXp:         Int    = 0,
    val estado:          String = "activo",
    val fechaNacimiento: String = ""
)

@Serializable
data class CreateEstudianteRequest(
    val ci:              String,
    val nombre:          String,
    val apellido:        String,
    val password:        String,
    val claseId:         String,
    val fechaNacimiento: String = ""
)

@Serializable
data class UpdateEstudianteRequest(
    val nombre:          String,
    val apellido:        String,
    val claseId:         String,
    val fechaNacimiento: String  = "",
    val newPassword:     String? = null
)

@Serializable
data class SetEstadoRequest(
    val estado: String
)

// ── Login estudiante (Android) ────────────────────────────────────────────────
@Serializable
data class LoginEstudianteRequest(
    val email:    String,
    val password: String
)

@Serializable
data class LoginEstudianteResponse(
    val usuarioId:   String,
    val nombre:      String,
    val apellido:    String,
    val email:       String,
    val claseId:     String,
    val claseNombre: String,
    val grado:       String,
    val nivelXp:     Int
)

// ── Contenido + Pregunta ──────────────────────────────────────────────────────
@Serializable
data class ContenidoBiologico(
    val contenidoId:     String,
    val titulo:          String,
    val descripcion:     String = "",
    val categoria:       String = "",
    val nivelDificultad: Int    = 1
)

@Serializable
data class Pregunta(
    val preguntaId:      String,
    val contenidoId:     String,
    val tituloContenido: String       = "",
    val enunciado:       String,
    val subtema:         String       = "",
    val nivelDificultad: Int          = 1,
    val opciones:        List<Opcion> = emptyList()
)

@Serializable
data class Opcion(
    val opcionId:  String,
    val letra:     String,
    val texto:     String,
    val esCorrecta: Boolean
)

@Serializable
data class CreatePreguntaRequest(
    val contenidoId:     String,
    val enunciado:       String,
    val subtema:         String               = "",
    val nivelDificultad: Int                  = 1,
    val docenteId:       String,
    val opciones:        List<CreateOpcionRequest>
)

@Serializable
data class UpdatePreguntaRequest(
    val enunciado:       String,
    val subtema:         String  = "",
    val nivelDificultad: Int     = 1,
    val contenidoId:     String,
    val opciones:        List<CreateOpcionRequest>
)

@Serializable
data class CreateOpcionRequest(
    val letra:     String,
    val texto:     String,
    val esCorrecta: Boolean
)

// ── Progreso (desde Android) ──────────────────────────────────────────────────
@Serializable
data class SubmitIntentoRequest(
    val usuarioId:    String,
    val evaluacionId: String,
    val resultados:   List<ResultadoRequest>
)

@Serializable
data class ResultadoRequest(
    val preguntaId:        String,
    val esCorrecta:        Boolean,
    val tiempoRespuestaMs: Int = 0
)

// ── Sesión de quiz libre (desde Android) ─────────────────────────────────────

@Serializable
data class SubmitSesionRequest(
    val studentId : String,
    val organId   : String,
    val answers   : List<AnswerRecord>,
    val timestamp : Long = System.currentTimeMillis(),
)

@Serializable
data class AnswerRecord(
    val questionId: String,
    val wasCorrect: Boolean,
)

@Serializable
data class SesionQuiz(
    val sesionId     : String,
    val organId      : String,
    val correctas    : Int,
    val incorrectas  : Int,
    val saltadas     : Int,
    val puntajeTotal : Double,
    val realizadoEn  : String,
)

@Serializable
data class SesionResumenEstudiante(
    val usuarioId : String,
    val nombre    : String,
    val apellido  : String,
    val email     : String,
    val sesiones  : List<SesionQuiz>,
)

// ── Progreso del estudiante (para pantalla Clases) ────────────────────────────
@Serializable
data class EstudianteProgreso(
    val usuarioId:      String,
    val nombre:         String,
    val apellido:       String,
    val email:          String,
    val nivelXp:        Int,
    val totalIntentos:  Int,
    val totalCorrectas: Int,
    val totalPreguntas: Int
)

// ── Evaluación ────────────────────────────────────────────────────────────────
@Serializable
data class Evaluacion(
    val evaluacionId:    String,
    val contenidoId:     String,
    val tituloContenido: String = "",
    val titulo:          String,
    val nivelDificultad: Int    = 1,
    val totalIntentos:   Int    = 0,
    val creadoEn:        String = ""
)

@Serializable
data class CreateEvaluacionRequest(
    val contenidoId:     String,
    val titulo:          String,
    val nivelDificultad: Int = 1,
    val docenteId:       String
)

@Serializable
data class UpdateEvaluacionRequest(
    val titulo:          String,
    val nivelDificultad: Int,
    val contenidoId:     String
)

@Serializable
data class IntentoResumen(
    val intentoId:        String,
    val evaluacionId:     String,
    val tituloEvaluacion: String = "",
    val usuarioId:        String,
    val nombreEstudiante: String = "",
    val correctas:        Int,
    val incorrectas:      Int,
    val saltadas:         Int,
    val puntajeTotal:     Double,
    val iniciadoEn:       String,
    val finalizadoEn:     String = ""
)

// ── API wrapper ───────────────────────────────────────────────────────────────
@Serializable
data class ApiResponse<T>(
    val ok:    Boolean,
    val data:  T?      = null,
    val error: String? = null
)
