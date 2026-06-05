import db.DocenteRepository
import db.TotpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.CreateDocenteRequest
import models.DocenteInfo
import models.DocenteListItem
import org.jetbrains.exposed.sql.transactions.transaction

object ApiClient {

    suspend fun listarDocentes(): List<DocenteListItem> = withContext(Dispatchers.IO) {
        try {
            transaction {
                exec("""
                    SELECT u.usuario_id, u.primer_nombre, u.segundo_nombre,
                           u.apellido_paterno, u.apellido_materno, u.email,
                           u.estado,
                           COALESCE(d.especialidad, '')     AS especialidad,
                           COALESCE(d.titulo_academico, '') AS titulo_academico,
                           COALESCE(t.habilitado, FALSE)    AS tiene_2fa
                    FROM usuario u
                    JOIN rol r ON r.rol_id = u.rol_id
                    LEFT JOIN docente d ON d.usuario_id = u.usuario_id
                    LEFT JOIN usuario_2fa t ON t.usuario_id = u.usuario_id
                    WHERE r.nombre = 'docente'
                    ORDER BY u.apellido_paterno, u.primer_nombre
                """.trimIndent()) { rs ->
                    val lista = mutableListOf<DocenteListItem>()
                    while (rs.next()) {
                        lista.add(DocenteListItem(
                            usuarioId       = rs.getString("usuario_id"),
                            primerNombre    = rs.getString("primer_nombre"),
                            segundoNombre   = rs.getString("segundo_nombre"),
                            apellidoPaterno = rs.getString("apellido_paterno"),
                            apellidoMaterno = rs.getString("apellido_materno"),
                            email           = rs.getString("email"),
                            estado          = rs.getString("estado"),
                            especialidad    = rs.getString("especialidad"),
                            tituloAcademico = rs.getString("titulo_academico"),
                            tiene2FA        = rs.getBoolean("tiene_2fa")
                        ))
                    }
                    lista.toList()
                }
            } ?: emptyList()
        } catch (e: Exception) {
            println("[ApiClient] Error listarDocentes: ${e.message}")
            emptyList()
        }
    }

    suspend fun crearDocente(req: CreateDocenteRequest): Result<DocenteInfo> =
        withContext(Dispatchers.IO) {
            try {
                val docente = DocenteRepository.create(req)
                val secreto = TotpRepository.generarSecreto()
                TotpRepository.guardarSecreto(docente.usuarioId, secreto)
                try {
                    QrMailService.enviarQrPorCorreo(
                        destinatario  = docente.email,
                        nombreUsuario = docente.primerNombre,
                        secreto       = secreto
                    )
                    TotpRepository.marcarQrEnviado(docente.usuarioId)
                } catch (e: Exception) {
                    println("[ApiClient] Error enviando QR: ${e.message}")
                }
                Result.success(docente)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
