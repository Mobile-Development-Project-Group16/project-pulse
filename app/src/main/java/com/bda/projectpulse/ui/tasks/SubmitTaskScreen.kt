package com.bda.projectpulse.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.R
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskPriority
import com.bda.projectpulse.models.Attachment
import com.bda.projectpulse.models.AttachmentType
import com.bda.projectpulse.ui.components.*
import com.bda.projectpulse.ui.theme.ProjectPulseTheme
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavHostController
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitTaskScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    var submissionText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var attachmentUrl by remember { mutableStateOf("") }

    val error = viewModel.error.value
    val task by viewModel.selectedTask.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.loadTaskById(taskId)
    }

    LaunchedEffect(error) {
        if (error != null) {
            showError = true
            errorMessage = error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Task") },
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
                value = submissionText,
                onValueChange = { submissionText = it },
                label = { Text("Submission Text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Attachments Section
            Text(
                text = "Attachments",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (attachments.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    items(attachments) { attachment ->
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
                                text = attachment.url,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    attachments = attachments.filter { it != attachment }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove attachment")
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showAttachmentDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Add Attachment")
            }

            if (showAttachmentDialog) {
                AlertDialog(
                    onDismissRequest = { showAttachmentDialog = false },
                    title = { Text("Add Attachment") },
                    text = {
                        OutlinedTextField(
                            value = attachmentUrl,
                            onValueChange = { attachmentUrl = it },
                            label = { Text("Attachment URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (attachmentUrl.isNotBlank()) {
                                    attachments = attachments + Attachment(
                                        id = UUID.randomUUID().toString(),
                                        url = attachmentUrl,
                                        name = attachmentUrl.substringAfterLast("/"),
                                        type = AttachmentType.OTHER,
                                        uploadedAt = Timestamp.now()
                                    )
                                    attachmentUrl = ""
                                    showAttachmentDialog = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAttachmentDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Button(
                onClick = {
                    if (submissionText.isBlank()) {
                        showError = true
                        errorMessage = "Please enter submission text"
                        return@Button
                    }

                    isLoading = true
                    task?.let { currentTask ->
                        val updatedTask = currentTask.copy(
                            submissionText = submissionText,
                            attachments = attachments,
                            status = TaskStatus.IN_REVIEW,
                            updatedAt = Timestamp.now()
                        )
                        viewModel.updateTask(updatedTask)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && task != null
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

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
} 