import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import db.DatabaseConfig
import server.HttpServer
import ui.App

fun main() = application {
    DatabaseConfig.init()
    HttpServer.start()

    Window(
        onCloseRequest = { HttpServer.stop(); exitApplication() },
        title = "Didactai · Panel del Maestro",
        state = WindowState(size = DpSize(1100.dp, 720.dp))
    ) {
        App()
    }
}
