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
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable

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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUser.value = userRepository.getCurrentUser()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadTaskById(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                taskRepository.getTaskById(taskId)
                    .catch { e -> _error.value = e.message }
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
            _error.value = null
            try {
                taskRepository.getTasks()
                    .catch { e -> _error.value = e.message }
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
            _error.value = null
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                taskRepository.getProjectTasks(projectId, currentUser.role, currentUser.uid)
                    .catch { e -> _error.value = e.message }
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
                _isLoading.value = true
                userRepository.getUsers()
                    .catch { e -> _error.value = e.message }
                    .collect { userList ->
                        _users.value = userList
                    }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProjectTeamMembers(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // First, get the project details to get team member IDs
                val projectFlow = projectRepository.getProjectById(projectId)
                val project = projectFlow.firstOrNull()
                
                if (project == null) {
                    _error.value = "Project not found"
                    _isLoading.value = false
                    return@launch
                }
                
                // Get all users and filter to only include team members
                userRepository.getUsers()
                    .catch { e -> _error.value = e.message }
                    .collect { allUsers ->
                        val teamMembers = allUsers.filter { user ->
                            project.teamMembers.contains(user.uid) || project.ownerId == user.uid
                        }
                        _projectTeamMembers.value = teamMembers
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                println("Starting saveTask coroutine for task: ${task.title}")

                withContext(NonCancellable + Dispatchers.IO) {
                    val currentUser = userRepository.getCurrentUser()
                    
                    if (currentUser == null) {
                        _error.value = "User not authenticated"
                        println("Error: User not authenticated")
                        return@withContext
                    }
                    
                    // Check if user has permission to manage tasks
                    if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.MANAGER) {
                        _error.value = "Only administrators and managers can manage tasks"
                        println("Error: User does not have permission to manage tasks")
                        return@withContext
                    }
                    
                    // Create or update task
                    if (task.id == "new" || task.id.isEmpty()) {
                        println("Creating new task with title: ${task.title}")
                        val taskWithCreator = task.copy(
                            id = "", // Let Firestore generate the ID
                            createdBy = currentUser.uid,
                            createdAt = Timestamp.now(),
                            updatedAt = Timestamp.now()
                        )
                        taskRepository.createTask(taskWithCreator)
                        println("Task created successfully")
                        
                        // Refresh the task list after creation
                        loadTasksByProjectId(task.projectId)
                    } else {
                        // For task updates, check if the user is the creator or an admin
                        val existingTask = taskRepository.getTaskById(task.id).first()
                        if (existingTask != null && existingTask.createdBy != currentUser.uid && currentUser.role != UserRole.ADMIN) {
                            _error.value = "You can only edit tasks you created"
                            println("Error: User cannot edit task they did not create")
                            return@withContext
                        }
                        
                        println("Updating task with ID: ${task.id}")
                        val updatedTask = task.copy(updatedAt = Timestamp.now())
                        taskRepository.updateTask(updatedTask)
                        println("Task updated successfully")
                        
                        // Refresh the task list and selected task after update
                        loadTasksByProjectId(task.projectId)
                        loadTaskById(task.id)
                    }
                }
            } catch (e: Exception) {
                println("Error in saveTask: ${e.message}")
                println("Stack trace: ${e.stackTrace.joinToString("\n")}")
                _error.value = "Failed to save task: ${e.message}"
            } finally {
                _isLoading.value = false
                println("Finished saveTask coroutine for task: ${task.title}")
            }
        }
    }

    fun updateTaskStatus(status: TaskStatus) {
        viewModelScope.launch {
            try {
                _selectedTask.value?.let { task ->
                    taskRepository.updateTaskStatus(task.id, status)
                    loadTaskById(task.id) // Refresh the task
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                taskRepository.deleteTask(taskId)
            } catch (e: Exception) {
                _error.value = e.message
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
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                val taskWithCreator = task.copy(
                    createdBy = currentUser.uid,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                taskRepository.createTask(taskWithCreator)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getUserName(userId: String): String {
        return _users.value.find { it.uid == userId }?.displayName ?: "Unknown User"
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

    fun updateTaskPriority(priority: TaskPriority) {
        viewModelScope.launch {
            try {
                _selectedTask.value?.let { task ->
                    taskRepository.updateTaskPriority(task.id, priority)
                    loadTaskById(task.id) // Refresh the task
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun submitTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = _selectedTask.value ?: return@launch
                val currentUser = userRepository.getCurrentUser() ?: return@launch

                // Check if the user is assigned to this task
                if (!task.assigneeIds.contains(currentUser.uid)) {
                    _error.value = "You must be assigned to this task to submit it"
                    return@launch
                }

                val updatedTask = task.copy(
                    status = TaskStatus.IN_REVIEW,
                    updatedAt = Timestamp.now()
                )
                updateTask(updatedTask)
                _selectedTask.value = updatedTask
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun approveTask(taskId: String) {
        viewModelScope.launch {
            try {
                val task = _selectedTask.value ?: return@launch
                val currentUser = userRepository.getCurrentUser() ?: return@launch

                // Check if user has permission to approve tasks
                if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.MANAGER) {
                    _error.value = "Only administrators and managers can approve tasks"
                    return@launch
                }

                val updatedTask = task.copy(
                    status = TaskStatus.APPROVED,
                    updatedAt = Timestamp.now()
                )
                updateTask(updatedTask)
                _selectedTask.value = updatedTask
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun rejectTask(taskId: String, rejectionComment: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val task = _selectedTask.value ?: throw Exception("Task not found")
                val updatedTask = task.copy(
                    status = TaskStatus.IN_PROGRESS,
                    rejectionComment = rejectionComment
                )
                
                taskRepository.updateTask(updatedTask)
                _selectedTask.value = updatedTask
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAssignedTasks(): Flow<List<Task>> {
        return tasks.map { taskList ->
            taskList.filter { task ->
                val currentUser = userRepository.getCurrentUser()
                currentUser?.let { user ->
                    task.assigneeIds.contains(user.uid)
                } ?: false
            }
        }
    }
} 