package com.bda.projectpulse.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.User
import com.bda.projectpulse.repositories.TaskRepository
import com.bda.projectpulse.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tasks: List<Task> = emptyList()
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUser.value = userRepository.getCurrentUser()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user: ${e.message}"
                )
            }
        }
    }

    fun loadAllTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val tasks = taskRepository.getTasks().first()
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load tasks: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun loadProjectTasks(projectId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "User not authenticated",
                        isLoading = false
                    )
                    return@launch
                }
                
                taskRepository.getProjectTasks(projectId, currentUser.role, currentUser.uid)
                    .collect { tasks ->
                        _uiState.value = _uiState.value.copy(
                            tasks = tasks,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load project tasks: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun refreshTasks() {
        loadAllTasks()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 