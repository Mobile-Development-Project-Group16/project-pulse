package com.bda.projectpulse.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bda.projectpulse.ui.auth.AuthScreen
import com.bda.projectpulse.ui.profile.ProfileScreen
import com.bda.projectpulse.ui.projects.*
import com.bda.projectpulse.ui.tasks.*
import com.bda.projectpulse.ui.team.TeamMemberScreen

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
                viewModel = hiltViewModel(),
                onAuthSuccess = {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
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
                    navController.navigate(Screen.CreateEditProject.route)
                },
                onNavigateToEditProject = { projectId ->
                    navController.navigate(Screen.CreateEditProject.createRoute(projectId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            ProjectDetailsScreen(
                projectId = projectId,
                onNavigateToCreateTask = { projectId ->
                    navController.navigate(Screen.CreateEditTask.createRoute(projectId))
                },
                onNavigateToTaskDetails = { projectId, taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onNavigateToTeamManagement = { projectId ->
                    navController.navigate(Screen.TeamManagement.createRoute(projectId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateEditProject.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            CreateEditProjectScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TaskList.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            TaskListScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onCreateTask = {
                    navController.navigate(Screen.CreateEditTask.createRoute(projectId))
                }
            )
        }

        composable(
            route = Screen.CreateEditTask.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("taskId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val taskId = backStackEntry.arguments?.getString("taskId")?.takeIf { it != "new" }
            CreateEditTaskScreen(
                projectId = projectId,
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TaskDetails.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailsScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onEditTask = { task ->
                    val projectId = task.projectId
                    if (projectId.isNotEmpty()) {
                        navController.navigate(Screen.CreateEditTask.createRoute(projectId = projectId, taskId = taskId))
                    }
                }
            )
        }

        composable(
            route = Screen.TeamMember.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            TeamMemberScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                }
            )
        }

        composable(
            route = Screen.TeamManagement.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            TeamManagementScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
        fun createRoute(projectId: String? = null) = "projects/edit/${projectId ?: ""}"
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
}