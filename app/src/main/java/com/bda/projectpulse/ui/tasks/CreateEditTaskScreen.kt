package com.bda.projectpulse.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.bda.projectpulse.models.User
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    projectId: String,
    taskId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val selectedTask by viewModel.selectedTask.collectAsStateWithLifecycle(initialValue = null)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val teamMembers by viewModel.projectTeamMembers.collectAsStateWithLifecycle(initialValue = emptyList())
    val error = viewModel.error.value
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var assigneeIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAssigneeSelector by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Load task if editing and load project team members
    LaunchedEffect(taskId, projectId) {
        if (taskId != null) {
            viewModel.loadTaskById(taskId)
        }
        viewModel.loadProjectTeamMembers(projectId)
    }
    
    // Update UI when task is loaded
    LaunchedEffect(selectedTask) {
        selectedTask?.let { task ->
            title = task.title
            description = task.description
            priority = task.priority
            assigneeIds = task.assigneeIds
            dueDate = task.dueDate?.toDate()
        }
    }

    // Handle errors
    LaunchedEffect(error) {
        error?.let {
            errorMessage = it
            showError = true
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = Date(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAssigneeSelector) {
        AssigneeSelectionDialog(
            users = teamMembers,
            selectedAssigneeIds = assigneeIds,
            onAssigneeSelected = { userId, isSelected ->
                assigneeIds = if (isSelected) {
                    assigneeIds + userId
                } else {
                    assigneeIds - userId
                }
            },
            onDismiss = { showAssigneeSelector = false }
        )
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { 
                showError = false
                viewModel.clearError()
            },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    showError = false
                    viewModel.clearError()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "Create Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Title cannot be empty"
                                showError = true
                                return@IconButton
                            }

                            val task = Task(
                                id = taskId ?: "",
                                title = title,
                                description = description,
                                projectId = projectId,
                                status = selectedTask?.status ?: TaskStatus.TODO,
                                priority = priority,
                                assigneeIds = assigneeIds,
                                dueDate = dueDate?.let { Timestamp(it.time / 1000, 0) },
                                createdAt = selectedTask?.createdAt ?: Timestamp.now(),
                                updatedAt = Timestamp.now(),
                                subTasks = selectedTask?.subTasks ?: emptyList(),
                                comments = selectedTask?.comments ?: emptyList()
                            )

                            viewModel.saveTask(task)
                            if (error == null) {
                                onNavigateBack()
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text("Priority", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskPriority.values().forEach { priorityOption ->
                            FilterChip(
                                selected = priority == priorityOption,
                                onClick = { priority = priorityOption },
                                label = { Text(priorityOption.name) }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(dueDate?.let { "Due: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)}" } ?: "Set Due Date")
                    }
                    
                    OutlinedButton(
                        onClick = { showAssigneeSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val assigneeText = when {
                            assigneeIds.isEmpty() -> "Assign Task"
                            assigneeIds.size == 1 -> "Assigned to: ${viewModel.getUserName(assigneeIds.first())}"
                            else -> "Assigned to ${assigneeIds.size} team members"
                        }
                        Text(assigneeText)
                    }
                    
                    if (assigneeIds.isNotEmpty()) {
                        Text("Assignees:", style = MaterialTheme.typography.labelLarge)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(assigneeIds) { userId ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(viewModel.getUserName(userId))
                                    IconButton(onClick = { assigneeIds = assigneeIds - userId }) {
                                        Icon(
                                            Icons.Default.Close, 
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssigneeSelectionDialog(
    users: List<User>,
    selectedAssigneeIds: List<String>,
    onAssigneeSelected: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Assignees") },
        text = {
            if (users.isEmpty()) {
                Text("No team members available for this project. Add team members to the project before assigning tasks.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = selectedAssigneeIds.contains(user.uid),
                                onCheckedChange = { isChecked ->
                                    onAssigneeSelected(user.uid, isChecked)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.displayName)
                                Text(
                                    user.email,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
} 