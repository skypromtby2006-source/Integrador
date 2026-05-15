package com.anatomia.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anatomia.app.navigation.Screen
import com.anatomia.app.ui.screens.*
import com.anatomia.app.ui.theme.AppTheme
import com.anatomia.app.ui.theme.DidactaiTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    var appTheme      by remember { mutableStateOf(AppTheme.AUTO) }
    var fontSizeIndex by remember { mutableIntStateOf(2) }

    DidactaiTheme(
        appTheme      = appTheme,
        fontSizeIndex = fontSizeIndex
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = MaterialTheme.colorScheme.background
        ) {
        val navController = rememberNavController()

        NavHost(
            navController    = navController,
            startDestination = Screen.Login.route,
            modifier         = modifier,
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Agent.route) {
                DidactaiAgentScreen(navController)
            }
            composable(Screen.Quiz.route) {
                QuizScreen(navController)
            }
            composable(Screen.QuizResults.route) {
                QuizResultsScreen(navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    appTheme      = appTheme,
                    fontSizeIndex = fontSizeIndex,
                    onThemeChange = { appTheme = it },
                    onFontChange  = { fontSizeIndex = it }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(navController)
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(navController)
            }
        }
        } // Surface
    }
}
