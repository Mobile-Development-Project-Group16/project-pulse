package com.bda.projectpulse.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Projects : Screen("projects")
    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }
    object CreateProject : Screen("create_project")
    object EditProject : Screen("edit_project/{projectId}") {
        fun createRoute(projectId: String) = "edit_project/$projectId"
    }
    object TaskList : Screen("tasks/{projectId}") {
        fun createRoute(projectId: String) = "tasks/$projectId"
    }
    object TaskDetails : Screen("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }
    object CreateTask : Screen("create_task/{projectId}") {
        fun createRoute(projectId: String) = "create_task/$projectId"
    }
    object EditTask : Screen("edit_task/{projectId}/{taskId}") {
        fun createRoute(projectId: String, taskId: String) = "edit_task/$projectId/$taskId"
    }
    object Profile : Screen("profile")
    object TeamMembers : Screen("team_members/{userId}") {
        fun createRoute(userId: String) = "team_members/$userId"
    }
    object TeamManagement : Screen("team_management/{projectId}") {
        fun createRoute(projectId: String) = "team_management/$projectId"
    }
    object AdminSettings : Screen("admin_settings")
    object AIChat : Screen("ai_chat/{projectId}") {
        fun createRoute(projectId: String) = "ai_chat/$projectId"
    }
    object ProjectChat : Screen("project_chat/{projectId}") {
        fun createRoute(projectId: String) = "project_chat/$projectId"
    }
} 