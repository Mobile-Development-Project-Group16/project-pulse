package com.bda.projectpulse.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Projects : Screen("projects")
    object Tasks : Screen("tasks")
    object UserManagement : Screen("user_management")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
} 