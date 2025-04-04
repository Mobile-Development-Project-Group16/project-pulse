package com.bda.projectpulse.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskPriority
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.ui.components.TaskPriorityBadge
import com.bda.projectpulse.ui.components.TaskStatusChip
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onEditTask: (Task) -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val task by viewModel.selectedTask.collectAsStateWithLifecycle(initialValue = null)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val error = viewModel.error.value
    val users by viewModel.users.collectAsStateWithLifecycle(initialValue = emptyList())
    val assignees = remember(task?.assigneeIds, users) {
        task?.assigneeIds?.mapNotNull { userId ->
            users.find { it.uid == userId }
        } ?: emptyList()
    }

    LaunchedEffect(taskId) {
        viewModel.loadTaskById(taskId)
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = task?.title ?: "Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        task?.let { t -> onEditTask(t) }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text(
                    text = "Error: ${error}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                task != null -> TaskDetails(
                    task = task!!,
                    assignees = assignees,
                    viewModel = viewModel
                )
                else -> Text(
                    text = "Task not found",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun TaskDetails(
    task: Task,
    assignees: List<com.bda.projectpulse.models.User>,
    viewModel: TaskViewModel
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title and priority
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        // Priority badge with dropdown menu
                        Box {
                            TaskPriorityBadge(
                                priority = task.priority,
                                modifier = Modifier.clickable { showPriorityMenu = true }
                            )
                            
                            DropdownMenu(
                                expanded = showPriorityMenu,
                                onDismissRequest = { showPriorityMenu = false }
                            ) {
                                TaskPriority.values().forEach { priority ->
                                    DropdownMenuItem(
                                        text = { Text(priority.name) },
                                        onClick = {
                                            viewModel.updateTaskPriority(task.id, priority)
                                            showPriorityMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Status with dropdown menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showStatusMenu = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                TaskStatusChip(status = task.status)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                TaskStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.name.replace("_", " ")) },
                                        onClick = {
                                            viewModel.updateTaskStatus(task.id, status)
                                            showStatusMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (status) {
                                                    TaskStatus.TODO -> Icons.Default.Assignment
                                                    TaskStatus.IN_PROGRESS -> Icons.Default.DirectionsRun
                                                    TaskStatus.IN_REVIEW -> Icons.Default.RateReview
                                                    TaskStatus.COMPLETED -> Icons.Default.Done
                                                },
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Dates
                    Text(
                        text = "Timeline",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    
                    // Created/Updated dates
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange, 
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Created: ${dateFormat.format(task.createdAt.toDate())}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Update, 
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Updated: ${dateFormat.format(task.updatedAt.toDate())}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Due date
                    task.dueDate?.let { dueDate ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday, 
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Due: ${dateFormat.format(dueDate.toDate())}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Assignment
                    Text(
                        text = "Assignees",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (assignees.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person, 
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "No assignees",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Column {
                            assignees.forEach { user ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person, 
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            user.displayName ?: "Unnamed User",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (user.email.isNotEmpty()) {
                                            Text(
                                                user.email,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
                    
        // Subtasks if any
        if (task.subTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Subtasks",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(task.subTasks) { subtask ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subtask.completed,
                            onCheckedChange = null
                        )
                        Text(
                            text = subtask.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Comments if any
        if (task.comments.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(task.comments) { comment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.getUserName(comment.authorId),
                                style = MaterialTheme.typography.labelLarge
                            )
                            val commentDateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            Text(
                                text = commentDateFormat.format(comment.createdAt.toDate()),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comment.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
} 