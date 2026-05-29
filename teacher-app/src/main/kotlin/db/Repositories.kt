package db

import models.*
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

// ── AuthRepository ────────────────────────────────────────────────────────────
object AuthRepository {

    fun loginDocente(ci: String, password: String): DocenteSession? = transaction {
        val hash = DigestUtils.sha256Hex(password)
        (UsuarioTable innerJoin DocenteTable)
            .select {
                (UsuarioTable.usuarioId eq ci) and
                (UsuarioTable.passwordHash eq hash) and
                (UsuarioTable.rol eq "docente")
            }
            .singleOrNull()
            ?.let {
                DocenteSession(
                    usuarioId = it[UsuarioTable.usuarioId],
                    nombre    = it[UsuarioTable.nombre],
                    apellido  = it[UsuarioTable.apellido],
                    email     = it[UsuarioTable.email]
                )
            }
    }

    fun loginEstudiante(email: String, password: String): LoginEstudianteResponse? = transaction {
        val hash = DigestUtils.sha256Hex(password)
        (UsuarioTable innerJoin EstudianteTable)
            .leftJoin(ClaseTable, { EstudianteTable.claseId }, { ClaseTable.claseId })
            .select {
                (UsuarioTable.email eq email) and
                (UsuarioTable.passwordHash eq hash) and
                (UsuarioTable.rol eq "estudiante") and
                (UsuarioTable.estado eq "activo")
            }
            .singleOrNull()
            ?.let {
                LoginEstudianteResponse(
                    usuarioId   = it[UsuarioTable.usuarioId],
                    nombre      = it[UsuarioTable.nombre],
                    apellido    = it[UsuarioTable.apellido],
                    email       = it[UsuarioTable.email],
                    claseId     = it[EstudianteTable.claseId]?.toString() ?: "",
                    claseNombre = try { it[ClaseTable.nombre] } catch (_: Exception) { "" },
                    grado       = it[EstudianteTable.grado] ?: "",
                    nivelXp     = it[EstudianteTable.nivelXp]
                )
            }
    }
}

// ── ClaseRepository ───────────────────────────────────────────────────────────
object ClaseRepository {

    fun getAll(docenteId: String): List<Clase> = transaction {
        val counts = EstudianteTable
            .slice(EstudianteTable.claseId, EstudianteTable.usuarioId.count())
            .selectAll()
            .groupBy(EstudianteTable.claseId)
            .associate { it[EstudianteTable.claseId]?.toString() to it[EstudianteTable.usuarioId.count()].toInt() }

        ClaseTable
            .select { ClaseTable.docenteId eq docenteId }
            .orderBy(ClaseTable.grado, SortOrder.ASC)
            .map { row ->
                val id = row[ClaseTable.claseId].toString()
                Clase(
                    claseId      = id,
                    nombre       = row[ClaseTable.nombre],
                    grado        = row[ClaseTable.grado],
                    turno        = row[ClaseTable.turno],
                    anioEscolar  = row[ClaseTable.anioEscolar],
                    totalAlumnos = counts[id] ?: 0
                )
            }
    }
}

// ── EstudianteRepository ──────────────────────────────────────────────────────
object EstudianteRepository {

    fun getAll(): List<Estudiante> = transaction {
        (UsuarioTable innerJoin EstudianteTable)
            .leftJoin(ClaseTable, { EstudianteTable.claseId }, { ClaseTable.claseId })
            .select { UsuarioTable.rol eq "estudiante" }
            .orderBy(UsuarioTable.apellido, SortOrder.ASC)
            .map { it.toEstudiante() }
    }

    fun getByClase(claseId: String): List<Estudiante> = transaction {
        val uuid = UUID.fromString(claseId)
        (UsuarioTable innerJoin EstudianteTable)
            .leftJoin(ClaseTable, { EstudianteTable.claseId }, { ClaseTable.claseId })
            .select { EstudianteTable.claseId eq uuid }
            .orderBy(UsuarioTable.apellido, SortOrder.ASC)
            .map { it.toEstudiante() }
    }

