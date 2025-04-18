package com.bda.projectpulse.ui.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.ProjectPriority
import com.bda.projectpulse.models.ProjectStatus
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProjectScreen(
    projectId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ProjectStatus.PLANNING) }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isNewProject by remember { mutableStateOf(projectId == null || projectId == "{projectId}") }

    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        if (!isNewProject) {
            viewModel.loadProjectById(projectId!!)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    LaunchedEffect(selectedProject) {
        selectedProject?.let { project ->
            name = project.name
            description = project.description
            status = project.status
            startDate = project.startDate.toDate()
            endDate = project.endDate?.toDate()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            showError = true
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { 
                showError = false
                viewModel.updateError(null)
            },
            title = { Text("Error") },
            text = { Text(error ?: "An unknown error occurred") },
            confirmButton = {
                TextButton(onClick = { 
                    showError = false
                    viewModel.updateError(null)
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewProject) "Create Project" else "Edit Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isBlank()) {
                                viewModel.updateError("Project name cannot be empty")
                                return@IconButton
                            }

                            if (isNewProject) {
                                viewModel.createProject(
                                    Project(
                                        id = "",
                                        name = name,
                                        description = description,
                                        status = status,
                                        startDate = Timestamp(startDate.time / 1000, 0),
                                        endDate = endDate?.let { Timestamp(it.time / 1000, 0) },
                                        ownerId = currentUser?.uid ?: "",
                                        teamMembers = listOf(currentUser?.uid ?: "")
                                    )
                                )
                            } else {
                                projectId?.let { id ->
                                    viewModel.updateProject(
                                        Project(
                                            id = id,
                                            name = name,
                                            description = description,
                                            status = status,
                                            startDate = Timestamp(startDate.time / 1000, 0),
                                            endDate = endDate?.let { Timestamp(it.time / 1000, 0) },
                                            ownerId = selectedProject?.ownerId ?: currentUser?.uid ?: "",
                                            teamMembers = selectedProject?.teamMembers ?: listOf(currentUser?.uid ?: "")
                                        )
                                    )
                                }
                            }
                            onNavigateBack()
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("Status", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectStatus.values().forEach { projectStatus ->
                        FilterChip(
                            selected = status == projectStatus,
                            onClick = { status = projectStatus },
                            label = { Text(projectStatus.name) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = "Start Date",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(startDate.toString())
                }

                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = "End Date",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(endDate?.toString() ?: "Set End Date")
                }
            }
        }

        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = startDate.time
            )
            
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                startDate = Date(it)
                            }
                            showStartDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showStartDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = { Text("Select Start Date") }
                )
            }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = endDate?.time
            )
            
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                endDate = Date(it)
                            }
                            showEndDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEndDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = { Text("Select End Date") }
                )
            }
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + horizontalArrangement.spacing.toPx().toInt()
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row ->
            row.maxOf { it.height }
        } + (rows.size - 1) * verticalArrangement.spacing.toPx().toInt()

        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalArrangement.spacing.toPx().toInt()
                }
                y += row.maxOf { it.height } + verticalArrangement.spacing.toPx().toInt()
            }
        }
    }
} 