package com.bda.projectpulse.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.components.MainBottomBar
import androidx.navigation.NavController
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDashboardScreen(
    onNavigateToUserManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToTasks: () -> Unit,
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    if (currentUser?.role == UserRole.ADMIN) {
                        IconButton(onClick = onNavigateToUserManagement) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = "User Management")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        bottomBar = {
            MainBottomBar(
                navController = navController,
                unreadNotificationCount = uiState.unreadNotificationCount
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentUser?.role) {
                    UserRole.ADMIN -> AdminDashboard(
                        stats = uiState.stats,
                        onUserManagementClick = onNavigateToUserManagement,
                        onSettingsClick = onNavigateToSettings
                    )
                    UserRole.MANAGER -> ManagerDashboard(
                        stats = uiState.stats,
                        onProjectsClick = onNavigateToProjects,
                        onTasksClick = onNavigateToTasks
                    )
                    UserRole.USER -> TeamMemberDashboard(
                        stats = uiState.stats,
                        onProjectsClick = onNavigateToProjects,
                        onTasksClick = onNavigateToTasks
                    )
                    null -> {
                        // Show loading or error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        // Error Snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun AdminDashboard(
    stats: DashboardStats,
    onUserManagementClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(adminStats(stats)) { stat ->
                StatCard(
                    title = stat.first,
                    value = stat.second.toString(),
                    icon = when (stat.first) {
                        "Total Users" -> Icons.Default.Group
                        "Total Projects" -> Icons.Default.Folder
                        "Active Projects" -> Icons.Default.PlayArrow
                        "Pending Tasks" -> Icons.Default.Assignment
                        else -> Icons.Default.Info
                    }
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onUserManagementClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ManageAccounts, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("User Management")
            }
            
            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Settings")
            }
        }
    }
}

@Composable
private fun ManagerDashboard(
    stats: DashboardStats,
    onProjectsClick: () -> Unit,
    onTasksClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(managerStats(stats)) { stat ->
                StatCard(
                    title = stat.first,
                    value = stat.second.toString(),
                    icon = when (stat.first) {
                        "Total Projects" -> Icons.Default.Folder
                        "Pending Tasks" -> Icons.Default.Assignment
                        "Team Members" -> Icons.Default.Group
                        "Active Projects" -> Icons.Default.PlayArrow
                        else -> Icons.Default.Info
                    }
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onProjectsClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Projects")
            }
            
            OutlinedButton(
                onClick = onTasksClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Tasks")
            }
        }
    }
}

@Composable
private fun TeamMemberDashboard(
    stats: DashboardStats,
    onProjectsClick: () -> Unit,
    onTasksClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(300.dp)
        ) {
            items(teamMemberStats(stats)) { stat ->
                StatCard(
                    title = stat.first,
                    value = stat.second.toString(),
                    icon = when (stat.first) {
                        "Total Projects" -> Icons.Default.Folder
                        "Assigned Tasks" -> Icons.Default.Assignment
                        "Pending Tasks" -> Icons.Default.Pending
                        "Submitted Tasks" -> Icons.Default.Send
                        "Approved Tasks" -> Icons.Default.CheckCircle
                        "Rejected Tasks" -> Icons.Default.Cancel
                        else -> Icons.Default.Info
                    }
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onProjectsClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Projects")
            }
            
            OutlinedButton(
                onClick = onTasksClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Tasks")
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun adminStats(stats: DashboardStats) = listOf(
    "Total Users" to stats.totalUsers,
    "Total Projects" to stats.totalProjects,
    "Active Projects" to stats.activeProjects,
    "Pending Tasks" to stats.pendingTasks
)

private fun managerStats(stats: DashboardStats) = listOf(
    "Total Projects" to stats.totalProjects,
    "Active Projects" to stats.activeProjects,
    "Pending Tasks" to stats.pendingTasks,
    "Team Members" to stats.totalTeamMembers
)

private fun teamMemberStats(stats: DashboardStats) = listOf(
    "Total Projects" to stats.totalProjects,
    "Assigned Tasks" to stats.assignedTasks,
    "Pending Tasks" to stats.pendingTasks,
    "Submitted Tasks" to stats.submittedTasks,
    "Approved Tasks" to stats.approvedTasks,
    "Rejected Tasks" to stats.rejectedTasks
) 