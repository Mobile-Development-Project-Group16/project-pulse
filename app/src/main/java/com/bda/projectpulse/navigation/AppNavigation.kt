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
import com.bda.projectpulse.ui.notifications.NotificationScreen
import com.bda.projectpulse.ui.notifications.NotificationViewModel
import com.bda.projectpulse.ui.dashboard.RoleDashboardScreen
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import com.bda.projectpulse.ui.admin.UserManagementScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = "dashboard"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("dashboard") {
            RoleDashboardScreen(
                onNavigateToUserManagement = { navController.navigate("user_management") },
                onNavigateToSettings = { navController.navigate("admin_settings") },
                onNavigateToProjects = { navController.navigate("projects") },
                onNavigateToTasks = { navController.navigate("tasks") },
                onNavigateToProfile = { navController.navigate("profile") },
                navController = navController
            )
        }
        composable("projects") {
            ProjectListScreen(
                onNavigateToProjectDetails = { projectId -> navController.navigate("project/$projectId") },
                onNavigateToCreateProject = { navController.navigate("create_project") },
                onNavigateToEditProject = { projectId -> navController.navigate("edit_project/$projectId") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToAdminSettings = { navController.navigate("admin_settings") },
                navController = navController
            )
        }
        composable("tasks") {
            TaskListScreen(
                projectId = null,
                onTaskClick = { taskId -> 
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onCreateTask = { 
                    navController.navigate(Screen.CreateTask.createRoute("global"))
                },
                onNavigateBack = { 
                    navController.navigateUp()
                },
                navController = navController
            )
        }
        composable("team") {
            val viewModel: ProjectViewModel = hiltViewModel()
            val project by viewModel.selectedProject.collectAsStateWithLifecycle()
            
            TeamManagementScreen(
                projectId = "global",
                projectName = "Global Team",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("notifications") {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetails = { taskId -> navController.navigate("task/$taskId") },
                onNavigateToProjectChat = { projectId -> navController.navigate("chat/$projectId") },
                navController = navController
            )
        }
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
                },
                navController = navController
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
                },
                onNavigateToAddTeamMember = {
                    navController.navigate(Screen.TeamManagement.createRoute(projectId))
                },
                navController = navController
            )
        }

        composable(
            route = Screen.EditProject.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            EditProjectScreen(
                projectId = projectId,
                onNavigateBack = { navController.navigateUp() },
                navController = navController
            )
        }

        composable(
            route = Screen.TaskList.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            TaskListScreen(
                projectId = projectId,
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onCreateTask = { 
                    navController.navigate(Screen.CreateTask.createRoute(projectId ?: "global"))
                },
                onNavigateBack = { navController.navigateUp() },
                navController = navController
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
                },
                navController = navController
            )
        }

        composable(
            route = Screen.CreateTask.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val viewModel: TaskViewModel = hiltViewModel()
            CreateEditTaskScreen(
                projectId = projectId,
                taskId = null,
                onNavigateBack = { navController.navigateUp() },
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.SubmitTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            SubmitTaskScreen(
                taskId = taskId,
                onNavigateBack = { navController.navigateUp() },
                navController = navController
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
            
            if (project != null) {
                AIChatScreen(
                    projectId = projectId,
                    projectName = project?.name ?: "Project",
                    onNavigateBack = { navController.navigateUp() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
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
            
            if (project != null) {
                TeamManagementScreen(
                    projectId = projectId,
                    projectName = project?.name ?: "Project",
                    onNavigateBack = { navController.navigateUp() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(route = Screen.CreateProject.route) {
            CreateEditProjectScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val viewModel: TaskViewModel = hiltViewModel()
            CreateEditTaskScreen(
                projectId = projectId,
                taskId = taskId,
                onNavigateBack = { navController.navigateUp() },
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Screen.Notifications.route) {
            val viewModel: NotificationViewModel = hiltViewModel()
            NotificationScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToTaskDetails = { taskId ->
                    navController.navigate(Screen.TaskDetails.createRoute(taskId))
                },
                onNavigateToProjectChat = { projectId ->
                    navController.navigate(Screen.ProjectChat.createRoute(projectId))
                },
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(route = Screen.Dashboard.route) {
            RoleDashboardScreen(
                onNavigateToUserManagement = {
                    navController.navigate(Screen.UserManagement.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.AdminSettings.route)
                },
                onNavigateToProjects = {
                    navController.navigate(Screen.Projects.route)
                },
                onNavigateToTasks = {
                    navController.navigateUp()
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                navController = navController
            )
        }

        composable(route = Screen.UserManagement.route) {
            UserManagementScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { onNavigate(Screen.Dashboard.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Projects") },
            label = { Text("Projects") },
            selected = currentRoute == Screen.Projects.route,
            onClick = { onNavigate(Screen.Projects.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
            label = { Text("Tasks") },
            selected = currentRoute == Screen.TaskList.route,
            onClick = { onNavigate(Screen.TaskList.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.People, contentDescription = "Team") },
            label = { Text("Team") },
            selected = currentRoute == "team",
            onClick = { onNavigate("team") }
        )
    }
}