    fun create(req: CreateEstudianteRequest): Estudiante = transaction {
        val claseUuid = UUID.fromString(req.claseId)

        val clase = ClaseTable.select { ClaseTable.claseId eq claseUuid }.single()
        val grado = clase[ClaseTable.grado]

        val emailBase = "${req.nombre} ${req.apellido}"
            .lowercase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n")
            .trim().split(" ").filter { it.isNotBlank() }.joinToString(".")

        var email = "$emailBase.$grado@didactai.edu"
        var counter = 1
        while (UsuarioTable.select { UsuarioTable.email eq email }.count() > 0L) {
            email = "$emailBase$counter.$grado@didactai.edu"
            counter++
        }

        val hash = DigestUtils.sha256Hex(req.password)

        UsuarioTable.insert {
            it[usuarioId]          = req.ci
            it[nombre]             = req.nombre
            it[apellido]           = req.apellido
            it[UsuarioTable.email] = email
            it[passwordHash]       = hash
            it[rol]                = "estudiante"
            if (req.fechaNacimiento.isNotBlank()) {
                it[fechaNacimiento] = java.time.LocalDate.parse(req.fechaNacimiento)
            }
        }

        EstudianteTable.insert {
            it[usuarioId]                = req.ci
            it[EstudianteTable.claseId]  = claseUuid
            it[EstudianteTable.grado]    = grado
            it[turno]                    = clase[ClaseTable.turno]
        }

        (UsuarioTable innerJoin EstudianteTable)
            .leftJoin(ClaseTable, { EstudianteTable.claseId }, { ClaseTable.claseId })
            .select { UsuarioTable.usuarioId eq req.ci }
            .single()
            .toEstudiante()
    }

    fun update(
        ci: String,
        nombre: String,
        apellido: String,
        claseId: String,
        fechaNacimiento: String = "",
        newPassword: String?
    ): Estudiante = transaction {
        val claseUuid = UUID.fromString(claseId)
        val clase = ClaseTable.select { ClaseTable.claseId eq claseUuid }.single()
        val grado = clase[ClaseTable.grado]

        UsuarioTable.update({ UsuarioTable.usuarioId eq ci }) { row ->
            row[UsuarioTable.nombre]   = nombre
            row[UsuarioTable.apellido] = apellido
            if (newPassword != null) {
                row[passwordHash] = DigestUtils.sha256Hex(newPassword)
            }
            if (fechaNacimiento.isNotBlank()) {
                row[UsuarioTable.fechaNacimiento] = java.time.LocalDate.parse(fechaNacimiento)
            }
            row[updatedAt] = java.time.LocalDateTime.now()
        }

        EstudianteTable.update({ EstudianteTable.usuarioId eq ci }) { row ->
            row[EstudianteTable.claseId] = claseUuid
            row[EstudianteTable.grado]   = grado
            row[turno]                   = clase[ClaseTable.turno]
        }

        (UsuarioTable innerJoin EstudianteTable)
            .leftJoin(ClaseTable, { EstudianteTable.claseId }, { ClaseTable.claseId })
            .select { UsuarioTable.usuarioId eq ci }
            .single()
            .toEstudiante()
    }

    fun setEstado(ci: String, estado: String): Boolean = transaction {
        UsuarioTable.update({ UsuarioTable.usuarioId eq ci }) {
            it[UsuarioTable.estado] = estado
            it[updatedAt] = java.time.LocalDateTime.now()
        } > 0
    }

    fun delete(ci: String): Boolean = transaction {
        UsuarioTable.deleteWhere { usuarioId eq ci } > 0
    }

    fun getProgreso(ci: String): EstudianteProgreso = transaction {
        val usuario    = UsuarioTable.select { UsuarioTable.usuarioId eq ci }.single()
        val estudiante = EstudianteTable.select { EstudianteTable.usuarioId eq ci }.single()

        val intentos = IntentoEvaluacionTable
            .select { IntentoEvaluacionTable.usuarioId eq ci }
            .toList()

        val totalCorrectas = intentos.sumOf { it[IntentoEvaluacionTable.correctas].toInt() }
        val totalPreguntas = intentos.sumOf {
            it[IntentoEvaluacionTable.correctas].toInt() +
            it[IntentoEvaluacionTable.incorrectas].toInt() +
            it[IntentoEvaluacionTable.saltadas].toInt()
        }

        EstudianteProgreso(
            usuarioId      = ci,
            nombre         = usuario[UsuarioTable.nombre],
            apellido       = usuario[UsuarioTable.apellido],
            email          = usuario[UsuarioTable.email],
            nivelXp        = estudiante[EstudianteTable.nivelXp],
            totalIntentos  = intentos.size,
            totalCorrectas = totalCorrectas,
            totalPreguntas = totalPreguntas
        )
    }

