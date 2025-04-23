package com.bda.projectpulse.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.ui.components.MainBottomBar
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import java.util.Date
import com.bda.projectpulse.ui.notifications.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDashboardScreen(
    onNavigateToUserManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProfile: () -> Unit,
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val notificationState by notificationViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
        notificationViewModel.refreshNotifications()
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                navController = navController,
                unreadNotificationCount = uiState.unreadNotificationCount
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF3F4F6))
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${currentUser?.role?.name ?: "User"} Dashboard",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        )
                        Text(
                            text = "Welcome back, ${currentUser?.email ?: "User"}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF6B7280)
                            )
                        )
                    }
                }

                // Stats Grid
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        when (currentUser?.role) {
                            UserRole.ADMIN -> {
                                // Admin Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatCard(
                                        title = "Total Users",
                                        value = uiState.stats.totalUsers.toString(),
                                        icon = Icons.Default.People,
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.weight(1f),
                                        onClick = onNavigateToUserManagement
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    StatCard(
                                        title = "Total Projects",
                                        value = uiState.stats.totalProjects.toString(),
                                        icon = Icons.Default.Folder,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.weight(1f),
                                        onClick = onNavigateToProjects
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatCard(
                                        title = "Active Projects",
                                        value = uiState.stats.activeProjects.toString(),
                                        icon = Icons.Default.CheckCircle,
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f),
                                        onClick = onNavigateToProjects
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    StatCard(
                                        title = "Pending Tasks",
                                        value = uiState.stats.pendingTasks.toString(),
                                        icon = Icons.Default.Assignment,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.weight(1f),
                                        onClick = onNavigateToTasks
                                    )
                                }
                            }
                            UserRole.MANAGER -> {
                                // Manager Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatCard(
                                        title = "My Projects",
                                        value = uiState.stats.totalProjects.toString(),
                                        icon = Icons.Default.Folder,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    StatCard(
                                        title = "Team Members",
                                        value = uiState.stats.totalTeamMembers.toString(),
                                        icon = Icons.Default.People,
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatCard(
                                        title = "Active Tasks",
                                        value = uiState.stats.pendingTasks.toString(),
                                        icon = Icons.Default.CheckCircle,
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    StatCard(
                                        title = "Overdue Tasks",
                                        value = uiState.stats.pendingTasks.toString(),
                                        icon = Icons.Default.Warning,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            else -> {
                                // User Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatCard(
                                        title = "Completed",
                                        value = uiState.stats.approvedTasks.toString(),
                                        icon = Icons.Default.CheckCircle,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    StatCard(
                                        title = "In Progress",
                                        value = uiState.stats.submittedTasks.toString(),
                                        icon = Icons.Default.Timer,
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatCard(
                                        title = "Overdue",
                                        value = uiState.stats.rejectedTasks.toString(),
                                        icon = Icons.Default.Warning,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    StatCard(
                                        title = "My Projects",
                                        value = uiState.stats.totalProjects.toString(),
                                        icon = Icons.Default.Folder,
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Menu Options
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (currentUser?.role) {
                            UserRole.ADMIN -> {
                                MenuOptionCard(
                                    title = "User Management",
                                    description = "Manage users, roles & permissions",
                                    icon = Icons.Default.ManageAccounts,
                                    onClick = onNavigateToUserManagement
                                )
                                MenuOptionCard(
                                    title = "Settings",
                                    description = "Configure app preferences",
                                    icon = Icons.Default.Settings,
                                    onClick = onNavigateToSettings
                                )
                            }
                            UserRole.MANAGER -> {
                                MenuOptionCard(
                                    title = "Project Management",
                                    description = "Manage your projects and teams",
                                    icon = Icons.Default.Folder,
                                    onClick = onNavigateToProjects
                                )
                                MenuOptionCard(
                                    title = "Profile",
                                    description = "View and edit your profile",
                                    icon = Icons.Default.Person,
                                    onClick = onNavigateToProfile
                                )
                            }
                            else -> {
                                MenuOptionCard(
                                    title = "My Projects",
                                    description = "View your assigned projects",
                                    icon = Icons.Default.Folder,
                                    onClick = onNavigateToProjects
                                )
                                MenuOptionCard(
                                    title = "Profile",
                                    description = "View and edit your profile",
                                    icon = Icons.Default.Person,
                                    onClick = onNavigateToProfile
                                )
                            }
                        }
                    }
                }

                // Recent Activity
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937)
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (notificationState.notifications.isEmpty()) {
                            ActivityItemCard(
                                title = "No recent activity",
                                description = "Your recent activities will appear here",
                                timeAgo = "Just now"
                            )
                        } else {
                            notificationState.notifications.take(3).forEach { notification ->
                                ActivityItemCard(
                                    title = notification.title,
                                    description = notification.message,
                                    timeAgo = notification.timestamp.toDate().timeAgo()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = when (color) {
                        Color(0xFF2563EB) -> Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3B82F6),
                                Color(0xFF2563EB)
                            )
                        )
                        Color(0xFF10B981) -> Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF34D399),
                                Color(0xFF10B981)
                            )
                        )
                        Color(0xFFF59E0B) -> Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFBBF24),
                                Color(0xFFF59E0B)
                            )
                        )
                        Color(0xFFEF4444) -> Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF87171),
                                Color(0xFFEF4444)
                            )
                        )
                        else -> Brush.linearGradient(
                            colors = listOf(
                                color.copy(alpha = 0.8f),
                                color
                            )
                        )
                    },
                    shape = MaterialTheme.shapes.large
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9CA3AF)
        )
    }
}

@Composable
private fun ActivityItemCard(
    title: String,
    description: String,
    timeAgo: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                    )
                }
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF9CA3AF)
                    )
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280)
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// Helper extension function to format time ago
private fun Date.timeAgo(): String {
    val now = Date()
    val diff = now.time - this.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
} 