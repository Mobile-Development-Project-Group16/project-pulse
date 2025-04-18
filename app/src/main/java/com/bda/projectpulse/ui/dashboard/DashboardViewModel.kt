package com.bda.projectpulse.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.ProjectStatus
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.repositories.ProjectRepository
import com.bda.projectpulse.repositories.TaskRepository
import com.bda.projectpulse.repositories.UserRepository
import com.bda.projectpulse.repositories.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalUsers: Int = 0,
    val totalProjects: Int = 0,
    val activeProjects: Int = 0,
    val pendingTasks: Int = 0,
    val totalTeamMembers: Int = 0,
    val assignedTasks: Int = 0,
    val submittedTasks: Int = 0,
    val approvedTasks: Int = 0,
    val rejectedTasks: Int = 0
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: DashboardStats = DashboardStats(),
    val unreadNotificationCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    _currentUser.value = currentUser
                    loadStats(currentUser)
                    loadUnreadNotificationCount(currentUser.uid)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load dashboard data",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadStats(currentUser: User) {
        when (currentUser.role) {
            UserRole.ADMIN -> loadAdminStats()
            UserRole.MANAGER -> loadManagerStats(currentUser.uid)
            else -> loadTeamMemberStats(currentUser.uid)
        }
    }

    private suspend fun loadAdminStats() {
        val users = userRepository.getUsers().first()
        val projects = projectRepository.getProjects().first()
        val tasks = taskRepository.getTasks().first()

        _uiState.value = _uiState.value.copy(
            stats = DashboardStats(
                totalUsers = users.size,
                totalProjects = projects.size,
                activeProjects = projects.count { it.status == ProjectStatus.ACTIVE },
                pendingTasks = tasks.count { it.status == TaskStatus.TODO || it.status == TaskStatus.IN_PROGRESS }
            ),
            isLoading = false
        )
    }

    private suspend fun loadManagerStats(managerId: String) {
        val projects = projectRepository.getProjects().first()
        val tasks = taskRepository.getTasks().first()
        val users = userRepository.getUsers().first()

        val managerProjects = projects.filter { project -> project.ownerId == managerId }
        val projectIds = managerProjects.map { project -> project.id }
        val projectTasks = tasks.filter { task -> task.projectId in projectIds }
        val teamMembers = users.filter { user -> 
            projectIds.any { projectId -> 
                user.uid in managerProjects.find { project -> project.id == projectId }?.teamMembers.orEmpty() 
            }
        }

        _uiState.value = _uiState.value.copy(
            stats = DashboardStats(
                totalProjects = managerProjects.size,
                activeProjects = managerProjects.count { project -> project.status == ProjectStatus.ACTIVE },
                pendingTasks = projectTasks.count { task -> task.status == TaskStatus.TODO || task.status == TaskStatus.IN_PROGRESS },
                totalTeamMembers = teamMembers.size
            ),
            isLoading = false
        )
    }

    private suspend fun loadTeamMemberStats(memberId: String) {
        val tasks = taskRepository.getTasks().first()
        val projects = projectRepository.getProjects().first()

        val assignedTasks = tasks.filter { task -> memberId in task.assigneeIds }
        val userProjects = projects.filter { project -> memberId in project.teamMembers }

        _uiState.value = _uiState.value.copy(
            stats = DashboardStats(
                totalProjects = userProjects.size,
                assignedTasks = assignedTasks.size,
                pendingTasks = assignedTasks.count { task -> task.status == TaskStatus.TODO || task.status == TaskStatus.IN_PROGRESS },
                submittedTasks = assignedTasks.count { task -> task.status == TaskStatus.IN_REVIEW },
                approvedTasks = assignedTasks.count { task -> task.status == TaskStatus.APPROVED },
                rejectedTasks = assignedTasks.count { task -> task.status == TaskStatus.REJECTED }
            ),
            isLoading = false
        )
    }

    private fun loadUnreadNotificationCount(userId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.getUnreadNotificationCount(userId).collect { count ->
                    _uiState.value = _uiState.value.copy(unreadNotificationCount = count)
                }
            } catch (e: Exception) {
                // Handle error silently for notification count
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUser.value = userRepository.getCurrentUser()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load user data",
                    isLoading = false
                )
            }
        }
    }

    fun refreshDashboard() {
        loadDashboardData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 