    fun getProgresoByClase(claseId: String): List<EstudianteProgreso> = transaction {
        val uuid = UUID.fromString(claseId)
        EstudianteTable
            .select { EstudianteTable.claseId eq uuid }
            .map { getProgreso(it[EstudianteTable.usuarioId]) }
    }

    private fun ResultRow.toEstudiante() = Estudiante(
        usuarioId       = this[UsuarioTable.usuarioId],
        nombre          = this[UsuarioTable.nombre],
        apellido        = this[UsuarioTable.apellido],
        email           = this[UsuarioTable.email],
        claseId         = this[EstudianteTable.claseId]?.toString() ?: "",
        claseNombre     = try { this[ClaseTable.nombre] } catch (_: Exception) { "" },
        grado           = this[EstudianteTable.grado] ?: "",
        nivelXp         = this[EstudianteTable.nivelXp],
        estado          = this[UsuarioTable.estado],
        fechaNacimiento = this[UsuarioTable.fechaNacimiento]?.toString() ?: ""
    )
}

// ── ContenidoRepository ───────────────────────────────────────────────────────
object ContenidoRepository {

    fun getAll(): List<ContenidoBiologico> = transaction {
        // Nota: .select { condition } en Exposed 0.44.1 (no .selectAll().where{})
        ContenidoBiologicoTable
            .select { ContenidoBiologicoTable.activo eq true }
            .map {
                ContenidoBiologico(
                    contenidoId     = it[ContenidoBiologicoTable.contenidoId].toString(),
                    titulo          = it[ContenidoBiologicoTable.titulo],
                    descripcion     = it[ContenidoBiologicoTable.descripcion] ?: "",
                    categoria       = it[ContenidoBiologicoTable.categoria] ?: "",
                    nivelDificultad = it[ContenidoBiologicoTable.nivelDificultad].toInt()
                )
            }
    }

    fun create(titulo: String, descripcion: String, categoria: String,
               nivelDificultad: Int, docenteId: String): ContenidoBiologico = transaction {
        val newId = UUID.randomUUID()

        ContenidoTable.insert {
            it[contenidoId] = newId
            it[tipo]        = "biologico"
        }

        ContenidoBiologicoTable.insert {
            it[contenidoId]                        = newId
            it[ContenidoBiologicoTable.titulo]     = titulo
            it[ContenidoBiologicoTable.descripcion]= descripcion
            it[ContenidoBiologicoTable.categoria]  = categoria
            it[ContenidoBiologicoTable.nivelDificultad] = nivelDificultad.toShort()
            it[creadoPor]                          = docenteId
        }

        ContenidoBiologico(newId.toString(), titulo, descripcion, categoria, nivelDificultad)
    }
}

// ── PreguntaRepository ────────────────────────────────────────────────────────
object PreguntaRepository {

    fun getAll(): List<Pregunta> = transaction {
        BancoPreguntaTable
            .leftJoin(ContenidoBiologicoTable,
                { BancoPreguntaTable.contenidoId },
                { ContenidoBiologicoTable.contenidoId })
            .selectAll()
            .map { it.toPregunta() }
    }

    fun getByContenido(contenidoId: String): List<Pregunta> = transaction {
        val uuid = UUID.fromString(contenidoId)
        BancoPreguntaTable
            .leftJoin(ContenidoBiologicoTable,
                { BancoPreguntaTable.contenidoId },
                { ContenidoBiologicoTable.contenidoId })
            .select { BancoPreguntaTable.contenidoId eq uuid }
            .map { it.toPregunta() }
    }

    fun create(req: CreatePreguntaRequest): Pregunta = transaction {
        val contenidoUuid = UUID.fromString(req.contenidoId)
        val newPreguntaId = UUID.randomUUID()

        BancoPreguntaTable.insert {
            it[preguntaId]      = newPreguntaId
            it[contenidoId]     = contenidoUuid
            it[enunciado]       = req.enunciado
            it[subtema]         = req.subtema.takeIf { s -> s.isNotBlank() }
            it[nivelDificultad] = req.nivelDificultad.toShort()
            it[creadoPor]       = req.docenteId
        }

        req.opciones.forEach { opcion ->
            OpcionPreguntaTable.insert {
                it[opcionId]   = UUID.randomUUID()
                it[preguntaId] = newPreguntaId
                it[texto]      = opcion.texto
                it[letra]      = opcion.letra.first()
                it[esCorrecta] = opcion.esCorrecta
                it[orden]      = when (opcion.letra) { "A" -> 0; "B" -> 1; "C" -> 2; else -> 3 }.toShort()
            }
        }

        BancoPreguntaTable
            .leftJoin(ContenidoBiologicoTable,
                { BancoPreguntaTable.contenidoId },
                { ContenidoBiologicoTable.contenidoId })
            .select { BancoPreguntaTable.preguntaId eq newPreguntaId }
            .single()
            .toPregunta()
    }

