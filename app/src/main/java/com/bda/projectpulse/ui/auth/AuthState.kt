package com.bda.projectpulse.ui.auth

import com.bda.projectpulse.models.User
import com.bda.projectpulse.models.UserRole

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.TEAM_MEMBER,
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) 