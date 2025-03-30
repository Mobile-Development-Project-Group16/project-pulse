package com.bda.projectpulse.ui.tasks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.TaskPriority
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.repositories.TaskRepository
import com.bda.projectpulse.repositories.UserRepository
import com.bda.projectpulse.repositories.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask = _selectedTask.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _projectTeamMembers = MutableStateFlow<List<User>>(emptyList())
    val projectTeamMembers = _projectTeamMembers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val error = mutableStateOf<String?>(null)

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadTaskById(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                taskRepository.getTaskById(taskId)
                    .catch { e -> error.value = e.message }
                    .collect { task ->
                        _selectedTask.value = task
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                taskRepository.getTasks()
                    .catch { e -> error.value = e.message }
                    .collect { taskList ->
                        _tasks.value = taskList
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTasksByProjectId(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    error.value = "User not authenticated"
                    return@launch
                }
                
                taskRepository.getProjectTasks(projectId, currentUser.role, currentUser.uid)
                    .catch { e -> error.value = e.message }
                    .collect { tasks ->
                        _tasks.value = tasks
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                userRepository.getUsers()
                    .catch { e -> error.value = e.message }
                    .collect { userList ->
                        _users.value = userList
                    }
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun loadProjectTeamMembers(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                // First, get the project details to get team member IDs
                val projectFlow = projectRepository.getProjectById(projectId)
                val project = projectFlow.firstOrNull()
                
                if (project == null) {
                    error.value = "Project not found"
                    _isLoading.value = false
                    return@launch
                }
                
                // Get all users and filter to only include team members
                userRepository.getUsers()
                    .catch { e -> error.value = e.message }
                    .collect { allUsers ->
                        val teamMembers = allUsers.filter { user ->
                            project.teamMembers.contains(user.uid) || project.ownerId == user.uid
                        }
                        _projectTeamMembers.value = teamMembers
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    error.value = "User not authenticated"
                    return@launch
                }
                
                // Check if user has permission to manage tasks
                if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.MANAGER) {
                    error.value = "Only administrators and managers can manage tasks"
                    return@launch
                }
                
                if (task.id.isEmpty()) {
                    taskRepository.createTask(task, currentUser.uid)
                } else {
                    // For task updates, check if the user is the creator or an admin
                    val existingTask = taskRepository.getTaskById(task.id).first()
                    if (existingTask != null && existingTask.createdBy != currentUser.uid && currentUser.role != UserRole.ADMIN) {
                        error.value = "You can only edit tasks you created"
                        return@launch
                    }
                    
                    taskRepository.updateTask(task)
                }
                loadTasksByProjectId(task.projectId)
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                taskRepository.updateTaskStatus(taskId, status)
                    .onSuccess {
                        loadTaskById(taskId)
                    }
                    .onFailure { e ->
                        error.value = e.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(taskId: String, projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    error.value = "User not authenticated"
                    return@launch
                }
                
                // Check if user has permission to delete tasks
                if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.MANAGER) {
                    error.value = "Only administrators and managers can delete tasks"
                    return@launch
                }
                
                // For task deletion, check if the user is the creator or an admin
                val existingTask = taskRepository.getTaskById(taskId).first()
                if (existingTask != null && existingTask.createdBy != currentUser.uid && currentUser.role != UserRole.ADMIN) {
                    error.value = "You can only delete tasks you created"
                    return@launch
                }
                
                taskRepository.deleteTask(taskId)
                    .onSuccess {
                        loadTasksByProjectId(projectId)
                    }
                    .onFailure { e ->
                        error.value = e.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getTaskById(taskId: String): Flow<Task?> {
        return taskRepository.getTaskById(taskId)
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    error.value = "User not authenticated"
                    return@launch
                }
                
                taskRepository.createTask(task, currentUser.uid)
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                error.value = null
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

    fun clearError() {
        error.value = null
    }

    fun getUserName(userId: String): String {
        // First check in project team members
        val teamMember = _projectTeamMembers.value.find { it.uid == userId }
        if (teamMember != null) {
            return teamMember.displayName
        }
        
        // Then check in all users
        val user = _users.value.find { it.uid == userId }
        return user?.displayName ?: "Unknown User"
    }
    
    fun getUserEmail(userId: String): String {
        return _users.value.find { it.uid == userId }?.email ?: ""
    }
    
    fun getAssigneeNames(assigneeIds: List<String>): String {
        if (assigneeIds.isEmpty()) return "Unassigned"
        
        return assigneeIds.mapNotNull { userId ->
            _users.value.find { it.uid == userId }?.displayName
        }.joinToString(", ")
    }
    
    fun addAssignee(userId: String) {
        viewModelScope.launch {
            val task = _selectedTask.value ?: return@launch
            if (!task.assigneeIds.contains(userId)) {
                val updatedTask = task.copy(
                    assigneeIds = task.assigneeIds + userId
                )
                updateTask(updatedTask)
                _selectedTask.value = updatedTask
            }
        }
    }
    
    fun removeAssignee(userId: String) {
        viewModelScope.launch {
            val task = _selectedTask.value ?: return@launch
            if (task.assigneeIds.contains(userId)) {
                val updatedTask = task.copy(
                    assigneeIds = task.assigneeIds - userId
                )
                updateTask(updatedTask)
                _selectedTask.value = updatedTask
            }
        }
    }

    fun updateTaskPriority(taskId: String, priority: TaskPriority) {
        viewModelScope.launch {
            _isLoading.value = true
            error.value = null
            try {
                taskRepository.updateTaskPriority(taskId, priority)
                    .onSuccess {
                        loadTaskById(taskId)
                    }
                    .onFailure { e ->
                        error.value = e.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
} 