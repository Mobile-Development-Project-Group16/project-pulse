package com.bda.projectpulse.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object RoleDashboard : Screen("role_dashboard")
    object Projects : Screen("projects")
    object ProjectList : Screen("project_list")
    object ProjectDetails : Screen("projects/{projectId}") {
        fun createRoute(projectId: String) = "projects/$projectId"
    }
    object CreateEditProject : Screen("projects/create_edit?projectId={projectId}") {
        fun createRoute(projectId: String? = null) = "projects/create_edit?projectId=$projectId"
    }
    object CreateProject : Screen("projects/create")
    object EditProject : Screen("projects/{projectId}/edit") {
        fun createRoute(projectId: String) = "projects/$projectId/edit"
    }
    object Tasks : Screen("tasks")
    object TaskList : Screen("task_list")
    object CreateEditTask : Screen("tasks/create_edit?taskId={taskId}") {
        fun createRoute(taskId: String? = null) = "tasks/create_edit?taskId=$taskId"
    }
    object CreateTask : Screen("tasks/create")
    object EditTask : Screen("tasks/{taskId}/edit") {
        fun createRoute(taskId: String) = "tasks/$taskId/edit"
    }
    object Profile : Screen("profile")
    object TeamManagement : Screen("projects/{projectId}/team") {
        fun createRoute(projectId: String) = "projects/$projectId/team"
    }
    object Team : Screen("team")
    object TeamMember : Screen("team/{memberId}") {
        fun createRoute(memberId: String) = "team/$memberId"
    }
} 