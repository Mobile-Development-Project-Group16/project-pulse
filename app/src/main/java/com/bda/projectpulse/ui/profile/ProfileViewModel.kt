package com.bda.projectpulse.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.User
import com.bda.projectpulse.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _currentUser.value = userRepository.getCurrentUser()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                FirebaseAuth.getInstance().signOut()
                _currentUser.value = null
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val user = FirebaseAuth.getInstance().currentUser
                user?.updatePassword(newPassword)?.await()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val user = FirebaseAuth.getInstance().currentUser
                user?.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build())?.await()
                loadCurrentUser() // Reload user data after update
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 