package com.bda.projectpulse.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.Project
import com.bda.projectpulse.models.ProjectStatus
import com.bda.projectpulse.models.User
import com.bda.projectpulse.repositories.ProjectRepository
import com.bda.projectpulse.repositories.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectViewModel(
    private val projectRepository: ProjectRepository = ProjectRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadProjects()
        loadUsers()
    }

    fun loadProjects() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.getProjects().collect { projects ->
                    _projects.value = projects
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                userRepository.getUsers().collect { users ->
                    _users.value = users
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadProjectById(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.getProjectById(projectId).collect { project ->
                    _selectedProject.value = project
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createProject(project: Project) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.createProject(project).onSuccess {
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun updateProject(projectId: String, project: Project) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.updateProject(projectId, project).onSuccess {
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.deleteProject(projectId).onSuccess {
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun updateProjectStatus(projectId: String, status: ProjectStatus) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.updateProjectStatus(projectId, status).onSuccess {
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun addTeamMember(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.addTeamMember(projectId, userId).onSuccess {
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun removeTeamMember(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                projectRepository.removeTeamMember(projectId, userId).onSuccess {
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSelectedProject() {
        _selectedProject.value = null
    }
} 