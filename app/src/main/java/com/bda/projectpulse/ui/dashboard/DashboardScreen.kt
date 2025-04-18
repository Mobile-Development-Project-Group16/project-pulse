package com.bda.projectpulse.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.components.MainBottomBar
import com.bda.projectpulse.ui.components.StatusChip
import com.bda.projectpulse.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
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
                        IconButton(onClick = { navController.navigate(Screen.UserManagement.route) }) {
                            Icon(Icons.Default.People, contentDescription = "User Management")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
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
    ) { _ ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            RoleDashboardScreen(
                onNavigateToUserManagement = { navController.navigate(Screen.UserManagement.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun StatisticsSection() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatCard(
                title = "Active Projects",
                value = "12",
                icon = Icons.Default.Assignment,
                color = Color(0xFF2196F3)
            )
        }
        item {
            StatCard(
                title = "Pending Tasks",
                value = "28",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFFFFA000)
            )
        }
        item {
            StatCard(
                title = "Team Members",
                value = "8",
                icon = Icons.Default.Group,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onViewAll: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Row {
            TextButton(onClick = onViewAll) {
                Text("View All")
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentProjects(onProjectClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sample projects
        items(3) { index ->
            ProjectCard(
                name = "Project ${index + 1}",
                description = "This is a sample project description.",
                progress = (index + 1) * 30,
                onClick = { onProjectClick("project_$index") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectCard(
    name: String,
    description: String,
    progress: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$progress% Complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksOverview(onTaskClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sample tasks
        repeat(3) { index ->
            TaskItem(
                title = "Task ${index + 1}",
                project = "Project ${index + 1}",
                dueDate = "Mar ${28 + index}, 2024",
                priority = if (index == 0) "HIGH" else if (index == 1) "MEDIUM" else "LOW",
                onClick = { onTaskClick("task_$index") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskItem(
    title: String,
    project: String,
    dueDate: String,
    priority: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = project,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                StatusChip(
                    text = priority,
                    color = when (priority) {
                        "HIGH" -> Color(0xFFE53935)
                        "MEDIUM" -> Color(0xFFFFA000)
                        else -> Color(0xFF4CAF50)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due: $dueDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun AdminDashboardContent(stats: DashboardStats) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Total Users",
            value = stats.totalUsers.toString(),
            icon = Icons.Default.People
        )
        StatCard(
            title = "Total Projects",
            value = stats.totalProjects.toString(),
            icon = Icons.Default.Folder
        )
        StatCard(
            title = "Active Projects",
            value = stats.activeProjects.toString(),
            icon = Icons.Default.CheckCircle
        )
        StatCard(
            title = "Pending Tasks",
            value = stats.pendingTasks.toString(),
            icon = Icons.Default.Assignment
        )
    }
}

@Composable
fun ManagerDashboardContent(stats: DashboardStats) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Total Projects",
            value = stats.totalProjects.toString(),
            icon = Icons.Default.Folder
        )
        StatCard(
            title = "Active Projects",
            value = stats.activeProjects.toString(),
            icon = Icons.Default.CheckCircle
        )
        StatCard(
            title = "Pending Tasks",
            value = stats.pendingTasks.toString(),
            icon = Icons.Default.Assignment
        )
        StatCard(
            title = "Team Members",
            value = stats.totalTeamMembers.toString(),
            icon = Icons.Default.People
        )
    }
}

@Composable
fun TeamMemberDashboardContent(stats: DashboardStats) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Assigned Tasks",
            value = stats.assignedTasks.toString(),
            icon = Icons.Default.Assignment
        )
        StatCard(
            title = "Pending Tasks",
            value = stats.pendingTasks.toString(),
            icon = Icons.Default.HourglassEmpty
        )
        StatCard(
            title = "Submitted Tasks",
            value = stats.submittedTasks.toString(),
            icon = Icons.Default.Upload
        )
        StatCard(
            title = "Approved Tasks",
            value = stats.approvedTasks.toString(),
            icon = Icons.Default.CheckCircle
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
} 