package db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object UsuarioTable : Table("usuario") {
    val usuarioId       = varchar("usuario_id", 15)
    // ── Nombre atomizado (4 campos) ──────────────────────────────────────────
    val primerNombre    = varchar("primer_nombre", 60)
    val segundoNombre   = varchar("segundo_nombre", 60).nullable()
    val apellidoPaterno = varchar("apellido_paterno", 60)
    val apellidoMaterno = varchar("apellido_materno", 60).nullable()
    // ── Resto de campos ───────────────────────────────────────────────────────
    val email           = varchar("email", 120)
    val passwordHash    = text("password_hash")
    val rol             = varchar("rol", 20)
    val rolId           = integer("rol_id").nullable()
    val avatarUrl       = text("avatar_url").nullable()
    val correoPersonal  = varchar("correo_personal", 200).nullable()
    val estado          = varchar("estado", 20).default("activo")
    val fechaNacimiento = date("fecha_nacimiento").nullable()
    val createdAt       = datetime("created_at")
    val updatedAt       = datetime("updated_at")
    override val primaryKey = PrimaryKey(usuarioId)
}

object DocenteTable : Table("docente") {
    val usuarioId           = varchar("usuario_id", 15).references(UsuarioTable.usuarioId)
    val especialidad        = varchar("especialidad", 80).nullable()
    val tituloAcademico     = varchar("titulo_academico", 80).nullable()
    val puedeCrearContenido = bool("puede_crear_contenido").default(false)
    override val primaryKey = PrimaryKey(usuarioId)
}

object ClaseTable : Table("clase") {
    val claseId     = uuid("clase_id")
    val docenteId   = varchar("docente_id", 15).references(UsuarioTable.usuarioId)
    val nombre      = varchar("nombre", 80)
    val grado       = varchar("grado", 20)
    val turno       = varchar("turno", 20)
    val anioEscolar = varchar("anio_escolar", 9)
    val activa      = bool("activa").default(true)
    override val primaryKey = PrimaryKey(claseId)
}

object EstudianteTable : Table("estudiante") {
    val usuarioId       = varchar("usuario_id", 15).references(UsuarioTable.usuarioId)
    val claseId         = uuid("clase_id").references(ClaseTable.claseId).nullable()
    val grado           = varchar("grado", 20).nullable()
    val turno           = varchar("turno", 20).nullable()
    val fechaNacimiento = varchar("fecha_nacimiento", 20).nullable()
    val nivelXp         = integer("nivel_xp").default(0)
    override val primaryKey = PrimaryKey(usuarioId)
}

object ContenidoTable : Table("contenido") {
    val contenidoId = uuid("contenido_id")
    val tipo        = varchar("tipo", 20)
    val createdAt   = datetime("created_at")
    override val primaryKey = PrimaryKey(contenidoId)
}

object ContenidoBiologicoTable : Table("contenido_biologico") {
    val contenidoId     = uuid("contenido_id").references(ContenidoTable.contenidoId)
    val titulo          = varchar("titulo", 120)
    val descripcion     = text("descripcion").nullable()
    val categoria       = varchar("categoria", 60).nullable()
    val nivelDificultad = short("nivel_dificultad").default(1)
    val gradoObjetivo   = varchar("grado_objetivo", 20).nullable()
    val textoLectura    = text("texto_lectura").nullable()
    val activo          = bool("activo").default(true)
    val creadoPor       = varchar("creado_por", 15).references(UsuarioTable.usuarioId)
    val createdAt       = datetime("created_at")
    override val primaryKey = PrimaryKey(contenidoId)
}

object BancoPreguntaTable : Table("banco_pregunta") {
    val preguntaId      = uuid("pregunta_id")
    val contenidoId     = uuid("contenido_id").references(ContenidoBiologicoTable.contenidoId)
    val enunciado       = text("enunciado")
    val tipo            = varchar("tipo", 30).default("opcion_multiple")
    val subtema         = varchar("subtema", 80).nullable()
    val nivelDificultad = short("nivel_dificultad").default(1)
    val creadoPor       = varchar("creado_por", 15).references(UsuarioTable.usuarioId)
    val createdAt       = datetime("created_at")
    override val primaryKey = PrimaryKey(preguntaId)
}

object OpcionPreguntaTable : Table("opcion_pregunta") {
    val opcionId   = uuid("opcion_id")
    val preguntaId = uuid("pregunta_id").references(BancoPreguntaTable.preguntaId)
    val texto      = text("texto")
    val letra      = char("letra")
    val esCorrecta = bool("es_correcta").default(false)
    val orden      = short("orden").default(0)
    override val primaryKey = PrimaryKey(opcionId)
}