    fun update(
        preguntaId:      String,
        enunciado:       String,
        subtema:         String,
        nivelDificultad: Int,
        contenidoId:     String,
        opciones:        List<models.CreateOpcionRequest>
    ): Pregunta = transaction {
        val uuid          = UUID.fromString(preguntaId)
        val contenidoUuid = UUID.fromString(contenidoId)

        BancoPreguntaTable.update({ BancoPreguntaTable.preguntaId eq uuid }) { row ->
            row[BancoPreguntaTable.enunciado]       = enunciado
            row[BancoPreguntaTable.subtema]         = subtema.takeIf { it.isNotBlank() }
            row[BancoPreguntaTable.nivelDificultad] = nivelDificultad.toShort()
            row[BancoPreguntaTable.contenidoId]     = contenidoUuid
        }

        OpcionPreguntaTable.deleteWhere { OpcionPreguntaTable.preguntaId eq uuid }

        opciones.forEach { opcion ->
            OpcionPreguntaTable.insert {
                it[OpcionPreguntaTable.opcionId]   = UUID.randomUUID()
                it[OpcionPreguntaTable.preguntaId] = uuid
                it[texto]                           = opcion.texto
                it[letra]                           = opcion.letra.first()
                it[esCorrecta]                      = opcion.esCorrecta
                it[orden] = when (opcion.letra) {
                    "A" -> 0; "B" -> 1; "C" -> 2; else -> 3
                }.toShort()
            }
        }

        BancoPreguntaTable
            .leftJoin(ContenidoBiologicoTable,
                { BancoPreguntaTable.contenidoId },
                { ContenidoBiologicoTable.contenidoId })
            .select { BancoPreguntaTable.preguntaId eq uuid }
            .single()
            .toPregunta()
    }

    fun delete(preguntaId: String): Boolean = transaction {
        val uuid = UUID.fromString(preguntaId)
        BancoPreguntaTable.deleteWhere { BancoPreguntaTable.preguntaId eq uuid } > 0
    }

    private fun ResultRow.toPregunta(): Pregunta {
        val pid = this[BancoPreguntaTable.preguntaId]
        val opciones = OpcionPreguntaTable
            .select { OpcionPreguntaTable.preguntaId eq pid }
            .orderBy(OpcionPreguntaTable.orden, SortOrder.ASC)
            .map { o ->
                Opcion(
                    opcionId   = o[OpcionPreguntaTable.opcionId].toString(),
                    letra      = o[OpcionPreguntaTable.letra].toString(),
                    texto      = o[OpcionPreguntaTable.texto],
                    esCorrecta = o[OpcionPreguntaTable.esCorrecta]
                )
            }
        return Pregunta(
            preguntaId      = pid.toString(),
            contenidoId     = this[BancoPreguntaTable.contenidoId].toString(),
            tituloContenido = try { this[ContenidoBiologicoTable.titulo] } catch (_: Exception) { "" },
            enunciado       = this[BancoPreguntaTable.enunciado],
            subtema         = this[BancoPreguntaTable.subtema] ?: "",
            nivelDificultad = this[BancoPreguntaTable.nivelDificultad].toInt(),
            opciones        = opciones
        )
    }
}

// ── EvaluacionRepository ──────────────────────────────────────────────────────
object EvaluacionRepository {

