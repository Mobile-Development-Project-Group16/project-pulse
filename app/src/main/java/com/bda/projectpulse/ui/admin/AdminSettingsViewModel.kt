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

    fun saveApiKey() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _success.value = null

                if (apiKeyDataSource.saveApiKey(_apiKey.value)) {
                    _success.value = "API key saved successfully"
                } else {
                    _error.value = "Failed to save API key"
                }
            } catch (e: Exception) {
                _error.value = "Failed to save API key: ${e.message}"
            } finally {
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
} 