object EvaluacionTable : Table("evaluacion") {
    val evaluacionId    = uuid("evaluacion_id")
    val contenidoId     = uuid("contenido_id").references(ContenidoBiologicoTable.contenidoId)
    val titulo          = varchar("titulo", 120)
    val nivelDificultad = short("nivel_dificultad").default(1)
    val creadoPor       = varchar("creado_por", 15).references(UsuarioTable.usuarioId)
    val createdAt       = datetime("created_at")
    override val primaryKey = PrimaryKey(evaluacionId)
}

object IntentoEvaluacionTable : Table("intento_evaluacion") {
    val intentoId    = uuid("intento_id")
    val evaluacionId = uuid("evaluacion_id")
    val usuarioId    = varchar("usuario_id", 15).references(UsuarioTable.usuarioId)
    val puntajeTotal = float("puntaje_total").nullable()
    val correctas    = short("correctas").default(0)
    val incorrectas  = short("incorrectas").default(0)
    val saltadas     = short("saltadas").default(0)
    val iniciadoEn   = datetime("iniciado_en")
    val finalizadoEn = datetime("finalizado_en").nullable()
    override val primaryKey = PrimaryKey(intentoId)
}

object ResultadoPreguntaTable : Table("resultado_pregunta") {
    val resultadoId       = uuid("resultado_id")
    val intentoId         = uuid("intento_id").references(IntentoEvaluacionTable.intentoId)
    val preguntaId        = uuid("pregunta_id").references(BancoPreguntaTable.preguntaId)
    val esCorrecta        = bool("es_correcta").nullable()
    val puntaje           = float("puntaje").default(0f)
    val tiempoRespuestaMs = integer("tiempo_respuesta_ms").nullable()
    val timestamp         = datetime("timestamp")
    override val primaryKey = PrimaryKey(resultadoId)
}

object SesionQuizTable : Table("sesion_quiz") {
    val sesionId     = uuid("sesion_id").autoGenerate()
    val usuarioId    = varchar("usuario_id", 20).references(UsuarioTable.usuarioId)
    val organId      = varchar("organ_id", 50)
    val correctas    = short("correctas").default(0)
    val incorrectas  = short("incorrectas").default(0)
    val saltadas     = short("saltadas").default(0)
    val puntajeTotal = float("puntaje_total").nullable()
    val realizadoEn  = datetime("realizado_en").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(sesionId)
}

object DatabaseConfig {
    private const val URL      = "jdbc:postgresql://localhost:5432/didactai"
    private const val USER     = "postgres"
    private const val PASSWORD = "postgres"

    fun init() {
        Database.connect(url = URL, driver = "org.postgresql.Driver",
            user = USER, password = PASSWORD)
        println("[DB] Conectado a PostgreSQL — esquema Didactai v2")
        seedContenidoBiologico()
    }

    private fun seedContenidoBiologico() {
        transaction {
            val count = ContenidoBiologicoTable.selectAll().count()
            if (count > 0L) {
                println("[SEED] Contenido biológico ya existe ($count registros) — omitiendo seed")
                return@transaction
            }

            val docenteId = "12395472"
            val now = java.time.LocalDateTime.now()

            val organs = listOf(
                Triple(
                    "Corazón humano",
                    "Anatomía y fisiología del corazón: cámaras, válvulas y ciclo cardíaco.",
                    "Circulatorio"
                ),
                Triple(
                    "Pulmones",
                    "Anatomía del sistema respiratorio: bronquios, alvéolos e intercambio gaseoso.",
                    "Respiratorio"
                ),
                Triple(
                    "Riñones",
                    "Anatomía del sistema urinario: nefronas, filtración y producción de orina.",
                    "Urinario"
                ),
            )

            organs.forEach { (titulo, descripcion, categoria) ->
                val newId = java.util.UUID.randomUUID()

                ContenidoTable.insert {
                    it[contenidoId] = newId
                    it[tipo]        = "biologico"
                    it[createdAt]   = now
                }

                ContenidoBiologicoTable.insert {
                    it[contenidoId]                      = newId
                    it[ContenidoBiologicoTable.titulo]       = titulo
                    it[ContenidoBiologicoTable.descripcion]  = descripcion
                    it[ContenidoBiologicoTable.categoria]    = categoria
                    it[nivelDificultad]                  = 1
                    it[activo]                           = true
                    it[creadoPor]                        = docenteId
                    it[ContenidoBiologicoTable.createdAt]    = now
                }

                println("[SEED] Contenido insertado: $titulo")
            }

            println("[SEED] 3 contenidos biológicos insertados correctamente")
        }
    }
}
