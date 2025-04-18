package com.bda.projectpulse.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.User
import com.bda.projectpulse.repositories.UserRepository
import com.bda.projectpulse.repositories.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _teamMembers = MutableStateFlow<List<User>>(emptyList())
    val teamMembers: StateFlow<List<User>> = _teamMembers.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    init {
        loadTeamMembers()
    }

    fun loadTeamMembers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _teamMembers.value = teamRepository.getTeamMembers()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _searchResults.value = userRepository.searchUsersByEmail(query)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTeamMember(user: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                teamRepository.addTeamMember(user)
                loadTeamMembers() // Reload team members after adding
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeTeamMember(user: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                teamRepository.removeTeamMember(user)
                loadTeamMembers() // Reload team members after removing
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 