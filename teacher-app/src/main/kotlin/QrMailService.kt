import jakarta.activation.DataHandler
import jakarta.activation.DataSource
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import qrcode.QRCode
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URLEncoder
import java.util.Properties

object QrMailService {

    private const val GMAIL_USER     = "skypromtby2006@gmail.com"
    private const val GMAIL_APP_PASS = "jxjk wbrb iodz eoyv"

    val smtpSession: Session by lazy {
        val props = Properties().apply {
            put("mail.smtp.auth",            "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host",            "smtp.gmail.com")
            put("mail.smtp.port",            "587")
            put("mail.smtp.ssl.trust",       "smtp.gmail.com")
        }
        Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(GMAIL_USER, GMAIL_APP_PASS)
        })
    }

    fun generarOtpauthUri(email: String, secreto: String, emisor: String = "Didactai"): String {
        val emailEncoded  = URLEncoder.encode(email,  "UTF-8")
        val emisorEncoded = URLEncoder.encode(emisor, "UTF-8")
        return "otpauth://totp/$emisorEncoded:$emailEncoded?secret=$secreto&issuer=$emisorEncoded"
    }

    fun generarQrPng(uri: String): ByteArray = QRCode(uri).renderToBytes("PNG")

    fun enviarQrPorCorreo(
        destinatario:  String,
        nombreUsuario: String,
        secreto:       String,
        session:       Session = smtpSession
    ) {
        val uri   = generarOtpauthUri(destinatario, secreto)
        val qrPng = generarQrPng(uri)

        val mensaje = MimeMessage(session).apply {
            setFrom(InternetAddress(GMAIL_USER, "Didactai"))
            setRecipient(Message.RecipientType.TO, InternetAddress(destinatario))
            subject = "Didactai — Configura tu autenticación de dos factores"
        }

        val multipart = MimeMultipart("related")

        val htmlPart = MimeBodyPart().apply {
            setContent("""
                <html><body style="font-family:sans-serif;color:#1a1c1d">
                  <h2>Hola, $nombreUsuario</h2>
                  <p>Tu cuenta en <strong>Didactai</strong> está lista.</p>
                  <p>Para activar la autenticación de dos factores, escanea este código QR
                     con <strong>Google Authenticator</strong> o <strong>Authy</strong>:</p>
                  <br>
                  <img src="cid:qr_didactai" alt="QR 2FA" width="220" height="220">
                  <br><br>
                  <p>O ingresa el secreto manualmente:<br>
                     <code style="background:#f0f0f0;padding:4px 8px;border-radius:4px">$secreto</code>
                  </p>
                  <p style="color:#666;font-size:12px">
                    Este código es personal y confidencial. No lo compartas con nadie.
                  </p>
                </body></html>
            """.trimIndent(), "text/html; charset=utf-8")
        }
        multipart.addBodyPart(htmlPart)

        val imgPart = MimeBodyPart().apply {
            val ds: DataSource = object : DataSource {
                override fun getInputStream(): InputStream  = ByteArrayInputStream(qrPng)
                override fun getOutputStream(): OutputStream = throw UnsupportedOperationException()
                override fun getContentType(): String       = "image/png"
                override fun getName(): String              = "qr_didactai.png"
            }
            dataHandler = DataHandler(ds)
            setHeader("Content-ID", "<qr_didactai>")
            setHeader("Content-Disposition", "inline; filename=\"qr_didactai.png\"")
        }
        multipart.addBodyPart(imgPart)

        mensaje.setContent(multipart)
        Transport.send(mensaje)
    }
}
