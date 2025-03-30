package com.bda.projectpulse.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.common.ErrorMessage
import com.bda.projectpulse.ui.common.StatusChip
import com.bda.projectpulse.ui.common.TaskStatusChip
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    onNavigateToCreateTask: (String) -> Unit,
    onNavigateToTaskDetails: (String, String) -> Unit,
    onNavigateToTeamManagement: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val project by viewModel.selectedProject.collectAsStateWithLifecycle()
    val tasks by viewModel.projectTasks.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.loadProjectById(projectId)
        viewModel.loadProjectTasks(projectId)
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "Project Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val currentUser = viewModel.currentUser.value
            val canCreateTask = currentUser != null && (
                currentUser.role == UserRole.ADMIN || 
                currentUser.role == UserRole.MANAGER
            )
            
            if (canCreateTask) {
                FloatingActionButton(
                    onClick = { onNavigateToCreateTask(projectId) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Task")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            ErrorMessage(error = error!!)
        } else {
            project?.let { currentProject ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Project header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = currentProject.name,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = currentProject.description,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                // Date information
                                Spacer(modifier = Modifier.height(16.dp))
                                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = "Date",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val startDate = dateFormat.format(currentProject.startDate.toDate())
                                    val endDate = currentProject.endDate?.let { 
                                        dateFormat.format(it.toDate()) 
                                    } ?: "No end date"
                                    Text(
                                        text = "Duration: $startDate to $endDate",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // Status information
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Status: ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    StatusChip(status = currentProject.status)
                                }
                                
                                // Created by information
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Created by: ${viewModel.getUserName(currentProject.ownerId)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                // Created/updated timestamps
                                Spacer(modifier = Modifier.height(8.dp))
                                val createdAt = dateFormat.format(currentProject.startDate.toDate())
                                Text(
                                    text = "Created: $createdAt",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    // Team Members section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Team Members (${currentProject.teamMembers.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            // Only show "Manage Team" button for admins, managers, or the project owner
                            val currentUser = viewModel.currentUser.value
                            val canManageTeam = currentUser != null && (
                                currentProject.ownerId == currentUser.uid ||
                                currentUser.role == UserRole.ADMIN ||
                                currentUser.role == UserRole.MANAGER
                            )
                            
                            if (canManageTeam) {
                                Button(
                                    onClick = { onNavigateToTeamManagement(projectId) },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Group, 
                                        contentDescription = "Manage Team",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Manage Team")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Team members list
                    if (currentProject.teamMembers.isNotEmpty()) {
                        items(currentProject.teamMembers) { memberId ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ListItem(
                                    headlineContent = { Text(viewModel.getUserName(memberId)) },
                                    supportingContent = { Text(viewModel.getUserEmail(memberId)) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "No team members assigned to this project",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Tasks section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tasks (${tasks.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Tasks list
                    if (tasks.isEmpty()) {
                        item {
                            Text(
                                text = "No tasks yet",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(tasks) { task ->
                            TaskCard(
                                task = task,
                                viewModel = viewModel,
                                onClick = { onNavigateToTaskDetails(projectId, task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(
    task: Task,
    viewModel: ProjectViewModel,
    onClick: () -> Unit
) {
    val assigneeText = remember(task.assigneeIds) {
        when {
            task.assigneeIds.isEmpty() -> "Unassigned"
            task.assigneeIds.size == 1 -> viewModel.getUserName(task.assigneeIds.first())
            else -> "${task.assigneeIds.size} assignees"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        ListItem(
            headlineContent = { Text(task.title) },
            supportingContent = { 
                Column {
                    Text(task.description)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Assignee: $assigneeText",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            trailingContent = {
                TaskStatusChip(status = task.status)
            }
        )
    }
}