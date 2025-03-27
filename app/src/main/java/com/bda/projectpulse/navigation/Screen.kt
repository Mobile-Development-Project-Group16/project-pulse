package com.bda.projectpulse.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object RoleDashboard : Screen("role_dashboard")
    object ProjectList : Screen("projects")
    object CreateProject : Screen("projects/create")
    object EditProject : Screen("projects/{projectId}/edit") {
        fun createRoute(projectId: String) = "projects/$projectId/edit"
    }
    object ProjectDetails : Screen("projects/{projectId}") {
        fun createRoute(projectId: String) = "projects/$projectId"
    }
    object TaskList : Screen("tasks")
    object CreateTask : Screen("tasks/create")
    object EditTask : Screen("tasks/{taskId}/edit") {
        fun createRoute(taskId: String) = "tasks/$taskId/edit"
    }
    object Profile : Screen("profile")
    object Team : Screen("team")
    object TeamMember : Screen("team/{memberId}") {
        fun createRoute(memberId: String) = "team/$memberId"
    }
} 