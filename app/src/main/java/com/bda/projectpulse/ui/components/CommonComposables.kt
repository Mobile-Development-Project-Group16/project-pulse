package com.bda.projectpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.models.ProjectStatus
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.User

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .background(
                color = color,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun StatusChip(status: ProjectStatus) {
    val (text, color) = when (status) {
        ProjectStatus.ACTIVE -> "Active" to Color(0xFF4CAF50)
        ProjectStatus.ON_HOLD -> "On Hold" to Color(0xFFFFA000)
        ProjectStatus.COMPLETED -> "Completed" to Color(0xFF2196F3)
        ProjectStatus.CANCELLED -> "Cancelled" to Color(0xFFE53935)
        ProjectStatus.TODO -> "To Do" to Color(0xFF9E9E9E)
        ProjectStatus.PLANNING -> "Planning" to Color(0xFF9C27B0)
    }
    StatusChip(text = text, color = color)
}

@Composable
fun TaskStatusChip(status: TaskStatus) {
    val (text, color) = when (status) {
        TaskStatus.TODO -> "To Do" to Color(0xFF9E9E9E)
        TaskStatus.IN_PROGRESS -> "In Progress" to Color(0xFFFFA000)
        TaskStatus.IN_REVIEW -> "In Review" to Color(0xFF2196F3)
        TaskStatus.APPROVED -> "Approved" to Color(0xFF4CAF50)
        TaskStatus.REJECTED -> "Rejected" to Color(0xFFE53935)
    }
    StatusChip(text = text, color = color)
}

@Composable
fun TeamMemberItem(
    user: User,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Remove team member"
                )
            }
        }
    }
}

@Composable
fun AddTeamMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Team Member") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(email)
                    onDismiss()
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 