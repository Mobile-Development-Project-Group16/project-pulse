package com.bda.projectpulse.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.repositories.UserRepository
import com.bda.projectpulse.repositories.ProjectRepository
import com.bda.projectpulse.data.repository.TeamRepository
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _teamMembers = MutableStateFlow<List<User>>(emptyList())
    val teamMembers: StateFlow<List<User>> = _teamMembers.asStateFlow()

    private val _availableUsers = MutableStateFlow<List<User>>(emptyList())
    val availableUsers: StateFlow<List<User>> = _availableUsers.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUser.value = userRepository.getCurrentUser()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load current user"
            }
        }
    }

    fun loadTeamMembers(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val project = projectRepository.getProjectById(projectId).first()
                val members = project?.teamMembers?.mapNotNull { userId ->
                    try {
                        userRepository.getUserById(userId).first()
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                _teamMembers.value = members
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load team members"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAvailableUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val users = userRepository.getUsers().first()
                _availableUsers.value = users
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load available users"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTeamMember(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                teamRepository.addTeamMember(projectId, userId, UserRole.USER)
                loadTeamMembers(projectId)
                loadAvailableUsers()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add team member"
            }
        }
    }

    fun removeTeamMember(projectId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                projectRepository.removeTeamMember(projectId, userId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove team member"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTeamMemberRole(projectId: String, userId: String, newRole: UserRole) {
        viewModelScope.launch {
            try {
                teamRepository.addTeamMember(projectId, userId, newRole)
                loadTeamMembers(projectId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update team member role"
            }
        }
    }
} 