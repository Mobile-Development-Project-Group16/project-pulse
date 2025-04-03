package com.bda.projectpulse.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.data.models.AIChatMessage
import com.bda.projectpulse.data.repository.AIChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun sendMessage(
        apiKey: String,
        message: String,
        projectId: String,
        projectName: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val updatedMessages = _messages.value.toMutableList().apply {
                    add(AIChatMessage("user", message))
                }

                val response = repository.sendMessage(
                    apiKey = apiKey,
                    messages = updatedMessages,
                    projectId = projectId,
                    projectName = projectName
                )

                updatedMessages.add(response.choices.first().message)
                _messages.value = updatedMessages
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        _error.value = null
    }
} 