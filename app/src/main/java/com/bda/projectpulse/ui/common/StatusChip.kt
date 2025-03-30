package com.bda.projectpulse.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.uppercase()) {
        "PLANNING" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        "IN_PROGRESS", "INPROGRESS" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "COMPLETED" -> Pair(Color(0xFFE0F2F1), Color(0xFF00695C))
        "ON_HOLD", "ONHOLD" -> Pair(Color(0xFFFFF3E0), Color(0xFFEF6C00))
        "CANCELLED" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        "HIGH" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // For priority
        "MEDIUM" -> Pair(Color(0xFFFFF3E0), Color(0xFFEF6C00)) // For priority
        "LOW" -> Pair(Color(0xFFE0F2F1), Color(0xFF00695C)) // For priority
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
} 