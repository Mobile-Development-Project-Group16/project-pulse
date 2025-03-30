package com.bda.projectpulse.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.data.TaskRepository
import com.bda.projectpulse.data.UserRepository
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamMemberUiState(
    val selectedUser: User? = null,
    val assignedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TeamMemberViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamMemberUiState())
    val uiState: StateFlow<TeamMemberUiState> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            userRepository.getUserById(userId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { user ->
                    _uiState.update { it.copy(isLoading = false, selectedUser = user) }
                    loadAssignedTasks(userId)
                }
        }
    }

    private fun loadAssignedTasks(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            taskRepository.getTasksByAssignee(userId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { tasks ->
                    _uiState.update { it.copy(isLoading = false, assignedTasks = tasks) }
                }
        }
    }
} 