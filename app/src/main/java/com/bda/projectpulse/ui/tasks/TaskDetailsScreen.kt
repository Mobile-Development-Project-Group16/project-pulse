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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskPriority
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.navigation.Screen
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
    navController: NavHostController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val task: Task? by viewModel.selectedTask.collectAsStateWithLifecycle()
    val error: String? by viewModel.error.collectAsStateWithLifecycle()
    val users: List<User> by viewModel.users.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showRejectionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var rejectionComment by remember { mutableStateOf("") }

    val assignees = remember(task?.assigneeIds, users) {
        task?.assigneeIds?.mapNotNull { userId ->
            users.find { it.uid == userId }
        } ?: emptyList()
    }

    LaunchedEffect(taskId) {
        viewModel.loadTaskById(taskId)
        viewModel.loadUsers()
    }

    // Rejection Dialog
    if (showRejectionDialog) {
        AlertDialog(
            onDismissRequest = { showRejectionDialog = false },
            title = { Text("Reject Task") },
            text = {
                Column {
                    Text("Please provide a reason for rejection:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionComment,
                        onValueChange = { rejectionComment = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        isError = rejectionComment.isBlank()
                    )
                    if (rejectionComment.isBlank()) {
                        Text(
                            text = "Please enter a rejection reason",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (rejectionComment.isNotBlank()) {
                            task?.let { currentTask ->
                                viewModel.rejectTask(currentTask.id, rejectionComment)
                            }
                            showRejectionDialog = false
                            rejectionComment = ""
                        }
                    }
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRejectionDialog = false
                    rejectionComment = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        task?.let { currentTask ->
                            viewModel.deleteTask(currentTask.id)
                            onNavigateBack()
                        }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    task?.let { currentTask ->
                        if (currentTask.status == TaskStatus.TODO || currentTask.status == TaskStatus.IN_PROGRESS) {
                            TextButton(
                                onClick = { 
                                    navController.navigate(Screen.SubmitTask.createRoute(currentTask.id))
                                }
                            ) {
                                Text("Submit")
                            }
                        }

                        // Show delete option for admin/manager
                        if (currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.MANAGER) {
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Task",
                                    tint = Color.Red
                                )
                            }
                        }

                        IconButton(onClick = { onEditTask(currentTask) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                task?.let { currentTask ->
                    // Task Details Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        TaskDetails(
                            task = currentTask,
                    assignees = assignees,
                    viewModel = viewModel
                )
                    }

                    // Submission Details Card (if available)
                    if (currentTask.submissionText.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Submission Text",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentTask.submissionText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    // Attachments Card (if available)
                    if (currentTask.attachments.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Attachments",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                currentTask.attachments.forEach { attachment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = attachment.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = { /* TODO: Handle attachment download */ }
                                        ) {
                                            Text("Download")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Approve/Reject Buttons (if in review and user is admin/manager)
                    if (currentTask.status == TaskStatus.IN_REVIEW && 
                        (currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.MANAGER)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { viewModel.approveTask(currentTask.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Green
                                    )
                                ) {
                                    Text("Approve")
                                }
                                Button(
                                    onClick = { showRejectionDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red
                                    )
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }

                    // Rejection Comment Card (if rejected)
                    if (currentTask.status == TaskStatus.IN_PROGRESS && currentTask.rejectionComment.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Rejection Reason:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentTask.rejectionComment,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun TaskDetails(
    task: Task,
    assignees: List<User>,
    viewModel: TaskViewModel
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
                ) {
                    // Title and priority
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                style = MaterialTheme.typography.titleLarge
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
                                viewModel.updateTaskPriority(priority)
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
                    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                    val allowedStatuses = when {
                        // Admin and managers can change to any status
                        currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.MANAGER -> {
                            TaskStatus.values().toList()
                        }
                        // Regular users can only change between TODO and IN_PROGRESS if:
                        // 1. Task hasn't been submitted yet (is in TODO or IN_PROGRESS)
                        // 2. Task was rejected (back to IN_PROGRESS)
                        task.status == TaskStatus.TODO || task.status == TaskStatus.IN_PROGRESS -> {
                            listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS)
                        }
                        // If task was rejected, allow changing back to IN_PROGRESS
                        task.status == TaskStatus.REJECTED -> {
                            listOf(TaskStatus.IN_PROGRESS)
                        }
                        // For all other cases (IN_REVIEW, APPROVED), no status changes allowed
                        else -> {
                            emptyList()
                        }
                    }
                    
                    if (allowedStatuses.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No status changes allowed") },
                            onClick = { showStatusMenu = false },
                            enabled = false
                        )
                    } else {
                        allowedStatuses.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.name.replace("_", " ")) },
                                        onClick = {
                                    viewModel.updateTaskStatus(status)
                                            showStatusMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (status) {
                                                    TaskStatus.TODO -> Icons.Default.Assignment
                                                    TaskStatus.IN_PROGRESS -> Icons.Default.DirectionsRun
                                                    TaskStatus.IN_REVIEW -> Icons.Default.RateReview
                                            TaskStatus.APPROVED -> Icons.Default.CheckCircle
                                            TaskStatus.REJECTED -> Icons.Default.Cancel
                                                },
                                                contentDescription = null
                                            )
                                        }
                                    )
                        }
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
        
        // Show submission text if available and task is in review
        if (task.status == TaskStatus.IN_REVIEW && task.submissionText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Submission Text",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.submissionText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
                    
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
                                text = user.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (user.email.isNotEmpty()) {
                                            Text(
                                    text = user.email,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                            }
                        }
                    }
                }
            }
        }
                    
        // Subtasks if any
        if (task.subTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Subtasks",
                style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            
            task.subTasks.forEach { subtask ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Comments",
                style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            
            task.comments.forEach { comment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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