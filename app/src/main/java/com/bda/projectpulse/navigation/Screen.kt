package com.bda.projectpulse.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Projects : Screen("projects")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object AdminSettings : Screen("admin_settings")
    object UserManagement : Screen("user_management")
    
    object ProjectDetails : Screen("project/{projectId}") {
        fun createRoute(projectId: String) = "project/$projectId"
    }
    
    object CreateProject : Screen("project/create")
    object EditProject : Screen("project/{projectId}/edit") {
        fun createRoute(projectId: String) = "project/$projectId/edit"
    }
    
    object TaskList : Screen("project/{projectId}/tasks") {
        fun createRoute(projectId: String) = "project/$projectId/tasks"
    }
    
    object TaskDetails : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }
    
    object CreateTask : Screen("project/{projectId}/task/create") {
        fun createRoute(projectId: String) = "project/$projectId/task/create"
    }
    
    object EditTask : Screen("project/{projectId}/task/{taskId}/edit") {
        fun createRoute(projectId: String, taskId: String) = "project/$projectId/task/$taskId/edit"
    }
    
    object SubmitTask : Screen("task/{taskId}/submit") {
        fun createRoute(taskId: String) = "task/$taskId/submit"
    }
    
    object TeamMembers : Screen("team/{userId}") {
        fun createRoute(userId: String) = "team/$userId"
    }
    
    object AIChat : Screen("project/{projectId}/ai") {
        fun createRoute(projectId: String) = "project/$projectId/ai"
    }
    
    object ProjectChat : Screen("project/{projectId}/chat") {
        fun createRoute(projectId: String) = "project/$projectId/chat"
    }
    
    object TeamManagement : Screen("project/{projectId}/team") {
        fun createRoute(projectId: String) = "project/$projectId/team"
    }
} 