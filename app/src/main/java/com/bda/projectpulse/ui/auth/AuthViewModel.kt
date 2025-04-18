package com.bda.projectpulse.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.auth.FirebaseAuthManager
import com.bda.projectpulse.data.repositories.AuthRepository
import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole
import com.bda.projectpulse.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val authRepository: AuthRepository,
    private val authManager: FirebaseAuthManager,
    private val authService: AuthService
) : ViewModel() {
    private val TAG = "AuthViewModel"
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _availableRoles = MutableStateFlow<List<UserRole>>(emptyList())
    val availableRoles: StateFlow<List<UserRole>> = _availableRoles.asStateFlow()

    private var authStateJob: Job? = null
    private var adminCheckJob: Job? = null

    init {
        checkAuthState()
        checkAdminUser()
    }

    private fun checkAuthState() {
        authStateJob?.cancel()
        authStateJob = viewModelScope.launch {
            try {
                val currentUser = authManager.getCurrentUser()
                if (currentUser != null) {
                    try {
                        val user = authManager.getUserData(currentUser.uid)
                        _authState.value = AuthState.Authenticated(user)
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            Log.e(TAG, "Error getting user data", e)
                            _authState.value = AuthState.Error(e.message ?: "Failed to get user data")
                        }
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(TAG, "Error checking auth state", e)
                    _authState.value = AuthState.Error(e.message ?: "Failed to check auth state")
                }
            }
        }
    }

    private fun checkAdminUser() {
        adminCheckJob?.cancel()
        adminCheckJob = viewModelScope.launch {
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
                if (e !is CancellationException) {
                    Log.e(TAG, "Error checking for admin user", e)
                    _availableRoles.value = listOf(UserRole.MANAGER, UserRole.USER)
                }
            }
        }
    }

    fun updateEmail(email: String) {
        _formState.update { it.copy(email = email, error = null) }
    }

    fun updatePassword(password: String) {
        _formState.update { it.copy(password = password, error = null) }
    }

    fun updateDisplayName(name: String) {
        _formState.update { it.copy(displayName = name, error = null) }
    }

    fun updateRole(role: UserRole) {
        Log.d(TAG, "Updating selected role to: $role")
        _formState.update { it.copy(role = role, error = null) }
    }

    fun toggleAuthMode() {
        _formState.update { it.copy(
            isRegistering = !it.isRegistering,
            error = null
        ) }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _formState.value = _formState.value.copy(
                error = "Please fill in all fields",
                isLoading = false
            )
            return
        }

        _formState.value = _formState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val user = authService.login(email, password)
                _authState.value = AuthState.Authenticated(user)
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when (e.message) {
                    "invalid-email" -> "Invalid email address"
                    "user-disabled" -> "This account has been disabled"
                    "user-not-found" -> "No account found with this email"
                    "wrong-password" -> "Incorrect password"
                    else -> "Login failed: ${e.message}"
                }
                _formState.value = _formState.value.copy(
                    error = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _formState.value = _formState.value.copy(
                error = "Please fill in all fields",
                isLoading = false
            )
            return
        }

        if (password.length < 6) {
            _formState.value = _formState.value.copy(
                error = "Password must be at least 6 characters long",
                isLoading = false
            )
            return
        }

        _formState.value = _formState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val user = authService.register(email, password, name, _formState.value.role)
                _authState.value = AuthState.Authenticated(user)
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when (e.message) {
                    "email-already-in-use" -> "An account with this email already exists"
                    "invalid-email" -> "Invalid email address"
                    "operation-not-allowed" -> "Registration is currently disabled"
                    "weak-password" -> "Password is too weak"
                    else -> "Registration failed: ${e.message}"
                }
                _formState.value = _formState.value.copy(
                    error = errorMessage,
                    isLoading = false
                )
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.Unauthenticated
        _formState.value = AuthFormState()
    }

    override fun onCleared() {
        super.onCleared()
        authStateJob?.cancel()
        adminCheckJob?.cancel()
    }
} 