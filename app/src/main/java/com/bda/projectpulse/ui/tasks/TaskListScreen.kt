package com.bda.projectpulse.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.*
import com.bda.projectpulse.ui.components.TaskStatusChip
import androidx.navigation.NavHostController
import com.bda.projectpulse.navigation.Screen
import com.bda.projectpulse.navigation.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    projectId: String? = null,
    onTaskClick: (String) -> Unit,
    onCreateTask: () -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    LaunchedEffect(projectId) {
        if (projectId != null) {
            viewModel.loadProjectTasks(projectId)
        } else {
            viewModel.loadAllTasks()
        }
    }

    // Filter tasks based on selected tab
    val filteredTasks = remember(uiState.tasks, selectedTab) {
        when (selectedTab) {
            0 -> uiState.tasks // All Tasks
            1 -> uiState.tasks.filter { it.status == TaskStatus.TODO }
            2 -> uiState.tasks.filter { it.status == TaskStatus.IN_PROGRESS }
            3 -> uiState.tasks.filter { it.status == TaskStatus.IN_REVIEW }
            4 -> uiState.tasks.filter { it.status == TaskStatus.APPROVED }
            5 -> uiState.tasks.filter { it.status == TaskStatus.REJECTED }
            else -> uiState.tasks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${filteredTasks.size} tasks",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                navigationIcon = {
                    if (projectId != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.MANAGER) {
                        IconButton(onClick = onCreateTask) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create Task",
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.shapes.small
                                    )
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (projectId == null) {
                BottomNavigationBar(
                    currentRoute = Screen.TaskList.route,
                    onNavigate = { route -> 
                        if (route != Screen.TaskList.route) {
                            navController.navigate(route) {
                                popUpTo(Screen.TaskList.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("All Tasks", "To Do", "In Progress", "In Review", "Approved", "Rejected").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (selectedTab == index) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        )
                    }
                }

                // Task Groups
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // To Do Group
                    item {
                        TaskGroup(
                            title = "To Do",
                            taskCount = filteredTasks.count { it.status == TaskStatus.TODO },
                            tasks = filteredTasks.filter { it.status == TaskStatus.TODO },
                            onTaskClick = onTaskClick
                        )
                    }

                    // In Progress Group
                    item {
                        TaskGroup(
                            title = "In Progress",
                            taskCount = filteredTasks.count { it.status == TaskStatus.IN_PROGRESS },
                            tasks = filteredTasks.filter { it.status == TaskStatus.IN_PROGRESS },
                            onTaskClick = onTaskClick
                        )
                    }

                    // In Review Group
                    item {
                        TaskGroup(
                            title = "In Review",
                            taskCount = filteredTasks.count { it.status == TaskStatus.IN_REVIEW },
                            tasks = filteredTasks.filter { it.status == TaskStatus.IN_REVIEW },
                            onTaskClick = onTaskClick
                        )
                    }

                    // Approved Group
                    item {
                        TaskGroup(
                            title = "Approved",
                            taskCount = filteredTasks.count { it.status == TaskStatus.APPROVED },
                            tasks = filteredTasks.filter { it.status == TaskStatus.APPROVED },
                            onTaskClick = onTaskClick
                        )
                    }

                    // Rejected Group
                    item {
                        TaskGroup(
                            title = "Rejected",
                            taskCount = filteredTasks.count { it.status == TaskStatus.REJECTED },
                            tasks = filteredTasks.filter { it.status == TaskStatus.REJECTED },
                            onTaskClick = onTaskClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskGroup(
    title: String,
    taskCount: Int,
    tasks: List<Task>,
    onTaskClick: (String) -> Unit
) {
    if (tasks.isNotEmpty()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "$taskCount tasks",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Due Date",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(task.dueDate?.toDate() ?: Date()),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // User avatar would go here
                    Text(
                        text = if (task.assigneeIds.isNotEmpty()) "${task.assigneeIds.size} assigned" else "Unassigned",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                TaskStatusChip(status = task.status)
            }
        }
    }
} 