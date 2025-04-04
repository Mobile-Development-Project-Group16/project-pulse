package com.bda.projectpulse.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bda.projectpulse.ui.admin.AdminSettingsScreen
import com.bda.projectpulse.ui.admin.AdminSettingsViewModel
import com.bda.projectpulse.ui.auth.AuthScreen
import com.bda.projectpulse.ui.auth.AuthViewModel
import com.bda.projectpulse.ui.profile.ProfileScreen
import com.bda.projectpulse.ui.projects.*
import com.bda.projectpulse.ui.tasks.*
import com.bda.projectpulse.ui.team.TeamMemberScreen
import com.bda.projectpulse.ui.ai.AIChatScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = hiltViewModel<AuthViewModel>(),
                onAuthSuccess = {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProjectList.route) {
            ProjectListScreen(
                onNavigateToProjectDetails = { projectId ->
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                },
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateEditProject.createRoute())
                },
                onNavigateToEditProject = { projectId ->
                    navController.navigate(Screen.CreateEditProject.createRoute(projectId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable

            ProjectDetailsScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEditProject = { projectId ->
                    navController.navigate(Screen.CreateEditProject.createRoute(projectId))
                },
                onNavigateToCreateTask = { projectId ->
                    navController.navigate(Screen.CreateEditTask.createRoute(projectId))
                },
                onNavigateToTaskDetails = { projectId, taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onNavigateToTeamManagement = { projectId ->
                    navController.navigate(Screen.TeamManagement.createRoute(projectId))
                },
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.CreateEditProject.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            val isNewProject = projectId == "new"

            CreateEditProjectScreen(
                projectId = if (isNewProject) null else projectId,
                onNavigateBack = { navController.navigateUp() },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.TaskList.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable

            TaskListScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onCreateTask = {
                    navController.navigate(Screen.CreateEditTask.createRoute(projectId))
                },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.CreateEditTask.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val taskId = backStackEntry.arguments?.getString("taskId")

            CreateEditTaskScreen(
                projectId = projectId,
                taskId = taskId,
                onNavigateBack = { navController.navigateUp() },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.TaskDetails.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable

            TaskDetailsScreen(
                taskId = taskId,
                onNavigateBack = { navController.navigateUp() },
                onEditTask = { task ->
                    val projectId = task.projectId
                    if (projectId.isNotEmpty()) {
                        navController.navigate(Screen.CreateEditTask.createRoute(projectId = projectId, taskId = taskId))
                    }
                },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.TeamMember.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

            TeamMemberScreen(
                userId = userId,
                onNavigateBack = { navController.navigateUp() },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.TeamManagement.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable

            TeamManagementScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(
            route = Screen.AIChat.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("projectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val projectName = backStackEntry.arguments?.getString("projectName") ?: return@composable

            AIChatScreen(
                projectId = projectId,
                projectName = projectName,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AdminSettings.route) {
            AdminSettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                viewModel = hiltViewModel<AdminSettingsViewModel>()
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object ProjectList : Screen("projects")
    object ProjectDetails : Screen("projects/{projectId}") {
        fun createRoute(projectId: String) = "projects/$projectId"
    }
    object CreateEditProject : Screen("projects/edit/{projectId}") {
        fun createRoute(projectId: String? = null) = "projects/edit/${projectId ?: "new"}"
    }
    object TaskList : Screen("projects/{projectId}/tasks") {
        fun createRoute(projectId: String) = "projects/$projectId/tasks"
    }
    object CreateEditTask : Screen("projects/{projectId}/tasks/edit/{taskId}") {
        fun createRoute(projectId: String, taskId: String? = null) =
            "projects/$projectId/tasks/edit/${taskId ?: "new"}"
    }
    object TaskDetails : Screen("tasks/{taskId}") {
        fun createRoute(taskId: String) = "tasks/$taskId"
    }
    object TeamMember : Screen("team/{userId}") {
        fun createRoute(userId: String) = "team/$userId"
    }
    object TeamManagement : Screen("projects/{projectId}/team") {
        fun createRoute(projectId: String) = "projects/$projectId/team"
    }
    object Profile : Screen("profile")
    object AIChat : Screen("ai-chat/{projectId}/{projectName}") {
        fun createRoute(projectId: String, projectName: String) = "ai-chat/$projectId/$projectName"
    }
    object AdminSettings : Screen("admin-settings")
}