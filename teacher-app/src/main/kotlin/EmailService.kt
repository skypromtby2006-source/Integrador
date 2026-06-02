import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

object EmailService {

    // TODO: mover a variable de entorno en producción
    private const val GMAIL_USER     = "skypromtby2006@gmail.com"
    private const val GMAIL_APP_PASS = "jxjk wbrb iodz eoyv"

    private val props = Properties().apply {
        put("mail.smtp.auth",            "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host",            "smtp.gmail.com")
        put("mail.smtp.port",            "587")
        put("mail.smtp.ssl.trust",       "smtp.gmail.com")
    }

    fun sendCredentials(
        toEmail    : String,
        studentName: String,
        loginEmail : String,
        password   : String,
    ): Boolean {
        if (GMAIL_APP_PASS == "PENDING") {
            println("[EMAIL] App Password no configurado — envío omitido")
            return false
        }
        return try {
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication() =
                    PasswordAuthentication(GMAIL_USER, GMAIL_APP_PASS)
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(GMAIL_USER, "Didactai — Sistema Educativo"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = "Tus credenciales de acceso — Didactai"
                setContent(buildEmailHtml(studentName, loginEmail, password), "text/html; charset=utf-8")
            }

            Transport.send(message)
            println("[EMAIL] Credenciales enviadas a $toEmail")
            true
        } catch (e: Exception) {
            println("[EMAIL] Error al enviar a $toEmail: ${e.message}")
            false
        }
    }

    private fun buildEmailHtml(name: String, email: String, password: String): String = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: sans-serif; max-width: 500px; margin: 0 auto; padding: 24px;">
          <div style="background: #40798C; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 24px;">
            <h1 style="color: white; margin: 0; font-size: 24px;">didact<span style="color: #59FFCC;">ai</span></h1>
            <p style="color: rgba(255,255,255,0.8); margin: 4px 0 0; font-size: 12px; letter-spacing: 2px;">TU CUERPO · EXPLORADO</p>
          </div>
          <h2 style="color: #1A1C1D;">¡Hola, $name!</h2>
          <p style="color: #41484B;">Tu docente te ha registrado en Didactai. Aquí están tus credenciales de acceso:</p>
          <div style="background: #F2F4F4; border-radius: 8px; padding: 16px; margin: 16px 0;">
            <p style="margin: 0 0 8px;"><strong>Correo:</strong> <code style="color: #40798C;">$email</code></p>
            <p style="margin: 0;"><strong>Contraseña:</strong> <code style="color: #40798C;">$password</code></p>
          </div>
          <p style="color: #41484B; font-size: 13px;">
            Te recomendamos guardar estas credenciales en un lugar seguro.<br>
            Puedes cambiar tu contraseña desde la app en cualquier momento.
          </p>
          <p style="color: #71787B; font-size: 11px; margin-top: 24px;">
            Este mensaje fue enviado automáticamente por Didactai. No respondas a este correo.
          </p>
        </body>
        </html>
    """.trimIndent()
}
