package com.bda.projectpulse.ui.tasks

import android.util.Log
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
import kotlinx.coroutines.delay

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
        println("TaskViewModel initialized")
        // Load current user first, then load users
        viewModelScope.launch {
            try {
                loadCurrentUser()
                // Wait a bit to ensure current user is loaded
                delay(500)
                loadUsers()
            } catch (e: Exception) {
                println("Error in init block: ${e.message}")
                _error.value = "Initialization error: ${e.message}"
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                println("Starting to load current user")
                
                // Use supervisorScope to prevent cancellation from affecting child coroutines
                supervisorScope {
                    try {
                        // Use withContext to ensure we're on the IO dispatcher
                        withContext(Dispatchers.IO + NonCancellable) {
                            val user = userRepository.getCurrentUser()
                            if (user != null) {
                                println("Successfully loaded current user: ${user.displayName}")
                                _currentUser.value = user
                                _error.value = null
                            } else {
                                println("Current user is null")
                                _error.value = "User not authenticated"
                            }
                        }
                    } catch (e: Exception) {
                        println("Error in supervisorScope: ${e.message}")
                        _error.value = "Error loading current user: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                println("Unexpected error in loadCurrentUser: ${e.message}")
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
                println("Finished loading current user")
            }
        }
    }

    fun loadTaskById(taskId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                println("Starting to load task with ID: $taskId")
                
                // Use supervisorScope to prevent cancellation from affecting child coroutines
                supervisorScope {
                    try {
                        // First check if the user is authenticated
                        val currentUser = userRepository.getCurrentUser()
                        if (currentUser == null) {
                            println("Error: User not authenticated")
                            _error.value = "User not authenticated"
                            _selectedTask.value = null
                            return@supervisorScope
                        }
                        
                        // Use withContext to ensure we're on the IO dispatcher
                        withContext(Dispatchers.IO + NonCancellable) {
                            taskRepository.getTaskById(taskId)
                                .catch { e ->
                                    println("Error loading task: ${e.message}")
                                    _error.value = "Failed to load task: ${e.message}"
                                    _selectedTask.value = null
                                }
                                .collect { task ->
                                    if (task != null) {
                                        println("Successfully loaded task: ${task.title}")
                                        _selectedTask.value = task
                                        _error.value = null
                                    } else {
                                        println("Task not found with ID: $taskId")
                                        _error.value = "Task not found"
                                        _selectedTask.value = null
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        println("Error in supervisorScope: ${e.message}")
                        _error.value = "Error loading task: ${e.message}"
                        _selectedTask.value = null
                    }
                }
            } catch (e: Exception) {
                println("Unexpected error in loadTaskById: ${e.message}")
                _error.value = "Unexpected error: ${e.message}"
                _selectedTask.value = null
            } finally {
                _isLoading.value = false
                println("Finished loading task with ID: $taskId")
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
                _error.value = null
                println("Starting to load users")
                
                // Use supervisorScope to prevent cancellation from affecting child coroutines
                supervisorScope {
                    try {
                        // Use withContext to ensure we're on the IO dispatcher
                        withContext(Dispatchers.IO + NonCancellable) {
                            userRepository.getUsers()
                                .catch { e ->
                                    println("Error loading users: ${e.message}")
                                    _error.value = "Failed to load users: ${e.message}"
                                }
                                .collect { userList ->
                                    println("Successfully loaded ${userList.size} users")
                                    _users.value = userList
                                    _error.value = null
                                }
                        }
                    } catch (e: Exception) {
                        println("Error in supervisorScope: ${e.message}")
                        _error.value = "Error loading users: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                println("Unexpected error in loadUsers: ${e.message}")
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
                println("Finished loading users")
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
                _isLoading.value = true
                _error.value = null
                
                val task = _selectedTask.value
                if (task != null) {
                    Log.d("TaskViewModel", "Updating task status to $status for task ${task.id}")
                    
                    // Use NonCancellable context to ensure completion even if the coroutine is cancelled
                    withContext(NonCancellable) {
                        val result = taskRepository.updateTaskStatus(task.id, status)
                        
                        if (result.isSuccess) {
                            Log.d("TaskViewModel", "Successfully updated task status to $status")
                        } else {
                            val exception = result.exceptionOrNull()
                            Log.e("TaskViewModel", "Failed to update task status: ${exception?.message}", exception)
                            throw exception ?: Exception("Unknown error updating task status")
                        }
                        
                        // Refresh the task
                        loadTaskById(task.id)
                    }
                } else {
                    Log.e("TaskViewModel", "Cannot update status - no task selected")
                    _error.value = "No task selected"
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error updating task status", e)
                _error.value = "Failed to update task status: ${e.message}"
            } finally {
                _isLoading.value = false
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

                // Use updateTaskStatus instead of updateTask to trigger notifications
                taskRepository.updateTaskStatus(task.id, TaskStatus.IN_REVIEW)
                
                // Refresh the task to get the latest version
                loadTaskById(task.id)
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

                // Use updateTaskStatus instead of updateTask to trigger notifications
                taskRepository.updateTaskStatus(task.id, TaskStatus.APPROVED)
                
                // Refresh the task to get the latest version
                loadTaskById(task.id)
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
                
                // First update the task status to trigger notifications
                taskRepository.updateTaskStatus(task.id, TaskStatus.REJECTED)
                
                // Then update the rejection comment separately
                val updatedTask = task.copy(
                    status = TaskStatus.REJECTED,
                    rejectionComment = rejectionComment
                )
                taskRepository.updateTask(updatedTask)
                
                // Refresh the task
                loadTaskById(task.id)
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

    fun submitTaskWithSubmission(taskId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("TaskViewModel", "Starting combined task submission process for task $taskId")
                
                // Use NonCancellable context to ensure completion even if the coroutine is cancelled
                withContext(NonCancellable) {
                    // Wait briefly to ensure the task update completes
                    kotlinx.coroutines.delay(500)
                    
                    // Update the task status to IN_REVIEW
                    Log.d("TaskViewModel", "Changing task status to IN_REVIEW")
                    val result = taskRepository.updateTaskStatus(taskId, TaskStatus.IN_REVIEW)
                    
                    if (result.isSuccess) {
                        Log.d("TaskViewModel", "Successfully updated task status to IN_REVIEW")
                    } else {
                        val exception = result.exceptionOrNull()
                        Log.e("TaskViewModel", "Failed to update task status: ${exception?.message}", exception)
                        throw exception ?: Exception("Unknown error updating task status")
                    }
                    
                    // Wait a bit to ensure status change completes and notifications are processed
                    kotlinx.coroutines.delay(1000)
                    
                    // Refresh the task
                    loadTaskById(taskId)
                    
                    Log.d("TaskViewModel", "Task submission completed successfully")
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error in task submission process", e)
                _error.value = "Failed to submit task: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 