    fun getAll(search: String = "", nivelDificultad: Int? = null): List<models.Evaluacion> = transaction {
        (EvaluacionTable leftJoin ContenidoBiologicoTable)
            .selectAll()
            .let { query ->
                if (search.isNotBlank())
                    query.andWhere {
                        (EvaluacionTable.titulo.lowerCase() like "%${search.lowercase()}%") or
                        (ContenidoBiologicoTable.titulo.lowerCase() like "%${search.lowercase()}%")
                    }
                else query
            }
            .let { query ->
                if (nivelDificultad != null)
                    query.andWhere { EvaluacionTable.nivelDificultad eq nivelDificultad.toShort() }
                else query
            }
            .orderBy(EvaluacionTable.createdAt, SortOrder.DESC)
            .map { row ->
                val id = row[EvaluacionTable.evaluacionId].toString()
                val totalIntentos = IntentoEvaluacionTable
                    .select { IntentoEvaluacionTable.evaluacionId eq row[EvaluacionTable.evaluacionId] }
                    .count().toInt()
                models.Evaluacion(
                    evaluacionId    = id,
                    contenidoId     = row[EvaluacionTable.contenidoId].toString(),
                    tituloContenido = try { row[ContenidoBiologicoTable.titulo] } catch (_: Exception) { "" },
                    titulo          = row[EvaluacionTable.titulo],
                    nivelDificultad = row[EvaluacionTable.nivelDificultad].toInt(),
                    totalIntentos   = totalIntentos,
                    creadoEn        = row[EvaluacionTable.createdAt].toString()
                )
            }
    }

    fun create(req: models.CreateEvaluacionRequest): models.Evaluacion = transaction {
        val newId = UUID.randomUUID()
        EvaluacionTable.insert {
            it[evaluacionId]    = newId
            it[contenidoId]     = UUID.fromString(req.contenidoId)
            it[titulo]          = req.titulo
            it[nivelDificultad] = req.nivelDificultad.toShort()
            it[creadoPor]       = req.docenteId
        }
        getAll().first { it.evaluacionId == newId.toString() }
    }

    fun update(id: String, req: models.UpdateEvaluacionRequest): Boolean = transaction {
        EvaluacionTable.update({ EvaluacionTable.evaluacionId eq UUID.fromString(id) }) {
            it[titulo]          = req.titulo
            it[nivelDificultad] = req.nivelDificultad.toShort()
            it[contenidoId]     = UUID.fromString(req.contenidoId)
        } > 0
    }

    fun delete(id: String): Boolean = transaction {
        EvaluacionTable.deleteWhere { EvaluacionTable.evaluacionId eq UUID.fromString(id) } > 0
    }

    fun getIntentos(evaluacionId: String? = null, estudianteId: String? = null): List<models.IntentoResumen> = transaction {
        IntentoEvaluacionTable
            .leftJoin(EvaluacionTable,
                { IntentoEvaluacionTable.evaluacionId },
                { EvaluacionTable.evaluacionId })
            .leftJoin(UsuarioTable,
                { IntentoEvaluacionTable.usuarioId },
                { UsuarioTable.usuarioId })
            .selectAll()
            .let { q ->
                if (evaluacionId != null)
                    q.andWhere { IntentoEvaluacionTable.evaluacionId eq UUID.fromString(evaluacionId) }
                else q
            }
            .let { q ->
                if (estudianteId != null)
                    q.andWhere { IntentoEvaluacionTable.usuarioId eq estudianteId }
                else q
            }
            .orderBy(IntentoEvaluacionTable.iniciadoEn, SortOrder.DESC)
            .map { row ->
                models.IntentoResumen(
                    intentoId        = row[IntentoEvaluacionTable.intentoId].toString(),
                    evaluacionId     = row[IntentoEvaluacionTable.evaluacionId].toString(),
                    tituloEvaluacion = try { row[EvaluacionTable.titulo] } catch (_: Exception) { "" },
                    usuarioId        = row[IntentoEvaluacionTable.usuarioId],
                    nombreEstudiante = try {
                        "${row[UsuarioTable.nombre]} ${row[UsuarioTable.apellido]}"
                    } catch (_: Exception) { "" },
                    correctas    = row[IntentoEvaluacionTable.correctas].toInt(),
                    incorrectas  = row[IntentoEvaluacionTable.incorrectas].toInt(),
                    saltadas     = row[IntentoEvaluacionTable.saltadas].toInt(),
                    puntajeTotal = row[IntentoEvaluacionTable.puntajeTotal]?.toDouble() ?: 0.0,
                    iniciadoEn   = row[IntentoEvaluacionTable.iniciadoEn].toString(),
                    finalizadoEn = row[IntentoEvaluacionTable.finalizadoEn]?.toString() ?: ""
                )
            }
    }
}

