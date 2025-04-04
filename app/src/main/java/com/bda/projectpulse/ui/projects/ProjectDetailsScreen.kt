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
import com.bda.projectpulse.navigation.Screen
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    onNavigateToCreateTask: (String) -> Unit,
    onNavigateToTaskDetails: (String, String) -> Unit,
    onNavigateToTeamManagement: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditProject: (String) -> Unit,
    navController: NavHostController,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val project by viewModel.selectedProject.collectAsStateWithLifecycle()
    val tasks by viewModel.projectTasks.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        viewModel.loadProjectById(projectId)
        viewModel.loadProjectTasks(projectId)
        viewModel.loadUsers()
    }

    // Check if user can manage this project
    val canManageProject = currentUser != null && (
        currentUser?.role == UserRole.ADMIN || 
        project?.ownerId == currentUser?.uid
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete this project? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProject(projectId)
                        showDeleteConfirmation = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "Project Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            project?.let { currentProject ->
                                val encodedProjectName = java.net.URLEncoder.encode(currentProject.name, "UTF-8")
                                    .replace("+", "%20")
                                navController.navigate(
                                    Screen.AIChat.createRoute(
                                        projectId = currentProject.id,
                                        projectName = encodedProjectName
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "AI Chat")
                    }
                    
                    if (canManageProject) {
                        IconButton(onClick = { onNavigateToEditProject(projectId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Project")
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Project")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val canCreateTask = currentUser != null && (
                currentUser?.role == UserRole.ADMIN || 
                currentUser?.role == UserRole.MANAGER ||
                project?.teamMembers?.contains(currentUser?.uid) == true ||
                project?.ownerId == currentUser?.uid
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
                            val canManageTeam = currentUser?.let { user ->
                                currentProject.ownerId == user.uid ||
                                user.role == UserRole.ADMIN ||
                                user.role == UserRole.MANAGER
                            } ?: false
                            
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