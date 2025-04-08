package com.bda.projectpulse.ui.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    projectId: String,
    projectName: String,
    onNavigateBack: () -> Unit,
    viewModel: TeamManagementViewModel = hiltViewModel()
) {
    val teamMembers by viewModel.teamMembers.collectAsStateWithLifecycle()
    val availableUsers by viewModel.availableUsers.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }

    LaunchedEffect(projectId) {
        viewModel.loadTeamMembers(projectId)
        viewModel.loadAvailableUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$projectName Team") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teamMembers) { member ->
                    TeamMemberCard(
                        member = member,
                        onRemove = { viewModel.removeTeamMember(projectId, member.uid) },
                        onRoleChange = { newRole ->
                            viewModel.updateTeamMemberRole(projectId, member.uid, newRole)
                        }
                    )
                }
            }
        }

        if (showAddMemberDialog) {
            AlertDialog(
                onDismissRequest = { showAddMemberDialog = false },
                title = { Text("Add Team Member") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = selectedUser?.displayName ?: "",
                            onValueChange = { },
                            label = { Text("Select User") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { /* TODO: Show user selection dialog */ }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select User")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = selectedRole?.name ?: "",
                            onValueChange = { },
                            label = { Text("Select Role") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { /* TODO: Show role selection dialog */ }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Role")
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedUser?.let { user ->
                                selectedRole?.let { role ->
                                    viewModel.addTeamMember(projectId, user.uid, role)
                                    showAddMemberDialog = false
                                }
                            }
                        },
                        enabled = selectedUser != null && selectedRole != null
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddMemberDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun TeamMemberCard(
    member: User,
    onRemove: () -> Unit,
    onRoleChange: (UserRole) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = member.role.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Remove, contentDescription = "Remove Member")
            }
        }
    }
} 