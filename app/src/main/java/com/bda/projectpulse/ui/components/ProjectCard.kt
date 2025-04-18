package com.bda.projectpulse.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.models.Project
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectCard(
    project: Project,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyLarge
            )
            
            // Date information
            Spacer(modifier = Modifier.height(8.dp))
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Date",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val startDate = dateFormat.format(project.startDate.toDate())
                val endDate = project.endDate?.let { 
                    dateFormat.format(it.toDate()) 
                } ?: "No end date"
                Text(
                    text = "Duration: $startDate to $endDate",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Status information
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                StatusChip(status = project.status)
            }
        }
    }
} 