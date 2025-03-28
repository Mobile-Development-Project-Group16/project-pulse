package com.bda.projectpulse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bda.projectpulse.ui.auth.AuthScreen
import com.bda.projectpulse.ui.auth.AuthViewModel
import com.bda.projectpulse.ui.projects.CreateEditProjectScreen
import com.bda.projectpulse.ui.projects.ProjectListScreen
import com.bda.projectpulse.ui.projects.ProjectViewModel
import com.bda.projectpulse.ui.projects.TeamManagementScreen
import com.bda.projectpulse.ui.tasks.CreateEditTaskScreen
import com.bda.projectpulse.ui.tasks.TaskListScreen
import com.bda.projectpulse.ui.tasks.TaskViewModel
import com.bda.projectpulse.ui.profile.ProfileScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    projectViewModel: ProjectViewModel,
    taskViewModel: TaskViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Projects.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Projects.route) {
            ProjectListScreen(
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateEditProject.route)
                },
                onNavigateToEditProject = { projectId ->
                    navController.navigate("${Screen.CreateEditProject.route}/$projectId")
                },
                onNavigateToTeamManagement = { projectId ->
                    navController.navigate("${Screen.TeamManagement.route}/$projectId")
                }
            )
        }

        composable(Screen.CreateEditProject.route) {
            CreateEditProjectScreen(
                viewModel = projectViewModel,
                onSave = {
                    navController.navigateUp()
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable("${Screen.CreateEditProject.route}/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            if (projectId != null) {
                CreateEditProjectScreen(
                    viewModel = projectViewModel,
                    projectId = projectId,
                    onSave = {
                        navController.navigateUp()
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable("${Screen.TeamManagement.route}/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            if (projectId != null) {
                TeamManagementScreen(
                    viewModel = projectViewModel,
                    projectId = projectId,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(Screen.Tasks.route) {
            TaskListScreen(
                onNavigateToCreateTask = {
                    navController.navigate(Screen.CreateEditTask.route)
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate("${Screen.CreateEditTask.route}/$taskId")
                }
            )
        }

        composable(Screen.CreateEditTask.route) {
            CreateEditTaskScreen(
                viewModel = taskViewModel,
                onSave = {
                    navController.navigateUp()
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable("${Screen.CreateEditTask.route}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                CreateEditTaskScreen(
                    viewModel = taskViewModel,
                    taskId = taskId,
                    onSave = {
                        navController.navigateUp()
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
} 