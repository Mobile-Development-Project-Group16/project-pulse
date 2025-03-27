package com.bda.projectpulse.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDashboardScreen(
    userRole: String,
    userName: String,
    onProjectClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onTeamMemberClick: (String) -> Unit,
    onCreateProject: () -> Unit,
    onCreateTask: () -> Unit,
    onManageTeam: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(userName)
                        Text(
                            text = userRole,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role-specific actions
            item {
                RoleActions(
                    userRole = userRole,
                    onCreateProject = onCreateProject,
                    onCreateTask = onCreateTask,
                    onManageTeam = onManageTeam
                )
            }

            // Role-specific content
            when (userRole) {
                "ADMIN" -> {
                    item { AdminDashboard(onTeamMemberClick = onTeamMemberClick) }
                }
                "PROJECT_MANAGER" -> {
                    item { ManagerDashboard(onProjectClick = onProjectClick) }
                }
                else -> {
                    item { TeamMemberDashboard(onTaskClick = onTaskClick) }
                }
            }
        }
    }
}

@Composable
private fun RoleActions(
    userRole: String,
    onCreateProject: () -> Unit,
    onCreateTask: () -> Unit,
    onManageTeam: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (userRole in listOf("ADMIN", "PROJECT_MANAGER")) {
                ActionButton(
                    text = "New Project",
                    icon = Icons.Default.Add,
                    onClick = onCreateProject,
                    modifier = Modifier.weight(1f)
                )
            }
            ActionButton(
                text = "New Task",
                icon = Icons.Default.Assignment,
                onClick = onCreateTask,
                modifier = Modifier.weight(1f)
            )
            if (userRole == "ADMIN") {
                ActionButton(
                    text = "Manage Team",
                    icon = Icons.Default.Group,
                    onClick = onManageTeam,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminDashboard(onTeamMemberClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Team Overview
        Text(
            text = "Team Overview",
            style = MaterialTheme.typography.titleMedium
        )
        repeat(3) { index ->
            TeamMemberCard(
                name = "Team Member ${index + 1}",
                role = if (index == 0) "Project Manager" else "Team Member",
                activeProjects = (index + 1) * 2,
                completedTasks = (index + 2) * 5,
                onClick = { onTeamMemberClick("member_$index") }
            )
        }
    }
}

@Composable
private fun ManagerDashboard(onProjectClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Project Status Overview
        Text(
            text = "Project Status",
            style = MaterialTheme.typography.titleMedium
        )
        repeat(3) { index ->
            ProjectStatusCard(
                name = "Project ${index + 1}",
                status = if (index == 0) "IN_PROGRESS" else if (index == 1) "ON_HOLD" else "COMPLETED",
                teamSize = (index + 2),
                completionRate = (index + 1) * 30,
                onClick = { onProjectClick("project_$index") }
            )
        }
    }
}

@Composable
private fun TeamMemberDashboard(onTaskClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // My Tasks
        Text(
            text = "My Tasks",
            style = MaterialTheme.typography.titleMedium
        )
        repeat(3) { index ->
            TaskCard(
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
private fun TeamMemberCard(
    name: String,
    role: String,
    activeProjects: Int,
    completedTasks: Int,
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
                    text = name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$activeProjects Active Projects",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$completedTasks Tasks Completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectStatusCard(
    name: String,
    status: String,
    teamSize: Int,
    completionRate: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall
                )
                StatusChip(
                    text = status,
                    color = when (status) {
                        "IN_PROGRESS" -> Color(0xFF4CAF50)
                        "ON_HOLD" -> Color(0xFFFFA000)
                        else -> Color(0xFF2196F3)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$teamSize Team Members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$completionRate% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = completionRate / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(
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
                    style = MaterialTheme.typography.titleSmall
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