package com.bda.projectpulse.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object ProjectList : Screen("project_list")
    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }
    object CreateProject : Screen("create_project")
    object EditProject : Screen("edit_project/{projectId}") {
        fun createRoute(projectId: String) = "edit_project/$projectId"
    }
    object TaskList : Screen("task_list/{projectId}") {
        fun createRoute(projectId: String) = "task_list/$projectId"
    }
    object TaskDetails : Screen("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }
    object CreateTask : Screen("create_task/{projectId}") {
        fun createRoute(projectId: String) = "create_task/$projectId"
    }
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }
    object Profile : Screen("profile")
    object TeamMembers : Screen("team_members")
    object AdminSettings : Screen("admin_settings")
    object AIChat : Screen("ai_chat")
} 