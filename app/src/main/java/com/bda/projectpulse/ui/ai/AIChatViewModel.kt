package com.bda.projectpulse.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.data.AIChatRepository
import com.bda.projectpulse.data.ChatMessage
import com.bda.projectpulse.data.models.AIChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val repository: AIChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val messages: StateFlow<List<AIChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentProjectId: String? = null

    fun loadChatHistory(projectId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                currentProjectId = projectId

                // Load chat history from repository
                val history = repository.getChatHistory(projectId)
                
                // Convert ChatMessage to AIChatMessage
                val messages = history.map { message ->
                    AIChatMessage(
                        role = message.role,
                        content = message.content
                    )
                }

                _messages.value = messages
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load chat history"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(
        message: String,
        projectId: String,
        projectName: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Add user message immediately
                _messages.update { currentMessages ->
                    currentMessages + AIChatMessage("user", message)
                }

                // Collect AI response
                repository.sendMessage(projectId, message)
                    .catch { e -> 
                        _error.value = e.message ?: "An error occurred"
                    }
                    .collect { response ->
                        val aiMessage = response.choices.first().message
                        _messages.update { currentMessages ->
                            currentMessages + aiMessage
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            currentProjectId?.let { projectId ->
                repository.clearChatHistory(projectId)
                _messages.value = emptyList()
                _error.value = null
            }
        }
    }
} 