// ── SesionRepository ──────────────────────────────────────────────────────────
object SesionRepository {

    fun submit(req: SubmitSesionRequest): Boolean = transaction {
        val correctas   = req.answers.count { it.wasCorrect }.toShort()
        val incorrectas = req.answers.count { !it.wasCorrect }.toShort()
        val puntaje     = if (req.answers.isNotEmpty())
            correctas.toFloat() / req.answers.size * 100f else 0f

        val ts = java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(req.timestamp),
            java.time.ZoneId.systemDefault()
        )

        SesionQuizTable.insert {
            it[usuarioId]                   = req.studentId
            it[organId]                     = req.organId
            it[SesionQuizTable.correctas]   = correctas
            it[SesionQuizTable.incorrectas] = incorrectas
            it[saltadas]                    = 0
            it[puntajeTotal]                = puntaje
            it[realizadoEn]                 = ts
        }
        true
    }

    fun getByEstudiante(ci: String): List<SesionQuiz> = transaction {
        SesionQuizTable
            .select { SesionQuizTable.usuarioId eq ci }
            .orderBy(SesionQuizTable.realizadoEn, SortOrder.DESC)
            .map { row ->
                SesionQuiz(
                    sesionId     = row[SesionQuizTable.sesionId].toString(),
                    organId      = row[SesionQuizTable.organId],
                    correctas    = row[SesionQuizTable.correctas].toInt(),
                    incorrectas  = row[SesionQuizTable.incorrectas].toInt(),
                    saltadas     = row[SesionQuizTable.saltadas].toInt(),
                    puntajeTotal = row[SesionQuizTable.puntajeTotal]?.toDouble() ?: 0.0,
                    realizadoEn  = row[SesionQuizTable.realizadoEn].toString(),
                )
            }
    }

    fun getResumenByClase(claseId: String): List<SesionResumenEstudiante> = transaction {
        val uuid        = UUID.fromString(claseId)
        val estudiantes = EstudianteTable
            .innerJoin(UsuarioTable, { EstudianteTable.usuarioId }, { UsuarioTable.usuarioId })
            .select { EstudianteTable.claseId eq uuid }
            .map { Triple(it[UsuarioTable.usuarioId], it[UsuarioTable.nombre], it[UsuarioTable.apellido]) }

        estudiantes.map { (ci, nombre, apellido) ->
            val usuario = UsuarioTable.select { UsuarioTable.usuarioId eq ci }.singleOrNull()
            SesionResumenEstudiante(
                usuarioId = ci,
                nombre    = nombre,
                apellido  = apellido,
                email     = usuario?.get(UsuarioTable.email) ?: "",
                sesiones  = getByEstudiante(ci),
            )
        }
    }
}

// ── IntentoRepository ─────────────────────────────────────────────────────────
object IntentoRepository {

    fun submit(req: SubmitIntentoRequest): Boolean = transaction {
        val intentoId    = UUID.randomUUID()
        val evaluacionId = UUID.fromString(req.evaluacionId)
        val correctas    = req.resultados.count { it.esCorrecta }.toShort()
        val incorrectas  = req.resultados.count { !it.esCorrecta }.toShort()

        IntentoEvaluacionTable.insert {
            it[IntentoEvaluacionTable.intentoId]    = intentoId
            it[IntentoEvaluacionTable.evaluacionId] = evaluacionId
            it[IntentoEvaluacionTable.usuarioId]    = req.usuarioId
            it[IntentoEvaluacionTable.correctas]    = correctas
            it[IntentoEvaluacionTable.incorrectas]  = incorrectas
            it[IntentoEvaluacionTable.saltadas]     = 0
            it[IntentoEvaluacionTable.puntajeTotal] = if (req.resultados.isNotEmpty())
                correctas.toFloat() / req.resultados.size * 100f else 0f
        }

        req.resultados.forEach { res ->
            ResultadoPreguntaTable.insert {
                it[resultadoId]       = UUID.randomUUID()
                it[ResultadoPreguntaTable.intentoId]  = intentoId
                it[ResultadoPreguntaTable.preguntaId] = UUID.fromString(res.preguntaId)
                it[esCorrecta]        = res.esCorrecta
                it[tiempoRespuestaMs] = res.tiempoRespuestaMs
                it[puntaje]           = if (res.esCorrecta) 1f else 0f
            }
        }
        true
    }
}
