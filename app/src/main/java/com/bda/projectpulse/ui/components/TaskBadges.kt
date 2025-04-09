package com.bda.projectpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.models.TaskPriority
import com.bda.projectpulse.models.TaskStatus
import androidx.compose.ui.draw.clip

@Composable
fun TaskStatusBadge(status: TaskStatus) {
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
fun TaskPriorityBadge(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor) = when (priority) {
        TaskPriority.LOW -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        TaskPriority.MEDIUM -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        TaskPriority.HIGH -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        TaskPriority.URGENT -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = priority.name,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
} 