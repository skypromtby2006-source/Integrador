package models

import kotlinx.serialization.Serializable

// ── Auth ──────────────────────────────────────────────────────────────────────
@Serializable
data class LoginDocenteRequest(
    val ci:       String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email:    String,
    val password: String
)

@Serializable
data class Verify2FARequest(
    val usuarioId: String,
    val codigo:    String
)

@Serializable
data class DocenteSession(
    val usuarioId:       String,
    val primerNombre:    String,
    val segundoNombre:   String?  = null,
    val apellidoPaterno: String,
    val apellidoMaterno: String?  = null,
    val email:           String,
    val rol:             String   = "docente"
) {
    val nombreCompleto: String get() =
        listOfNotNull(primerNombre, segundoNombre, apellidoPaterno, apellidoMaterno)
            .joinToString(" ")
    val nombreCorto: String get() = primerNombre
}

data class LoginResult(
    val usuarioId:    String,
    val primerNombre: String,
    val email:        String,
    val rol:          String,
    val requiere2FA:  Boolean
)

// ── Rol ───────────────────────────────────────────────────────────────────────
data class Rol(
    val rolId:      Int,
    val nombre:     String,
    val descripcion: String?
)

data class UsuarioConRol(
    val usuarioId:    String,
    val nombre:       String,
    val apellido:     String,
    val email:        String,
    val passwordHash: String,
    val rol:          String,
    val rolId:        Int
)

// ── Docente ───────────────────────────────────────────────────────────────────
@Serializable
data class CreateDocenteRequest(
    val ci:              String,
    val primerNombre:    String,
    val segundoNombre:   String?  = null,
    val apellidoPaterno: String,
    val apellidoMaterno: String?  = null,
    val email:           String,
    val password:        String,
    val especialidad:    String   = "",
    val tituloAcademico: String   = ""
)

@Serializable
data class DocenteInfo(
    val usuarioId:       String,
    val primerNombre:    String,
    val segundoNombre:   String?  = null,
    val apellidoPaterno: String,
    val apellidoMaterno: String?  = null,
    val email:           String,
    val especialidad:    String   = "",
    val tituloAcademico: String   = ""
) {
    val nombreCompleto: String get() =
        listOfNotNull(primerNombre, segundoNombre, apellidoPaterno, apellidoMaterno)
            .joinToString(" ")
}

@Serializable
data class DocenteListItem(
    val usuarioId:       String,
    val primerNombre:    String,
    val segundoNombre:   String?  = null,
    val apellidoPaterno: String,
    val apellidoMaterno: String?  = null,
    val email:           String,
    val estado:          String   = "activo",
    val especialidad:    String   = "",
    val tituloAcademico: String   = "",
    val tiene2FA:        Boolean  = false
) {
    val nombreCompleto: String get() =
        listOfNotNull(primerNombre, segundoNombre, apellidoPaterno, apellidoMaterno)
            .joinToString(" ")
}

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
    val ciNumero:        String,
    val ciComplemento:   String = "",
    val nombre:          String,
    val apellido:        String,
    val password:        String,
    val claseId:         String,
    val fechaNacimiento: String = "",
    val correoPersonal:  String = "",
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
    val descripcion:     String  = "",
    val categoria:       String  = "",
    val nivelDificultad: Int     = 1,
    val textoLectura:    String? = null,
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

// ── DTO para app Android ──────────────────────────────────────────────────────
@Serializable
data class QuestionAndroidDto(
    val id          : Int,
    val topic       : String,
    val organId     : String,
    val body        : String,
    val explanation : String,
    val options     : List<String>,
    val correctIndex: Int,
    val difficulty  : Int,
)

// ── API wrapper ───────────────────────────────────────────────────────────────
@Serializable
data class ApiResponse<T>(
    val ok:    Boolean,
    val data:  T?      = null,
    val error: String? = null
)
