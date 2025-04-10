package com.bda.projectpulse.ui.projects

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.*
import com.bda.projectpulse.repositories.ProjectRepository
import com.bda.projectpulse.repositories.TaskRepository
import com.bda.projectpulse.repositories.UserRepository
import com.bda.projectpulse.repositories.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.supervisorScope

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects = _projects.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject = _selectedProject.asStateFlow()

    private val _projectTasks = MutableStateFlow<List<Task>>(emptyList())
    val projectTasks = _projectTasks.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow<Int>(0)
    val unreadNotificationsCount = _unreadNotificationsCount.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers = _filteredUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        println("ProjectViewModel: Initializing")
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            println("ProjectViewModel: Current user ID: $currentUserId")
            if (currentUserId != null) {
                loadProjects()
                loadUsers()
                loadCurrentUser()
                loadUnreadNotificationsCount()
            }
        }
    }

    fun loadProjects() {
        println("ProjectViewModel: Loading projects")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val currentUser = userRepository.getCurrentUser()
                println("ProjectViewModel: Current user for projects: ${currentUser?.displayName}, role: ${currentUser?.role}")
                
                // Use a supervisor scope to handle errors without cancelling the parent coroutine
                supervisorScope {
                    projectRepository.getProjects(currentUser?.role).collect { projectList ->
                        println("ProjectViewModel: Projects loaded: ${projectList.size}")
                        _projects.value = projectList
                        
                        // Make sure to set isLoading to false after setting the projects value
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
                println("ProjectViewModel: Error loading projects: ${e.message}")
            }
        }
    }

    fun loadProjectById(projectId: String) {
        println("ProjectViewModel: Loading project by ID: $projectId")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.getProjectById(projectId).collect { project ->
                    println("ProjectViewModel: Project loaded: ${project?.name}")
                    _selectedProject.value = project
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProjectTasks(projectId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                taskRepository.getProjectTasks(projectId, currentUser.role, currentUser.uid)
                    .collect { tasks ->
                        _projectTasks.value = tasks
                    }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUsers() {
        println("ProjectViewModel: Loading users")
        viewModelScope.launch {
            try {
                userRepository.getUsers().collect { userList ->
                    println("ProjectViewModel: Users loaded: ${userList.size}")
                    _users.value = userList
                    updateFilteredUsers()
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadCurrentUser() {
        println("ProjectViewModel: Loading current user")
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                println("ProjectViewModel: Current user loaded: ${user?.uid}")
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredUsers()
    }

    private fun updateFilteredUsers() {
        val query = _searchQuery.value.lowercase()
        _filteredUsers.value = if (query.isBlank()) {
            _users.value
        } else {
            _users.value.filter { user ->
                user.displayName.lowercase().contains(query) ||
                user.email.lowercase().contains(query)
            }
        }
    }

    fun createProject(project: Project) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val currentUser = _currentUser.value
                if (currentUser == null) {
                    _error.value = "User is not authenticated"
                    return@launch
                }
                
                // Check if user has permission to create projects
                if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.MANAGER) {
                    _error.value = "Only administrators and managers can create projects"
                    return@launch
                }
                
                projectRepository.createProject(project, currentUser.role.name)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create project"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.updateProject(project.id, project)
                loadProjectById(project.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update project"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val currentUser = _currentUser.value
                if (currentUser == null) {
                    _error.value = "User is not authenticated"
                    return@launch
                }
                
                // Get the project to verify ownership
                val project = projectRepository.getProjectById(projectId).firstOrNull()
                if (project == null) {
                    _error.value = "Project not found"
                    return@launch
                }
                
                // Check if user has permission to delete the project
                val canDeleteProject = currentUser.role == UserRole.ADMIN || project.ownerId == currentUser.uid
                if (!canDeleteProject) {
                    _error.value = "You don't have permission to delete this project"
                    return@launch
                }
                
                // Delete the project
                projectRepository.deleteProject(projectId)
                    .onSuccess {
                        // Reload projects list after deletion
                        loadProjects()
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to delete project"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserName(userId: String): String {
        return _users.value.find { it.uid == userId }?.displayName ?: "Unknown User"
    }

    fun getUserEmail(userId: String): String {
        return _users.value.find { it.uid == userId }?.email ?: ""
    }

    fun updateError(message: String?) {
        _error.value = message
    }
    
    fun addTeamMember(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Check if user has permission to manage team members
                val currentUser = _currentUser.value
                if (currentUser == null) {
                    _error.value = "User is not authenticated"
                    return@launch
                }
                
                val project = _selectedProject.value ?: return@launch
                
                // Only admins, managers, or project owner can add team members
                val canManageTeam = currentUser.role == UserRole.ADMIN || 
                                   currentUser.role == UserRole.MANAGER ||
                                   project.ownerId == currentUser.uid
                
                if (!canManageTeam) {
                    _error.value = "You don't have permission to manage team members"
                    return@launch
                }
                
                // Only add if not already a team member
                if (!project.teamMembers.contains(userId)) {
                    val updatedProject = project.copy(
                        teamMembers = project.teamMembers + userId
                    )
                    projectRepository.updateProject(projectId, updatedProject)
                    _selectedProject.value = updatedProject
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add team member"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeTeamMember(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Check if user has permission to manage team members
                val currentUser = _currentUser.value
                if (currentUser == null) {
                    _error.value = "User is not authenticated"
                    return@launch
                }
                
                val project = _selectedProject.value ?: return@launch
                
                // Only admins, managers, or project owner can remove team members
                val canManageTeam = currentUser.role == UserRole.ADMIN || 
                                   currentUser.role == UserRole.MANAGER ||
                                   project.ownerId == currentUser.uid
                
                if (!canManageTeam) {
                    _error.value = "You don't have permission to manage team members"
                    return@launch
                }
                
                val updatedProject = project.copy(
                    teamMembers = project.teamMembers - userId
                )
                projectRepository.updateProject(projectId, updatedProject)
                _selectedProject.value = updatedProject
                
                // Also unassign this user from any tasks in this project
                unassignUserFromTasks(projectId, userId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove team member"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun unassignUserFromTasks(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }
                
                taskRepository.getProjectTasks(projectId, currentUser.role, currentUser.uid)
                    .collect { tasks ->
                        tasks.forEach { task ->
                            if (task.assigneeIds.contains(userId)) {
                                val updatedTask = task.copy(
                                    assigneeIds = task.assigneeIds - userId
                                )
                                taskRepository.updateTask(updatedTask)
                            }
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to unassign user from tasks"
            }
        }
    }

    fun loadUnreadNotificationsCount() {
        viewModelScope.launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                notificationRepository.getUnreadNotificationCount(currentUserId).collect { count ->
                    _unreadNotificationsCount.value = count
                }
            } catch (e: Exception) {
                println("Error loading unread notifications count: ${e.message}")
            }
        }
    }
} 