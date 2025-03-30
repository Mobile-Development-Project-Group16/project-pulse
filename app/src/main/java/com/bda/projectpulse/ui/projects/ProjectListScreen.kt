package com.bda.projectpulse.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.common.ErrorMessage
import com.bda.projectpulse.ui.common.StatusChip
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    onNavigateToProjectDetails: (String) -> Unit,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToEditProject: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
        viewModel.loadCurrentUser()
    }
    
    // Check if user can create projects (only ADMIN and MANAGER)
    val canCreateProjects = remember(currentUser) {
        currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.MANAGER
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation && projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                projectToDelete = null
            },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete this project? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        projectToDelete?.let { viewModel.deleteProject(it) }
                        showDeleteConfirmation = false
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showDeleteConfirmation = false
                        projectToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                actions = {
                    currentUser?.let { user ->
                        Text(
                            text = "Role: ${user.role.name}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB if user has permission to create projects
            if (canCreateProjects) {
                FloatingActionButton(
                    onClick = onNavigateToCreateProject
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Project")
                }
            }
        }
    ) { padding ->
        if (isLoading && projects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            ErrorMessage(error = error!!)
        } else if (projects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (canCreateProjects) {
                        Text("No projects found. Create a new project using the + button.")
                    } else {
                        Text("No projects found. You need to be added to a project by a manager or admin.")
                    }
                    
                    currentUser?.let { user ->
                        Spacer(modifier = Modifier.height(8.dp))
                        when (user.role) {
                            UserRole.ADMIN -> Text("As an Admin, you can create projects and manage all tasks.")
                            UserRole.MANAGER -> Text("As a Manager, you can create projects and manage your tasks.")
                            else -> Text("As a User, you can only view and work on tasks assigned to you.")
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    
                    items(projects) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { onNavigateToProjectDetails(project.id) },
                            onEdit = { onNavigateToEditProject(project.id) },
                            onDelete = { 
                                projectToDelete = project.id
                                showDeleteConfirmation = true
                            },
                            currentUserId = currentUser?.uid,
                            currentUserRole = currentUser?.role,
                            viewModel = viewModel
                        )
                    }
                }
                
                // Refresh action
                FilledIconButton(
                    onClick = { viewModel.loadProjects() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    currentUserId: String?,
    currentUserRole: UserRole?,
    viewModel: ProjectViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Date",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val startDate = dateFormat.format(project.startDate.toDate())
                val endDate = project.endDate?.let { dateFormat.format(it.toDate()) } ?: "No end date"
                Text(
                    text = "$startDate to $endDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Team members
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = "Team Members",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${project.teamMembers.size} team members",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = project.status)
                Text(
                    text = "Created by: ${viewModel.getUserName(project.ownerId)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
} 