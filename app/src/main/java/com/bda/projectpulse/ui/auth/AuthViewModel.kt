package com.bda.projectpulse.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.auth.FirebaseAuthManager
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.TEAM_MEMBER,
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authManager = FirebaseAuthManager.getInstance()
    private val TAG = "AuthViewModel"
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _availableRoles = MutableStateFlow<List<UserRole>>(emptyList())
    val availableRoles: StateFlow<List<UserRole>> = _availableRoles.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                authManager.getCurrentUser()?.let { firebaseUser ->
                    val user = authManager.getUserData(firebaseUser.uid)
                    _authState.value = AuthState.Authenticated(user)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user data", e)
            }
        }
        checkAdminUser()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
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

    private fun checkAdminUser() {
        viewModelScope.launch {
            try {
                val hasAdmin = authManager.hasAdminUser()
                Log.d(TAG, "Checking for admin user. Has admin: $hasAdmin")
                _availableRoles.value = if (hasAdmin) {
                    Log.d(TAG, "Admin exists, limiting available roles")
                    listOf(UserRole.PROJECT_MANAGER, UserRole.TEAM_MEMBER)
                } else {
                    Log.d(TAG, "No admin exists, showing all roles")
                    UserRole.values().toList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for admin user", e)
                _availableRoles.value = listOf(UserRole.PROJECT_MANAGER, UserRole.TEAM_MEMBER)
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

    fun signIn() {
        viewModelScope.launch {
            try {
                _formState.update { it.copy(isLoading = true, error = null) }
                val result = authManager.signIn(
                    _formState.value.email,
                    _formState.value.password
                )
                result.fold(
                    onSuccess = { user ->
                        _authState.value = AuthState.Authenticated(user)
                    },
                    onFailure = { e ->
                        _formState.update { it.copy(
                            isLoading = false,
                            error = e.message ?: "Authentication failed"
                        ) }
                        _authState.value = AuthState.Error(e.message ?: "Authentication failed")
                    }
                )
            } catch (e: Exception) {
                _formState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Authentication failed"
                ) }
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            try {
                _formState.update { it.copy(isLoading = true, error = null) }
                val result = authManager.signUp(
                    _formState.value.email,
                    _formState.value.password,
                    _formState.value.displayName,
                    _formState.value.role
                )
                result.fold(
                    onSuccess = { user ->
                        _authState.value = AuthState.Authenticated(user)
                    },
                    onFailure = { e ->
                        _formState.update { it.copy(
                            isLoading = false,
                            error = e.message ?: "Registration failed"
                        ) }
                        _authState.value = AuthState.Error(e.message ?: "Registration failed")
                    }
                )
            } catch (e: Exception) {
                _formState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Registration failed"
                ) }
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.Unauthenticated
        _formState.value = AuthFormState()
    }
} 