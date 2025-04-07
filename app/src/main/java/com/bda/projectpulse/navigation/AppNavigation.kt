package com.bda.projectpulse.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bda.projectpulse.ui.admin.AdminSettingsScreen
import com.bda.projectpulse.ui.admin.AdminSettingsViewModel
import com.bda.projectpulse.ui.auth.LoginScreen
import com.bda.projectpulse.ui.auth.RegisterScreen
import com.bda.projectpulse.ui.profile.ProfileScreen
import com.bda.projectpulse.ui.projects.*
import com.bda.projectpulse.ui.tasks.*
import com.bda.projectpulse.ui.team.TeamMemberScreen
import com.bda.projectpulse.ui.ai.AIChatScreen
import com.bda.projectpulse.ui.chat.ChatScreen
import com.bda.projectpulse.ui.projects.ProjectViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Auth.route) {
            AuthScreen(
                onNavigateToProjects = {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.ProjectList.route) {
            ProjectListScreen(
                onNavigateToProjectDetails = { projectId ->
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                },
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
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
                    navController.navigate(Screen.EditProject.createRoute(projectId))
                },
                onNavigateToTaskList = { projectId ->
                    navController.navigate(Screen.TaskList.createRoute(projectId))
                }
            )
        }

        composable(
            route = Screen.EditProject.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            EditProjectScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() }
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
                onNavigateToTaskDetails = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onNavigateToCreateTask = { projectId ->
                    navController.navigate(Screen.CreateTask.createRoute(projectId))
                }
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
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                }
            )
        }

        composable(
            route = Screen.CreateTask.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            CreateTaskScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.navigateUp() },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.TeamMembers.route) {
            TeamMemberScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = Screen.AdminSettings.route) {
            AdminSettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = Screen.AIChat.route) {
            AIChatScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Projects : Screen("projects")
    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }
    object TeamManagement : Screen("team_management/{projectId}") {
        fun createRoute(projectId: String) = "team_management/$projectId"
    }
    object TaskList : Screen("tasks/{projectId}") {
        fun createRoute(projectId: String) = "tasks/$projectId"
    }
    object TaskDetails : Screen("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }
    object AdminSettings : Screen("admin_settings")
    object AIChat : Screen("ai_chat/{projectId}") {
        fun createRoute(projectId: String) = "ai_chat/$projectId"
    }
    object ProjectChat : Screen("project_chat/{projectId}") {
        fun createRoute(projectId: String) = "project_chat/$projectId"
    }
    object CreateProject : Screen("create_project")
    object EditProject : Screen("edit_project/{projectId}") {
        fun createRoute(projectId: String) = "edit_project/$projectId"
    }
    object CreateTask : Screen("create_task/{projectId}") {
        fun createRoute(projectId: String) = "create_task/$projectId"
    }
    object EditTask : Screen("edit_task/{projectId}/{taskId}") {
        fun createRoute(projectId: String, taskId: String) = "edit_task/$projectId/$taskId"
    }
    object Profile : Screen("profile")
    object Auth : Screen("auth")
    object TeamMembers : Screen("team_members")
}