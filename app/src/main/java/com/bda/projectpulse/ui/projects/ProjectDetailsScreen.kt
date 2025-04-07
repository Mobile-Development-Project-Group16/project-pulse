package com.bda.projectpulse.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.ui.components.ProjectCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToTaskDetails: (String) -> Unit,
    onNavigateToTeamManagement: () -> Unit,
    onNavigateToAIChat: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditProject: () -> Unit,
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
                    IconButton(onClick = onNavigateToAIChat) {
                        Icon(Icons.Default.SmartToy, contentDescription = "AI Chat")
                    }
                    
                    if (canManageProject) {
                        IconButton(onClick = onNavigateToEditProject) {
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
                    onClick = onNavigateToCreateTask
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Task")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            } else {
                project?.let { project ->
                    ProjectCard(project = project)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Team Management
                        Button(
                            onClick = onNavigateToTeamManagement,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = "Team")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Team")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Tasks
                        Button(
                            onClick = onNavigateToCreateTask,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Tasks")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tasks")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // AI Chat
                        Button(
                            onClick = onNavigateToAIChat,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = "AI Chat")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Chat")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Team Chat
                        Button(
                            onClick = onNavigateToChat,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "Team Chat")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat")
                        }
                    }
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
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