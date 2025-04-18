package com.bda.projectpulse.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.data.ApiKeyDataSource
import com.bda.projectpulse.data.ModelConfigDataSource
import com.bda.projectpulse.data.models.ModelConfig
import com.bda.projectpulse.data.models.AvailableModels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSettingsViewModel @Inject constructor(
    private val apiKeyDataSource: ApiKeyDataSource,
    private val modelConfigDataSource: ModelConfigDataSource
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success.asStateFlow()

    // Use the ModelConfigDataSource's activeModel directly
    val activeModel: StateFlow<ModelConfig> = modelConfigDataSource.activeModel

    private val _selectedModel = MutableStateFlow("deepseek-chat-v3")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Load API key
                val savedApiKey = apiKeyDataSource.getApiKey()
                _apiKey.value = savedApiKey ?: ""
            } catch (e: Exception) {
                _error.value = "Failed to load settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // TODO: Implement API key saving logic
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save API key"
                _isLoading.value = false
            }
        }
    }

    fun setActiveModel(modelId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _success.value = null

                modelConfigDataSource.setActiveModel(modelId)
                _success.value = "Model updated successfully"
            } catch (e: Exception) {
                _error.value = "Failed to update model: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateModel(modelId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _selectedModel.value = modelId
                // TODO: Save the selected model to preferences or database
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 