package com.bda.projectpulse.ui.tasks

import androidx.compose.foundation.clickable
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
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskPriority
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.User
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: String? = null,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.TODO) }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var selectedAssigneeId by remember { mutableStateOf<String?>(null) }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var showProjectPicker by remember { mutableStateOf(false) }
    var showAssigneePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        taskId?.let { id ->
            viewModel.loadTask(id)
        }
    }

    val task by viewModel.task.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val users by viewModel.users.collectAsState()

    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description
            status = it.status
            priority = it.priority
            selectedProjectId = it.projectId
            selectedAssigneeId = it.assignedTo
            dueDate = it.dueDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        }
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
                            viewModel.saveTask(
                                Task(
                                    id = taskId ?: "",
                                    title = title,
                                    description = description,
                                    status = status,
                                    priority = priority,
                                    projectId = selectedProjectId ?: "",
                                    assignedTo = selectedAssigneeId,
                                    dueDate = dueDate?.let { Timestamp(it.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond, 0) }
                                )
                            )
                            onSave()
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Text("Status", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskStatus.values().forEach { taskStatus ->
                        FilterChip(
                            selected = status == taskStatus,
                            onClick = { status = taskStatus },
                            label = { Text(taskStatus.name) }
                        )
                    }
                }
            }

            item {
                Text("Priority", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { taskPriority ->
                        FilterChip(
                            selected = priority == taskPriority,
                            onClick = { priority = taskPriority },
                            label = { Text(taskPriority.name) }
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showProjectPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(projects.find { it.id == selectedProjectId }?.name ?: "Select Project")
                }
            }

            item {
                OutlinedButton(
                    onClick = { showAssigneePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(users.find { it.id == selectedAssigneeId }?.displayName ?: "Select Assignee")
                }
            }

            item {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dueDate?.toString() ?: "Set Due Date")
                }
            }
        }

        if (showProjectPicker) {
            AlertDialog(
                onDismissRequest = { showProjectPicker = false },
                title = { Text("Select Project") },
                text = {
                    LazyColumn {
                        items(projects) { project ->
                            ListItem(
                                headlineContent = { Text(project.name) },
                                supportingContent = { Text(project.description) },
                                modifier = Modifier.clickable {
                                    selectedProjectId = project.id
                                    showProjectPicker = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProjectPicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAssigneePicker) {
            AlertDialog(
                onDismissRequest = { showAssigneePicker = false },
                title = { Text("Select Assignee") },
                text = {
                    LazyColumn {
                        items(users) { user ->
                            ListItem(
                                headlineContent = { Text(user.displayName) },
                                supportingContent = { Text(user.email) },
                                modifier = Modifier.clickable {
                                    selectedAssigneeId = user.id
                                    showAssigneePicker = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAssigneePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.epochSecond?.times(1000)
                    ),
                    showModeToggle = false
                )
            }
        }
    }
} 