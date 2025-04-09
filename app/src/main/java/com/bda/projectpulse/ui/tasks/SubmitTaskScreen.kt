package com.bda.projectpulse.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.R
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskPriority
import com.bda.projectpulse.ui.components.*
import com.bda.projectpulse.ui.theme.ProjectPulseTheme
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitTaskScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error = viewModel.error.value

    LaunchedEffect(error) {
        if (error != null) {
            showError = true
            errorMessage = error ?: "An error occurred"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit New Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Priority Selection
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

            // Due Date Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dueDate?.let { SimpleDateFormat("MMM dd, yyyy").format(it) } ?: "Select Due Date")
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                androidx.compose.material3.DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    dueDate = Date(it)
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                                dueDate = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = true
                    )
                }
            }

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        showError = true
                        errorMessage = "Title cannot be empty"
                        return@Button
                    }

                    val task = Task(
                        id = "",
                        title = title,
                        description = description,
                        projectId = projectId,
                        status = TaskStatus.TODO,
                        priority = priority,
                        assigneeIds = emptyList(),
                        dueDate = dueDate?.let { Timestamp(it.time / 1000, 0) },
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now(),
                        subTasks = emptyList(),
                        comments = emptyList(),
                        attachments = emptyList(),
                        createdBy = ""
                    )

                    viewModel.saveTask(task)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Task")
                }
            }
        }
    }
} 