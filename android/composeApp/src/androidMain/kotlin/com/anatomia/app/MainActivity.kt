package com.anatomia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.anatomia.app.agent.MoodStore
import com.anatomia.app.agent.ProgressStore
import com.anatomia.app.db.DatabaseDriverFactory
import com.anatomia.app.db.DatabaseProvider
import com.anatomia.app.network.SessionStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DatabaseProvider.init(DatabaseDriverFactory(this))
        ProgressStore.init(this)
        MoodStore.init(this)
        SessionStore.init(this)

        // Ocultar status bar y barra de navegación — modo inmersivo
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            App(modifier = Modifier.fillMaxSize())
        }
    }
}
