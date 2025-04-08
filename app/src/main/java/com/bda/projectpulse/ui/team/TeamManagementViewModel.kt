package com.bda.projectpulse.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.data.repository.TeamRepository
import com.bda.projectpulse.data.repository.UserRepository
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamManagementViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _teamMembers = MutableStateFlow<List<User>>(emptyList())
    val teamMembers: StateFlow<List<User>> = _teamMembers.asStateFlow()

    private val _availableUsers = MutableStateFlow<List<User>>(emptyList())
    val availableUsers: StateFlow<List<User>> = _availableUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadTeamMembers(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _teamMembers.value = teamRepository.getTeamMembers(projectId)
                _error.value = null
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
            try {
                _availableUsers.value = userRepository.getAvailableUsers()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load available users"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTeamMember(projectId: String, userId: String, role: UserRole) {
        viewModelScope.launch {
            try {
                teamRepository.addTeamMember(projectId, userId, role)
                loadTeamMembers(projectId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add team member"
            }
        }
    }

    fun removeTeamMember(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                teamRepository.removeTeamMember(projectId, userId)
                loadTeamMembers(projectId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove team member"
            }
        }
    }

    fun updateTeamMemberRole(projectId: String, userId: String, newRole: UserRole) {
        viewModelScope.launch {
            try {
                teamRepository.updateTeamMemberRole(projectId, userId, newRole)
                loadTeamMembers(projectId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update team member role"
            }
        }
    }
} 