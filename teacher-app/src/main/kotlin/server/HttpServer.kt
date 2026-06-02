package server

import db.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.*
import kotlinx.serialization.json.Json

object HttpServer {
    const val PORT = 8080
    private var server: NettyApplicationEngine? = null

    fun start() {
        server = embeddedServer(Netty, port = PORT) {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
            }
            routing {

                // ── Health ────────────────────────────────────────────────
                get("/ping") {
                    call.respond(mapOf("status" to "ok", "version" to "2.0"))
                }

                // ── Visor HTML embebido ───────────────────────────────────
                get("/viewer/{organId}") {
                    val organId  = call.parameters["organId"] ?: "heart"
                    val modelUrl = "https://stable-jailbreak-squire.ngrok-free.dev/models/organ_${organId}.glb"
                    call.respondText(contentType = ContentType.Text.Html, text = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                          <meta charset="UTF-8">
                          <meta name="viewport" content="width=device-width, initial-scale=1.0">
                          <script type="module" src="https://ajax.googleapis.com/ajax/libs/model-viewer/3.4.0/model-viewer.min.js"></script>
                          <style>
                            body { margin:0; background:#12151A; display:flex; align-items:center; justify-content:center; height:100vh; }
                            model-viewer { width:100%; height:100vh; }
                          </style>
                        </head>
                        <body>
                          <model-viewer src="$modelUrl" auto-rotate camera-controls shadow-intensity="1"
                            onclick="if(window.AndroidBridge) window.AndroidBridge.onModelTapped()">
                          </model-viewer>
                        </body>
                        </html>
                    """.trimIndent())
                }

                // ── Modelos 3D estáticos ──────────────────────────────────
                get("/models/{filename}") {
                    val filename = call.parameters["filename"] ?: return@get
                    val allowed  = listOf("organ_heart.glb", "organ_lungs.glb", "organ_kidneys.glb")
                    if (filename !in allowed) {
                        call.respond(HttpStatusCode.NotFound); return@get
                    }
                    val file = java.io.File("models/$filename")
                    if (!file.exists()) {
                        call.respond(HttpStatusCode.NotFound); return@get
                    }
                    call.respondFile(file)
                }

                // ── Auth docente ──────────────────────────────────────────
                post("/auth/docente") {
                    val req = call.receive<LoginDocenteRequest>()
                    val session = AuthRepository.loginDocente(req.ci, req.password)
                    if (session != null) {
                        call.respond(ApiResponse(ok = true, data = session))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized,
                            ApiResponse<DocenteSession>(ok = false, error = "CI o contraseña incorrectos"))
                    }
                }

                // ── Auth estudiante (Android) ─────────────────────────────
                post("/auth/estudiante") {
                    val req = call.receive<LoginEstudianteRequest>()
                    val resp = AuthRepository.loginEstudiante(req.email, req.password)
                    if (resp != null) {
                        call.respond(ApiResponse(ok = true, data = resp))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized,
                            ApiResponse<LoginEstudianteResponse>(ok = false,
                                error = "Correo o contraseña incorrectos"))
                    }
                }

                // ── Clases ────────────────────────────────────────────────
                get("/clases/{docenteId}") {
                    val docenteId = call.parameters["docenteId"] ?: run {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse<List<Clase>>(ok = false, error = "docenteId requerido"))
                        return@get
                    }
                    call.respond(ApiResponse(ok = true, data = ClaseRepository.getAll(docenteId)))
                }

                // ── Estudiantes ───────────────────────────────────────────
                route("/estudiantes") {
                    get {
                        val claseId = call.request.queryParameters["claseId"]
                        val lista = if (claseId != null)
                            EstudianteRepository.getByClase(claseId)
                        else
                            EstudianteRepository.getAll()
                        call.respond(ApiResponse(ok = true, data = lista))
                    }

                    post {
                        val req = call.receive<CreateEstudianteRequest>()
                        if (req.ciNumero.isBlank() || req.nombre.isBlank() || req.password.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Estudiante>(ok = false, error = "ciNumero, nombre y password requeridos"))
                            return@post
                        }
                        val estudiante = EstudianteRepository.create(req)
                        call.respond(HttpStatusCode.Created, ApiResponse(ok = true, data = estudiante))
                    }

                    delete("/{ci}") {
                        val ci = call.parameters["ci"] ?: run {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Unit>(ok = false, error = "ci requerido"))
                            return@delete
                        }
                        val deleted = EstudianteRepository.delete(ci)
                        if (deleted) call.respond(ApiResponse<Unit>(ok = true))
                        else call.respond(HttpStatusCode.NotFound,
                            ApiResponse<Unit>(ok = false, error = "Estudiante no encontrado"))
                    }

                    put("/{ci}") {
                        val ci = call.parameters["ci"] ?: run {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Unit>(ok = false, error = "ci requerido"))
                            return@put
                        }
                        val req = call.receive<UpdateEstudianteRequest>()
                        val estudiante = EstudianteRepository.update(
                            ci              = ci,
                            nombre          = req.nombre,
                            apellido        = req.apellido,
                            claseId         = req.claseId,
                            fechaNacimiento = req.fechaNacimiento,
                            newPassword     = req.newPassword
                        )
                        call.respond(ApiResponse(ok = true, data = estudiante))
                    }

                    patch("/{ci}/estado") {
                        val ci = call.parameters["ci"] ?: run {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Unit>(ok = false, error = "ci requerido"))
                            return@patch
                        }
                        val req = call.receive<SetEstadoRequest>()
                        if (req.estado !in listOf("activo", "inactivo", "suspendido")) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Unit>(ok = false, error = "estado inválido"))
                            return@patch
                        }
                        val ok = EstudianteRepository.setEstado(ci, req.estado)
                        if (ok) call.respond(ApiResponse<Unit>(ok = true))
                        else call.respond(HttpStatusCode.NotFound,
                            ApiResponse<Unit>(ok = false, error = "Estudiante no encontrado"))
                    }
                }

                // ── Contenido biológico ───────────────────────────────────
                route("/contenido") {
                    get { call.respond(ApiResponse(ok = true, data = ContenidoRepository.getAll())) }
                }

                // ── Endpoint para app Android ─────────────────────────────
                get("/questions/by-organ/{organId}") {
                    val organId = call.parameters["organId"]
                    if (organId.isNullOrBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse(ok = false, data = "organId requerido")
                        )
                        return@get
                    }

                    val preguntas = PreguntaRepository.getByOrganId(organId)

                    val dtos = preguntas.mapIndexed { index, p ->
                        val correctIndex = p.opciones.indexOfFirst { it.esCorrecta }
                            .takeIf { it >= 0 } ?: 0
                        QuestionAndroidDto(
                            id           = index + 1,
                            topic        = p.tituloContenido.ifBlank { p.subtema.ifBlank { organId } },
                            organId      = p.contenidoId,
                            body         = p.enunciado,
                            explanation  = p.subtema.ifBlank { "Revisa este concepto en tus apuntes." },
                            options      = p.opciones.sortedBy { it.letra }.map { it.texto },
                            correctIndex = correctIndex,
                            difficulty   = p.nivelDificultad.coerceIn(1, 3),
                        )
                    }

                    call.respond(ApiResponse(ok = true, data = dtos))
                }

                // ── Banco de preguntas ────────────────────────────────────
                route("/preguntas") {
                    get {
                        val contenidoId = call.request.queryParameters["contenidoId"]
                        val lista = if (contenidoId != null)
                            PreguntaRepository.getByContenido(contenidoId)
                        else
                            PreguntaRepository.getAll()
                        call.respond(ApiResponse(ok = true, data = lista))
                    }

                    post {
                        val req = call.receive<CreatePreguntaRequest>()
                        if (req.enunciado.isBlank() || req.opciones.size !in 2..5) {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Pregunta>(ok = false,
                                    error = "enunciado requerido y opciones entre 2 y 5"))
                            return@post
                        }
                        val pregunta = PreguntaRepository.create(req)
                        call.respond(HttpStatusCode.Created, ApiResponse(ok = true, data = pregunta))
                    }

                    delete("/{id}") {
                        val id = call.parameters["id"] ?: run {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Unit>(ok = false, error = "id requerido"))
                            return@delete
                        }
                        val deleted = PreguntaRepository.delete(id)
                        if (deleted) call.respond(ApiResponse<Unit>(ok = true))
                        else call.respond(HttpStatusCode.NotFound,
                            ApiResponse<Unit>(ok = false, error = "Pregunta no encontrada"))
                    }

                    put("/{id}") {
                        val id = call.parameters["id"] ?: run {
                            call.respond(HttpStatusCode.BadRequest,
                                ApiResponse<Unit>(ok = false, error = "id requerido"))
                            return@put
                        }
                        val req = call.receive<UpdatePreguntaRequest>()
                        val pregunta = PreguntaRepository.update(
                            preguntaId      = id,
                            enunciado       = req.enunciado,
                            subtema         = req.subtema,
                            nivelDificultad = req.nivelDificultad,
                            contenidoId     = req.contenidoId,
                            opciones        = req.opciones
                        )
                        call.respond(ApiResponse(ok = true, data = pregunta))
                    }
                }

                // ── Evaluaciones ──────────────────────────────────────
                route("/evaluaciones") {
                    get {
                        val search = call.request.queryParameters["search"] ?: ""
                        val nivel  = call.request.queryParameters["nivel"]?.toIntOrNull()
                        call.respond(ApiResponse(ok = true,
                            data = EvaluacionRepository.getAll(search, nivel)))
                    }

                    post {
                        val req  = call.receive<CreateEvaluacionRequest>()
                        val eval = EvaluacionRepository.create(req)
                        call.respond(HttpStatusCode.Created, ApiResponse(ok = true, data = eval))
                    }

                    put("/{id}") {
                        val id  = call.parameters["id"] ?: return@put
                        val req = call.receive<UpdateEvaluacionRequest>()
                        val ok  = EvaluacionRepository.update(id, req)
                        if (ok) call.respond(ApiResponse<Unit>(ok = true))
                        else call.respond(HttpStatusCode.NotFound,
                            ApiResponse<Unit>(ok = false, error = "Evaluación no encontrada"))
                    }

                    delete("/{id}") {
                        val id = call.parameters["id"] ?: return@delete
                        val ok = EvaluacionRepository.delete(id)
                        if (ok) call.respond(ApiResponse<Unit>(ok = true))
                        else call.respond(HttpStatusCode.NotFound,
                            ApiResponse<Unit>(ok = false, error = "Evaluación no encontrada"))
                    }

                    get("/{id}/intentos") {
                        val id = call.parameters["id"] ?: return@get
                        call.respond(ApiResponse(ok = true,
                            data = EvaluacionRepository.getIntentos(evaluacionId = id)))
                    }
                }

                // ── Intentos / Progreso ───────────────────────────────────
                route("/intentos") {
                    post {
                        val req = call.receive<SubmitIntentoRequest>()
                        IntentoRepository.submit(req)
                        call.respond(ApiResponse<Unit>(ok = true))
                    }
                }

                // ── Progreso por clase / estudiante ───────────────────────
                get("/progreso/clase/{claseId}") {
                    val claseId = call.parameters["claseId"] ?: run {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(ok = false, error = "claseId requerido"))
                        return@get
                    }
                    call.respond(ApiResponse(ok = true,
                        data = EstudianteRepository.getProgresoByClase(claseId)))
                }

                get("/progreso/estudiante/{ci}") {
                    val ci = call.parameters["ci"] ?: run {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(ok = false, error = "ci requerido"))
                        return@get
                    }
                    call.respond(ApiResponse(ok = true, data = EstudianteRepository.getProgreso(ci)))
                }

                // ── Sesiones de quiz libre ────────────────────────────────────
                post("/progreso/sesion") {
                    val req = try {
                        call.receive<SubmitSesionRequest>()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(ok = false, error = "Payload inválido: ${e.message}"))
                        return@post
                    }

                    if (req.studentId.isBlank() || req.organId.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(ok = false, error = "studentId y organId son requeridos"))
                        return@post
                    }

                    val ok = try {
                        SesionRepository.submit(req)
                    } catch (e: Exception) {
                        println("[SESION] Error al guardar sesión: ${e.message}")
                        false
                    }

                    if (ok) call.respond(ApiResponse<Unit>(ok = true))
                    else call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse<Unit>(ok = false, error = "No se pudo guardar la sesión"))
                }

                get("/progreso/sesiones/{ci}") {
                    val ci = call.parameters["ci"] ?: run {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(ok = false, error = "ci requerido"))
                        return@get
                    }
                    val sesiones = try {
                        SesionRepository.getByEstudiante(ci)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError,
                            ApiResponse<Unit>(ok = false, error = e.message ?: "Error interno"))
                        return@get
                    }
                    call.respond(ApiResponse(ok = true, data = sesiones))
                }

                get("/progreso/sesiones/clase/{claseId}") {
                    val claseId = call.parameters["claseId"] ?: run {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse<Unit>(ok = false, error = "claseId requerido"))
                        return@get
                    }
                    val resumen = try {
                        SesionRepository.getResumenByClase(claseId)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError,
                            ApiResponse<Unit>(ok = false, error = e.message ?: "Error interno"))
                        return@get
                    }
                    call.respond(ApiResponse(ok = true, data = resumen))
                }
            }
        }.start(wait = false)
        println("[HTTP] Servidor v2 activo en http://localhost:$PORT")
    }

    fun stop() { server?.stop(1000, 2000) }
}
