package com.bda.projectpulse.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.models.ProjectStatus
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.User
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

@Composable
fun ErrorMessage(error: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun StatusChip(status: ProjectStatus) {
    val (color, text) = when (status) {
        ProjectStatus.TODO -> MaterialTheme.colorScheme.primary to "Planning"
        ProjectStatus.PLANNING -> MaterialTheme.colorScheme.primary to "Planning"
        ProjectStatus.ACTIVE -> MaterialTheme.colorScheme.secondary to "Active"
        ProjectStatus.COMPLETED -> MaterialTheme.colorScheme.primary to "Completed"
        ProjectStatus.ON_HOLD -> MaterialTheme.colorScheme.tertiary to "On Hold"
        ProjectStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TaskStatusChip(status: TaskStatus) {
    val (backgroundColor, textColor) = when (status) {
        TaskStatus.TODO -> Color.Gray to Color.White
        TaskStatus.IN_PROGRESS -> Color.Blue to Color.White
        TaskStatus.IN_REVIEW -> Color.Yellow to Color.Black
        TaskStatus.APPROVED -> Color.Green to Color.White
        TaskStatus.REJECTED -> Color.Red to Color.White
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun AddTeamMemberDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredUsers: List<User>,
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Team Member") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search by name or email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                ) {
                    items(filteredUsers) { user ->
                        UserItem(
                            user = user,
                            onClick = { onUserSelected(user) }
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

@Composable
fun TeamMemberItem(
    userName: String,
    userEmail: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Clear, contentDescription = "Remove Member")
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 