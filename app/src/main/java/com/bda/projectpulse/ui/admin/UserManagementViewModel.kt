package com.bda.projectpulse.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                userRepository.getUsers().collect { userList ->
                    _users.value = userList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load users"
            } finally {
                _isLoading.value = false
            }
        }
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

    fun createUser(email: String, password: String, name: String, role: UserRole) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val user = User(
                    uid = "", // Will be set by Firebase
                    email = email,
                    displayName = name,
                    role = role
                )
                userRepository.createUser(user, password)
                loadUsers() // Reload the user list
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val user = _users.value.find { it.uid == userId }
                if (user != null) {
                    val updatedUser = user.copy(role = newRole)
                    userRepository.updateUser(updatedUser)
                    loadUsers() // Reload the user list
                } else {
                    _error.value = "User not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update user role"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                userRepository.deleteUser(userId)
                loadUsers() // Reload the user list
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete user"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 