package com.bda.projectpulse.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.components.ErrorMessage
import com.bda.projectpulse.ui.components.StatusChip
import com.bda.projectpulse.ui.components.TaskStatusChip
import java.text.SimpleDateFormat
import java.util.*
import com.bda.projectpulse.navigation.Screen
import androidx.navigation.NavHostController
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.User
import androidx.compose.foundation.clickable

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
    onNavigateToTaskList: () -> Unit,
    onNavigateToAddTeamMember: () -> Unit,
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
        containerColor = MaterialTheme.colorScheme.surface,
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
            if (canManageProject) {
                FloatingActionButton(
                    onClick = onNavigateToCreateTask,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    project?.let { project ->
                        // Project Info Section
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusChip(status = project.status)
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Due ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                .format(project.endDate?.toDate() ?: Date())}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                // Progress Bar
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Progress",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${calculateProgress(tasks)}%",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = calculateProgress(tasks) / 100f,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Text(
                                    text = project.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // Team Members Section
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Team Members",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    
                                    if (canManageProject) {
                                        TextButton(
                                            onClick = onNavigateToAddTeamMember,
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Team Member",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add Member")
                                        }
                                    }
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    project.teamMembers.forEach { memberId ->
                                        val user = users.find { it.uid == memberId }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Card(
                                                shape = CircleShape,
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                if (user?.photoUrl != null) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(user.photoUrl)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = "Profile Picture",
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountCircle,
                                                        contentDescription = "Profile",
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(8.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = user?.displayName ?: "Unknown",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = user?.role?.name ?: "Member",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Tasks Overview Section
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Tasks Overview",
                                    style = MaterialTheme.typography.titleMedium
                                )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TaskOverviewCard(
                                        count = tasks.size,
                                        label = "Total Tasks",
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        onClick = onNavigateToTaskList
                                    )
                                    TaskOverviewCard(
                                        count = tasks.count { it.status == TaskStatus.APPROVED },
                                        label = "Completed",
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    )
                                    TaskOverviewCard(
                                        count = tasks.count { it.status == TaskStatus.IN_PROGRESS },
                                        label = "In Progress",
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                }
                            }
                        }
                        
                        // Timeline Section
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Task Timeline",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    tasks.sortedBy { it.dueDate?.toDate() }.forEach { task ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (task.status) {
                                                    TaskStatus.APPROVED -> Icons.Default.CheckCircle
                                                    else -> Icons.Default.RadioButtonUnchecked
                                                },
                                                contentDescription = null,
                                                tint = when (task.status) {
                                                    TaskStatus.APPROVED -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                            Column {
                                                Text(
                                                    text = SimpleDateFormat("MMM dd", Locale.getDefault())
                                                        .format(task.dueDate?.toDate() ?: Date()),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = task.title,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Action Buttons
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToAIChat,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("🤖 Generate Content")
                                }
                                
                        Button(
                            onClick = onNavigateToChat,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("💬 Open Project Chat")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskOverviewCard(
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
                    Text(
                text = label,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
    }
}

private fun calculateProgress(tasks: List<Task>): Int {
    if (tasks.isEmpty()) return 0
    val completed = tasks.count { it.status == TaskStatus.APPROVED }
    return (completed.toFloat() / tasks.size * 100).toInt()
}