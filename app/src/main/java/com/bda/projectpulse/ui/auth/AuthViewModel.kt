package com.bda.projectpulse.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.auth.FirebaseAuthManager
import com.bda.projectpulse.models.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authManager = FirebaseAuthManager.getInstance()
    private val TAG = "AuthViewModel"
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _availableRoles = MutableStateFlow<List<UserRole>>(emptyList())
    val availableRoles: StateFlow<List<UserRole>> = _availableRoles.asStateFlow()

    init {
        checkAuthState()
        checkAdminUser()
    }

    private fun checkAuthState() {
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val user = authManager.getUserData(currentUser.uid)
                    _authState.value = AuthState.Authenticated(user)
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking auth state", e)
                    _authState.value = AuthState.Error(e.message ?: "Failed to get user data")
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
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

    fun toggleRegisterMode() {
        _formState.update { it.copy(isRegistering = !it.isRegistering) }
    }

    fun signIn() {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = authManager.signIn(
                    _formState.value.email,
                    _formState.value.password
                )
                result.fold(
                    onSuccess = { user ->
                        _authState.value = AuthState.Authenticated(user)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Sign in failed", e)
                        _formState.update { it.copy(error = e.message) }
                    }
                )
            } finally {
                _formState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            try {
                // Double-check if admin role is allowed
                if (_formState.value.role == UserRole.ADMIN) {
                    val hasAdmin = authManager.hasAdminUser()
                    if (hasAdmin) {
                        _formState.update { it.copy(error = "Admin user already exists") }
                        return@launch
                    }
                }

                val result = authManager.signUp(
                    _formState.value.email,
                    _formState.value.password,
                    _formState.value.displayName,
                    _formState.value.role
                )
                result.fold(
                    onSuccess = { user ->
                        Log.d(TAG, "User registered successfully with role: ${user.role}")
                        _authState.value = AuthState.Authenticated(user)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Sign up failed", e)
                        _formState.update { it.copy(error = e.message) }
                    }
                )
            } finally {
                _formState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.Unauthenticated
        _formState.value = AuthFormState()
    }
} 