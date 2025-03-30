package com.bda.projectpulse.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.common.AddTeamMemberDialog
import com.bda.projectpulse.ui.common.ErrorMessage
import com.bda.projectpulse.ui.common.TeamMemberItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val project by viewModel.selectedProject.collectAsStateWithLifecycle()
    val allUsers by viewModel.users.collectAsStateWithLifecycle(emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val error by viewModel.error.collectAsStateWithLifecycle(null)
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    var showAddMemberDialog by remember { mutableStateOf(false) }
    
    // Check if current user is the project owner or has management privileges
    val hasManagementPrivileges = remember(project, currentUser) {
        currentUser?.let { user ->
            project?.ownerId == user.uid || 
            user.role == UserRole.ADMIN || 
            user.role == UserRole.MANAGER
        } ?: false
    }
    
    // Load project data
    LaunchedEffect(projectId) {
        viewModel.loadProjectById(projectId)
        viewModel.loadUsers()
        viewModel.loadCurrentUser()
    }
    
    // If not privileges, show access denied message
    LaunchedEffect(hasManagementPrivileges, project, currentUser) {
        if (project != null && currentUser != null && !hasManagementPrivileges) {
            viewModel.updateError("Access denied: Only project owners, administrators, or managers can manage team members")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Members") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasManagementPrivileges) {
                        IconButton(onClick = { showAddMemberDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Team Member")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
                project == null -> Text(
                    text = "Project not found",
                    modifier = Modifier.align(Alignment.Center)
                )
                !hasManagementPrivileges -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Access denied: Only project owners, administrators, or managers can manage team members",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
                else -> {
                    val teamMembers = allUsers.filter { user -> 
                        project?.teamMembers?.contains(user.uid) == true 
                    }
                    
                    if (teamMembers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No team members yet")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { showAddMemberDialog = true }) {
                                    Text("Add Team Member")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(teamMembers) { member ->
                                TeamMemberItem(
                                    user = member,
                                    onRemove = { 
                                        project?.let { 
                                            viewModel.removeTeamMember(it.id, member.uid) 
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add member dialog (only shown for project owner)
    if (showAddMemberDialog && hasManagementPrivileges) {
        AddTeamMemberDialog(
            allUsers = allUsers,
            currentTeamMembers = project?.teamMembers ?: emptyList(),
            onDismiss = { showAddMemberDialog = false },
            onAddMember = { userId ->
                project?.id?.let { projectId ->
                    viewModel.addTeamMember(projectId, userId)
                    showAddMemberDialog = false
                }
            }
        )
    }
}

@Composable
private fun TeamMemberItem(
    user: User,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { 
                Text(user.displayName ?: "Unnamed User") 
            },
            supportingContent = { 
                Text(
                    user.email ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            },
            trailingContent = {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.RemoveCircle,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingContent = {
                if (user.photoUrl != null) {
                    // You could use an AsyncImage here if you're using a library like Coil
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun AddTeamMemberDialog(
    allUsers: List<User>,
    currentTeamMembers: List<String>,
    onDismiss: () -> Unit,
    onAddMember: (String) -> Unit
) {
    val availableUsers = allUsers.filter { user -> !currentTeamMembers.contains(user.uid) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Team Member") },
        text = {
            if (availableUsers.isEmpty()) {
                Text("No users available to add")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableUsers) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = { 
                                    Text(user.displayName ?: "Unnamed User") 
                                },
                                supportingContent = { 
                                    Text(
                                        user.email ?: "",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.clickable { onAddMember(user.uid) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 