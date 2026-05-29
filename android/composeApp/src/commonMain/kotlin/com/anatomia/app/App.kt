package com.anatomia.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anatomia.app.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anatomia.app.ui.screen.bodymodel.BodyModelScreen
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
            composable(Screen.BodyModel.route) {
                BodyModelScreen(
                    onNavigateToAgent = { navController.navigate(Screen.Agent.route) },
                    onNavigateToQuiz  = { organId -> navController.navigate(Screen.Quiz.createRoute(organId)) },
                    onNavigateBack    = { navController.popBackStack() }
                )
            }
            composable(Screen.Agent.route) {
                DidactaiAgentScreen(navController)
            }
            composable(
                route     = Screen.Quiz.route,
                arguments = listOf(navArgument("organId") { type = NavType.StringType })
            ) { backStackEntry ->
                val organId      = backStackEntry.arguments?.getString("organId") ?: "heart"
                val quizViewModel: QuizViewModel = viewModel(backStackEntry)
                QuizScreen(
                    navController = navController,
                    organId       = organId,
                    viewModel     = quizViewModel,
                )
            }
            composable(Screen.QuizResults.route) { entry ->
                val quizEntry    = remember(entry) { navController.getBackStackEntry(Screen.Quiz.route) }
                val quizViewModel: QuizViewModel = viewModel(quizEntry)
                QuizResultsScreen(
                    navController = navController,
                    viewModel     = quizViewModel,
                )
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
