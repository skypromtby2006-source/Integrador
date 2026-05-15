package com.anatomia.app.navigation

sealed class Screen(val route: String) {
    object Login       : Screen("login")
    object Home        : Screen("home")
    object Agent       : Screen("agent")
    object Quiz        : Screen("quiz")
    object QuizResults : Screen("quiz_results")
    object Settings    : Screen("settings")
    object History     : Screen("history")
    object EditProfile : Screen("edit_profile")
}
