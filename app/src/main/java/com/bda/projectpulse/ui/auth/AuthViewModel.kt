package com.bda.projectpulse.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.auth.FirebaseAuthManager
import com.bda.projectpulse.data.repositories.AuthRepository
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.USER,
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val authManager = FirebaseAuthManager.getInstance()
    private val TAG = "AuthViewModel"
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _availableRoles = MutableStateFlow<List<UserRole>>(emptyList())
    val availableRoles: StateFlow<List<UserRole>> = _availableRoles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            supervisorScope {
                try {
                    authManager.getCurrentUser()?.let { firebaseUser ->
                        val user = authManager.getUserData(firebaseUser.uid)
                        _authState.value = AuthState.Authenticated(user)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting user data", e)
                    _authState.value = AuthState.Error(e.message ?: "Failed to get user data")
                }
            }
        }
        checkAdminUser()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            supervisorScope {
                try {
                    val currentUser = authManager.getCurrentUser()
                    if (currentUser != null) {
                        val user = authManager.getUserData(currentUser.uid)
                        _authState.value = AuthState.Authenticated(user)
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking auth state", e)
                    _authState.value = AuthState.Error(e.message ?: "Failed to get user data")
                }
            }
        }
    }

    private fun checkAdminUser() {
        viewModelScope.launch {
            supervisorScope {
                try {
                    val hasAdmin = authManager.hasAdminUser()
                    Log.d(TAG, "Checking for admin user. Has admin: $hasAdmin")
                    _availableRoles.value = if (hasAdmin) {
                        Log.d(TAG, "Admin exists, limiting available roles")
                        listOf(UserRole.MANAGER, UserRole.USER)
                    } else {
                        Log.d(TAG, "No admin exists, showing all roles")
                        UserRole.values().toList()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking for admin user", e)
                    _availableRoles.value = listOf(UserRole.MANAGER, UserRole.USER)
                }
            }
        }
    }

    fun updateEmail(email: String) {
        _formState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _formState.update { it.copy(password = password) }
    }

    fun updateDisplayName(name: String) {
        _formState.update { it.copy(displayName = name) }
    }

    fun updateRole(role: UserRole) {
        Log.d(TAG, "Updating selected role to: $role")
        _formState.update { it.copy(role = role) }
    }

    fun toggleAuthMode() {
        _formState.update { it.copy(
            isRegistering = !it.isRegistering,
            error = null
        ) }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            supervisorScope {
                try {
                    _isLoading.value = true
                    _error.value = null
                    authRepository.login(email, password)
                    checkAuthState()
                    onSuccess()
                } catch (e: Exception) {
                    _error.value = e.message ?: "Login failed"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            supervisorScope {
                try {
                    _isLoading.value = true
                    _error.value = null
                    authRepository.register(email, password, name)
                    checkAuthState()
                    onSuccess()
                } catch (e: Exception) {
                    _error.value = e.message ?: "Registration failed"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.Unauthenticated
        _formState.value = AuthFormState()
    }
} 