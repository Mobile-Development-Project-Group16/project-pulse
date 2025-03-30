package com.bda.projectpulse.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.models.ProjectStatus
import com.bda.projectpulse.models.TaskStatus
import androidx.compose.material3.SuggestionChip
import androidx.compose.ui.graphics.Color

@Composable
fun StatusChip(status: ProjectStatus) {
    val (backgroundColor, textColor) = when (status) {
        ProjectStatus.TODO -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
        ProjectStatus.PLANNING -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
        ProjectStatus.ACTIVE -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        ProjectStatus.COMPLETED -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        ProjectStatus.ON_HOLD -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
        ProjectStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    SuggestionChip(
        onClick = { },
        label = {
            Text(
                text = status.name,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        },
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
fun TaskStatusChip(status: TaskStatus) {
    val (color, text) = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.primary to "To Do"
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to "In Progress"
        TaskStatus.IN_REVIEW -> MaterialTheme.colorScheme.tertiary to "In Review"
        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primary to "Completed"
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