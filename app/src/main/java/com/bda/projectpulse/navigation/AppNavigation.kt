package com.bda.projectpulse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bda.projectpulse.ui.auth.AuthScreen
import com.bda.projectpulse.ui.auth.AuthViewModel
import com.bda.projectpulse.ui.dashboard.DashboardScreen
import com.bda.projectpulse.ui.dashboard.RoleDashboardScreen
import com.bda.projectpulse.ui.projects.ProjectListScreen
import com.bda.projectpulse.ui.projects.CreateEditProjectScreen
import com.bda.projectpulse.ui.projects.ProjectDetailsScreen
import com.bda.projectpulse.ui.tasks.TaskListScreen
import com.bda.projectpulse.ui.tasks.CreateEditTaskScreen
import com.bda.projectpulse.ui.profile.ProfileScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            AuthScreen(
                viewModel = AuthViewModel(),
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onProjectClick = { projectId -> 
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                },
                onViewAllProjects = {
                    navController.navigate(Screen.ProjectList.route)
                },
                onViewAllTasks = {
                    navController.navigate(Screen.TaskList.route)
                },
                onCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.RoleDashboard.route) {
            RoleDashboardScreen(
                userRole = "ADMIN",
                userName = "John Doe",
                onProjectClick = { projectId ->
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                },
                onTeamMemberClick = { memberId ->
                    navController.navigate(Screen.TeamMember.createRoute(memberId))
                },
                onCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onManageTeam = {
                    navController.navigate(Screen.Team.route)
                }
            )
        }

        composable(Screen.ProjectList.route) {
            ProjectListScreen(
                onProjectClick = { projectId ->
                    navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                },
                onCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                }
            )
        }

        composable(Screen.CreateProject.route) {
            CreateEditProjectScreen(
                isEditing = false,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = {
                    // Will implement later
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditProject.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) {
            CreateEditProjectScreen(
                isEditing = true,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = {
                    // Will implement later
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            ProjectDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditProject = {
                    navController.navigate(Screen.EditProject.createRoute(projectId ?: ""))
                },
                onCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                },
                onTeamMemberClick = { memberId ->
                    navController.navigate(Screen.TeamMember.createRoute(memberId))
                }
            )
        }

        composable(Screen.TaskList.route) {
            TaskListScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                },
                onCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateTask.route) {
            CreateEditTaskScreen(
                isEditing = false,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = {
                    // TODO: Implement task creation
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            CreateEditTaskScreen(
                isEditing = true,
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = {
                    // TODO: Implement task update
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
} 