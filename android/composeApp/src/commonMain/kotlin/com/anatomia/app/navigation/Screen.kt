package com.anatomia.app.navigation

sealed class Screen(val route: String) {
    object Login       : Screen("login")
    object Home        : Screen("home")
    object Agent : Screen("agent/{organId}") {
        fun createRoute(organId: String) = "agent/$organId"
        const val ARG_ORGAN_ID = "organId"
    }
    object BodyModel   : Screen("body_model")
    object Quiz : Screen("quiz/{organId}") {
        fun createRoute(organId: String) = "quiz/$organId"
    }
    object QuizResults : Screen("quiz_results")
    object Settings    : Screen("settings")
    object History     : Screen("history")
    object EditProfile : Screen("edit_profile")
}
