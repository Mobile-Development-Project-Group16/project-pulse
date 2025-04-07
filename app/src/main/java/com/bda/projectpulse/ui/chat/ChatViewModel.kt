package com.bda.projectpulse.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.ChatMessage
import com.bda.projectpulse.repositories.ChatRepository
import com.bda.projectpulse.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadMessages(projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                chatRepository.getProjectMessages(projectId)
                    .catch { e -> _error.value = e.message }
                    .collect { messages ->
                        _messages.value = messages
                        _uiState.value = _uiState.value.copy(
                            messages = messages,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun sendMessage(projectId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                val message = ChatMessage(
                    projectId = projectId,
                    senderId = currentUser.uid,
                    senderName = currentUser.displayName,
                    content = content
                )

                chatRepository.sendMessage(message)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteMessage(projectId: String, messageId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteMessage(projectId, messageId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 