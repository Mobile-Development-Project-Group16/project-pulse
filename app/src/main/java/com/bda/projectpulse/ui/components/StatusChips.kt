package com.bda.projectpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.models.ProjectStatus
import com.bda.projectpulse.models.TaskStatus

@Composable
fun StatusChip(status: ProjectStatus) {
    val textColor = when (status) {
        ProjectStatus.TODO -> Color(0xFF6A1B9A)
        ProjectStatus.PLANNING -> Color(0xFF6A1B9A)
        ProjectStatus.ACTIVE -> Color(0xFFE65100)
        ProjectStatus.COMPLETED -> Color(0xFF1B5E20)
        ProjectStatus.ON_HOLD -> Color(0xFF0288D1)
        ProjectStatus.CANCELLED -> Color(0xFFC62828)
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
fun TaskStatusChip(status: TaskStatus, modifier: Modifier = Modifier) {
    val (color, text) = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.primary to "To Do"
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to "In Progress"
        TaskStatus.IN_REVIEW -> MaterialTheme.colorScheme.tertiary to "In Review"
        TaskStatus.APPROVED -> Color.Green to "Approved"
        TaskStatus.REJECTED -> Color.Red to "Rejected"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusChip(status: TaskStatus) {
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