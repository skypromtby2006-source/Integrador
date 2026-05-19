package com.anatomia.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.anatomia.app.agent.ProgressStore

fun main() = application {
    ProgressStore.init(null)
    Window(
        onCloseRequest = ::exitApplication,
        title = "AnatomiaApp",
    ) {
        App()
    }
}
