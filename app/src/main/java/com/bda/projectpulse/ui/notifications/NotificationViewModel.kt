package com.bda.projectpulse.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.Notification
import com.bda.projectpulse.repositories.NotificationRepository
import com.bda.projectpulse.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val TAG = "NotificationViewModel"

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "NotificationViewModel initialized, loading notifications")
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            Log.d(TAG, "Starting to load notifications")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = userRepository.getCurrentUser()
                Log.d(TAG, "Current user: ${currentUser?.displayName} (${currentUser?.uid})")
                
                if (currentUser != null) {
                    Log.d(TAG, "Fetching notifications for user ID: ${currentUser.uid}")
                    notificationRepository.getNotificationsForUser(currentUser.uid)
                        .collect { notifications ->
                            Log.d(TAG, "Received ${notifications.size} notifications")
                            _uiState.update { state ->
                                Log.d(TAG, "Updating UI state with notifications")
                                state.copy(
                                    notifications = notifications,
                                    unreadCount = notifications.count { !it.read },
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                } else {
                    Log.e(TAG, "Cannot load notifications: current user is null")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notifications", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Marking notification as read: $notificationId")
                notificationRepository.markAsRead(notificationId)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Marking all notifications as read")
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    notificationRepository.markAllAsRead(currentUser.uid)
                } else {
                    Log.e(TAG, "Cannot mark all as read: current user is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking all notifications as read", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting notification: $notificationId")
                notificationRepository.deleteNotification(notificationId)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notification", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        Log.d(TAG, "Clearing error")
        _uiState.update { it.copy(error = null) }
    }
    
    // Force reload notifications
    fun refreshNotifications() {
        Log.d(TAG, "Manual refresh of notifications requested")
        loadNotifications()
    }
} 