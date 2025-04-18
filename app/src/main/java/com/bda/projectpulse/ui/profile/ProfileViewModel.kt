package com.bda.projectpulse.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.User
import com.bda.projectpulse.repositories.UserRepository
import com.bda.projectpulse.repositories.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow<Int>(0)
    val unreadNotificationsCount = _unreadNotificationsCount.asStateFlow()

    private val storage = Firebase.storage

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

    fun loadUnreadNotificationsCount() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUser()?.uid ?: return@launch
                notificationRepository.getUnreadNotificationCount(currentUserId).collect { count ->
                    _unreadNotificationsCount.value = count
                }
            } catch (e: Exception) {
                println("Error loading unread notifications count: ${e.message}")
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

    fun uploadProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                val storageRef = storage.reference
                val profilePicturesRef = storageRef.child("profile_pictures/${currentUser.uid}/${UUID.randomUUID()}")
                
                val uploadTask = profilePicturesRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                
                // Update user profile with new photo URL
                currentUser.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUrl)
                        .build()
                ).await()
                
                // Update Firestore user document
                userRepository.updateUserProfile(
                    currentUser.uid,
                    photoUrl = downloadUrl.toString()
                )
                
                // Reload user data
                loadCurrentUser()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 