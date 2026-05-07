package com.anatomia.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anatomia.app.ui.screen.agent.AgentScreen
import com.anatomia.app.ui.screen.bodymodel.BodyModelScreen

@Composable
fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "body_model",
        modifier = modifier,
    ) {
        composable("body_model") {
            BodyModelScreen(
                onNavigateToAgent = { organId ->
                    navController.navigate("agent/$organId")
                },
                onNavigateBack = {},
            )
        }
        composable("agent/{organId}") { backStackEntry ->
            val organId = backStackEntry.arguments?.getString("organId") ?: "heart"
            AgentScreen(
                organId = organId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
