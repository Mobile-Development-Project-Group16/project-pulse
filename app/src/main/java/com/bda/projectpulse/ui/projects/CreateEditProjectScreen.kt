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
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.ProjectPriority
import com.bda.projectpulse.models.ProjectStatus
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProjectScreen(
    viewModel: ProjectViewModel,
    projectId: String? = null,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ProjectStatus.PLANNING) }
    var priority by remember { mutableStateOf(ProjectPriority.MEDIUM) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        projectId?.let { id ->
            viewModel.loadProjectById(id)
        }
    }

    val selectedProject by viewModel.selectedProject.collectAsState()

    LaunchedEffect(selectedProject) {
        selectedProject?.let { project ->
            name = project.name
            description = project.description
            status = project.status
            priority = project.priority
            startDate = project.startDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            endDate = project.endDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectId == null) "Create Project" else "Edit Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val project = Project(
                                id = projectId ?: "",
                                name = name,
                                description = description,
                                status = status,
                                priority = priority,
                                startDate = startDate?.let { Timestamp(it.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond, 0) },
                                endDate = endDate?.let { Timestamp(it.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond, 0) }
                            )
                            if (projectId == null) {
                                viewModel.createProject(project)
                            } else {
                                viewModel.updateProject(projectId, project)
                            }
                            onSave()
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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

            Text("Priority", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectPriority.values().forEach { projectPriority ->
                    FilterChip(
                        selected = priority == projectPriority,
                        onClick = { priority = projectPriority },
                        label = { Text(projectPriority.name) }
                    )
                }
            }

            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(startDate?.toString() ?: "Set Start Date")
            }

            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(endDate?.toString() ?: "Set End Date")
            }
        }

        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.epochSecond?.times(1000)
                    ),
                    showModeToggle = false
                )
            }
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.epochSecond?.times(1000)
                    ),
                    showModeToggle = false
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