package com.bda.projectpulse.ui.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bda.projectpulse.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProjectViewModel = viewModel()
) {
    val selectedProject by viewModel.selectedProject.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAddMemberDialog by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        viewModel.loadProjectById(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Team Member")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                selectedProject?.teamMembers.isNullOrEmpty() -> {
                    Text(
                        text = "No team members assigned",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedProject?.teamMembers ?: emptyList()) { memberId ->
                            TeamMemberItem(
                                memberId = memberId,
                                onRemove = { userId ->
                                    viewModel.removeTeamMember(projectId, userId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add Team Member Dialog
        if (showAddMemberDialog) {
            AddTeamMemberDialog(
                onDismiss = { showAddMemberDialog = false },
                onAddMember = { userId ->
                    viewModel.addTeamMember(projectId, userId)
                    showAddMemberDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamMemberItem(
    memberId: String,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(memberId) },
            trailingContent = {
                IconButton(onClick = { onRemove(memberId) }) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Remove member",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTeamMemberDialog(
    onDismiss: () -> Unit,
    onAddMember: (String) -> Unit,
    viewModel: ProjectViewModel = viewModel()
) {
    val users by viewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Team Member") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Users") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User list
                if (users.isEmpty()) {
                    Text(
                        text = "No users available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    users.filter { user ->
                        user.displayName.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true)
                    }.forEach { user ->
                        ListItem(
                            headlineContent = { Text(user.displayName) },
                            supportingContent = { Text(user.email) },
                            modifier = Modifier.clickable { onAddMember(user.uid) }
                        )
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