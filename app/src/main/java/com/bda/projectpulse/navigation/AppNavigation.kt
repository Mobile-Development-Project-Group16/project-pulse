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
import com.bda.projectpulse.ui.auth.AuthScreen
import com.bda.projectpulse.ui.auth.AuthViewModel
import com.bda.projectpulse.ui.profile.ProfileScreen
import com.bda.projectpulse.ui.projects.*
import com.bda.projectpulse.ui.tasks.*
import com.bda.projectpulse.ui.team.TeamMemberScreen
import com.bda.projectpulse.ui.admin.AdminSettingsScreen
import com.bda.projectpulse.ui.admin.AdminSettingsViewModel
import com.bda.projectpulse.ui.ai.AIChatScreen
import com.bda.projectpulse.ui.chat.ProjectChatScreen
import com.bda.projectpulse.ui.team.TeamManagementScreen

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
            val viewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Projects.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(route = Screen.Projects.route) {
            ProjectListScreen(
                onNavigateToProjectDetails = { projectId ->
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                },
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToEditProject = { projectId ->
                    navController.navigate(Screen.EditProject.createRoute(projectId))
                },
                onNavigateToAdminSettings = {
                    navController.navigate(Screen.AdminSettings.route)
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
                onNavigateToCreateTask = { 
                    navController.navigate(Screen.CreateTask.createRoute(projectId))
                },
                onNavigateToTaskDetails = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onNavigateToTeamManagement = {
                    navController.navigate(Screen.TeamManagement.createRoute(projectId))
                },
                onNavigateToAIChat = {
                    navController.navigate(Screen.AIChat.createRoute(projectId))
                },
                onNavigateToEditProject = {
                    navController.navigate(Screen.EditProject.createRoute(projectId))
                },
                onNavigateToChat = {
                    navController.navigate(Screen.ProjectChat.createRoute(projectId))
                },
                onNavigateToTaskList = {
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
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onCreateTask = {
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
                onEditTask = { task ->
                    navController.navigate(Screen.EditTask.createRoute(task.projectId, task.id))
                }
            )
        }

        composable(
            route = Screen.CreateTask.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            CreateEditTaskScreen(
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
                },
                navController = navController
            )
        }

        composable(
            route = Screen.TeamMembers.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            TeamMemberScreen(
                userId = userId,
                onNavigateBack = { navController.navigateUp() },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                }
            )
        }

        composable(route = Screen.AdminSettings.route) {
            val viewModel: AdminSettingsViewModel = hiltViewModel()
            AdminSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.AIChat.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val viewModel: ProjectViewModel = hiltViewModel()
            val project by viewModel.selectedProject.collectAsStateWithLifecycle()
            
            LaunchedEffect(projectId) {
                viewModel.loadProjectById(projectId)
            }
            
            AIChatScreen(
                projectId = projectId,
                projectName = project?.name ?: "Project",
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.ProjectChat.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val viewModel: ProjectViewModel = hiltViewModel()
            val project by viewModel.selectedProject.collectAsStateWithLifecycle()
            
            LaunchedEffect(projectId) {
                viewModel.loadProjectById(projectId)
            }
            
            ProjectChatScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.TeamManagement.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val viewModel: ProjectViewModel = hiltViewModel()
            val project by viewModel.selectedProject.collectAsStateWithLifecycle()
            
            LaunchedEffect(projectId) {
                viewModel.loadProjectById(projectId)
            }
            
            TeamManagementScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}