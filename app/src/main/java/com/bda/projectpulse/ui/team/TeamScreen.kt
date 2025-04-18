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
import com.bda.projectpulse.models.User
import com.bda.projectpulse.ui.components.LoadingIndicator

@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }

    if (isLoading) {
        LoadingIndicator()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                if (it.isNotEmpty()) {
                    viewModel.searchUsers(it)
                    showSearchResults = true
                } else {
                    showSearchResults = false
                }
            },
            label = { Text("Search users by email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        showSearchResults = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (showSearchResults) {
            // Search Results
            Text(
                text = "Search Results",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { user ->
                    UserSearchItem(
                        user = user,
                        onAdd = {
                            viewModel.addTeamMember(user)
                            searchQuery = ""
                            showSearchResults = false
                        }
                    )
                }
            }
        } else {
            // Team Members List
            Text(
                text = "Team Members",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teamMembers) { member ->
                    TeamMemberItem(
                        member = member,
                        onRemove = { viewModel.removeTeamMember(member) }
                    )
                }
            }
        }

        // Error Message
        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun UserSearchItem(
    user: User,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = user.displayName ?: "No Name",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = user.email ?: "No Email",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add to team")
            }
        }
    }
}

@Composable
private fun TeamMemberItem(
    member: User,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = member.displayName ?: "No Name",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = member.email ?: "No Email",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Remove, contentDescription = "Remove from team")
            }
        }
    }
} 