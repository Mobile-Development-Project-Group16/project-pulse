package com.bda.projectpulse.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bda.projectpulse.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskClick: (String) -> Unit,
    onCreateTask: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedPriority by remember { mutableStateOf<String?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(Icons.Default.Add, contentDescription = "Create Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Active Filters
            if (selectedStatus != null || selectedPriority != null) {
                ActiveFilters(
                    status = selectedStatus,
                    priority = selectedPriority,
                    onClearStatus = { selectedStatus = null },
                    onClearPriority = { selectedPriority = null }
                )
            }

            // Task List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(10) { index ->
                    TaskItem(
                        title = "Task ${index + 1}",
                        project = "Project ${(index / 3) + 1}",
                        assignee = "Team Member ${(index % 3) + 1}",
                        dueDate = "Mar ${28 + index}, 2024",
                        status = when (index % 3) {
                            0 -> "TODO"
                            1 -> "IN_PROGRESS"
                            else -> "COMPLETED"
                        },
                        priority = when (index % 3) {
                            0 -> "HIGH"
                            1 -> "MEDIUM"
                            else -> "LOW"
                        },
                        onClick = { onTaskClick("task_$index") }
                    )
                }
            }
        }

        // Filter Sheet
        if (showFilterSheet) {
            FilterSheet(
                selectedStatus = selectedStatus,
                selectedPriority = selectedPriority,
                onStatusSelected = { selectedStatus = it },
                onPrioritySelected = { selectedPriority = it },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@Composable
private fun ActiveFilters(
    status: String?,
    priority: String?,
    onClearStatus: () -> Unit,
    onClearPriority: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        status?.let {
            FilterChip(
                selected = true,
                onClick = onClearStatus,
                label = { Text("Status: $it") },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear status filter",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
        priority?.let {
            FilterChip(
                selected = true,
                onClick = onClearPriority,
                label = { Text("Priority: $it") },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear priority filter",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    selectedStatus: String?,
    selectedPriority: String?,
    onStatusSelected: (String?) -> Unit,
    onPrioritySelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filter Tasks",
                style = MaterialTheme.typography.titleLarge
            )

            // Status Filter
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TODO", "IN_PROGRESS", "COMPLETED").forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                        label = { Text(status) }
                    )
                }
            }

            // Priority Filter
            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("LOW", "MEDIUM", "HIGH").forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { onPrioritySelected(if (selectedPriority == priority) null else priority) },
                        label = { Text(priority) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskItem(
    title: String,
    project: String,
    assignee: String,
    dueDate: String,
    status: String,
    priority: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = project,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(
                        text = priority,
                        color = when (priority) {
                            "HIGH" -> Color(0xFFE53935)
                            "MEDIUM" -> Color(0xFFFFA000)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                    StatusChip(
                        text = status,
                        color = when (status) {
                            "TODO" -> Color(0xFFFFA000)
                            "IN_PROGRESS" -> Color(0xFF2196F3)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = assignee,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: $dueDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 