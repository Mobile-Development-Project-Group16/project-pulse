package com.bda.projectpulse.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.data.repository.TaskRepository
import com.bda.projectpulse.data.repository.UserRepository
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
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

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _currentUser.value = userRepository.getCurrentUser()
            } catch (e: Exception) {
                _uiState.update { currentState -> currentState.copy(error = "Failed to load user: ${e.message}") }
            }
        }
    }

    fun loadAllTasks() {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true, error = null) }
            try {
                val tasks = taskRepository.getAllTasks()
                _uiState.update { currentState -> currentState.copy(tasks = tasks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { currentState -> 
                    currentState.copy(error = "Failed to load tasks: ${e.message}", isLoading = false) 
                }
            }
        }
    }

    fun loadProjectTasks(projectId: String) {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true, error = null) }
            try {
                val tasks = taskRepository.getTasksByProjectId(projectId)
                _uiState.update { currentState -> currentState.copy(tasks = tasks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { currentState -> 
                    currentState.copy(error = "Failed to load project tasks: ${e.message}", isLoading = false) 
                }
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