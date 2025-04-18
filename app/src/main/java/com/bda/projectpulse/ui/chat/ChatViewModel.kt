package com.bda.projectpulse.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.ChatMessage
import com.bda.projectpulse.repositories.ChatRepository
import com.bda.projectpulse.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            try {
                currentUserId = userRepository.getCurrentUser()?.uid
            } catch (e: Exception) {
                _error.value = "Failed to get current user: ${e.message}"
            }
        }
    }

    fun loadMessages(projectId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                chatRepository.getProjectMessages(projectId).collect { messages ->
                    _messages.value = messages.map { message ->
                        message.copy(isFromCurrentUser = message.senderId == currentUserId)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load messages: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(projectId: String, text: String, attachments: List<Uri> = emptyList()) {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    chatRepository.sendMessage(
                        projectId = projectId,
                        text = text,
                        senderId = currentUser.uid,
                        senderName = currentUser.displayName,
                        attachments = attachments
                    )
                } else {
                    _error.value = "User not logged in"
                }
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }
} 