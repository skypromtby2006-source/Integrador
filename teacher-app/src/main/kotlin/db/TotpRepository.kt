package db

import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.util.Date
import java.util.concurrent.TimeUnit

object TotpRepository {

    fun generarSecreto(): String {
        val bytes = ByteArray(20)
        SecureRandom().nextBytes(bytes)
        return Base32().encodeToString(bytes).trimEnd('=')
    }

    fun guardarSecreto(usuarioId: String, secreto: String) {
        transaction {
            exec("""
                INSERT INTO usuario_2fa (usuario_id, totp_secret, habilitado, qr_enviado)
                VALUES ('$usuarioId', '$secreto', FALSE, FALSE)
                ON CONFLICT (usuario_id) DO UPDATE
                    SET totp_secret = EXCLUDED.totp_secret,
                        habilitado  = FALSE,
                        qr_enviado  = FALSE
            """.trimIndent())
        }
    }

    fun obtenerSecreto(usuarioId: String): String? {
        return transaction {
            exec("SELECT totp_secret FROM usuario_2fa WHERE usuario_id = '$usuarioId'") { rs ->
                if (rs.next()) listOf(rs.getString("totp_secret")) else emptyList()
            }
        }?.firstOrNull()
    }

    fun marcarQrEnviado(usuarioId: String) {
        transaction {
            exec("""
                UPDATE usuario_2fa
                SET qr_enviado = TRUE, habilitado = TRUE
                WHERE usuario_id = '$usuarioId'
            """.trimIndent())
        }
    }

    fun verificarCodigo(usuarioId: String, codigoIngresado: String): Boolean {
        val secreto = obtenerSecreto(usuarioId) ?: return false
        val config = TimeBasedOneTimePasswordConfig(
            codeDigits    = 6,
            hmacAlgorithm = HmacAlgorithm.SHA1,
            timeStep      = 30,
            timeStepUnit  = TimeUnit.SECONDS
        )
        val padding    = "=".repeat((8 - secreto.length % 8) % 8)
        val secretBytes = Base32().decode(secreto + padding)
        val generator  = TimeBasedOneTimePasswordGenerator(secretBytes, config)
        val ahora      = System.currentTimeMillis()
        val codigoActual   = generator.generate(Date(ahora))
        val codigoAnterior = generator.generate(Date(ahora - 30_000))
        return codigoIngresado.trim() == codigoActual ||
               codigoIngresado.trim() == codigoAnterior
    }
}
