package com.bda.projectpulse.viewmodels

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bda.projectpulse.models.Attachment
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.repositories.TaskRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _users = MutableStateFlow<List<com.bda.projectpulse.models.User>>(emptyList())
    val users: StateFlow<List<com.bda.projectpulse.models.User>> = _users.asStateFlow()

    private val _attachments = MutableStateFlow<List<Attachment>>(emptyList())
    val attachments: StateFlow<List<Attachment>> = _attachments.asStateFlow()

    fun loadTaskById(taskId: String) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTask(taskId)
                _selectedTask.value = task
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load task"
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val users = taskRepository.getUsers()
                _users.value = users
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load users"
            }
        }
    }

    fun approveTask() {
        viewModelScope.launch {
            try {
                _selectedTask.value?.let { task ->
                    taskRepository.updateTaskStatus(task.id, TaskStatus.APPROVED)
                    _selectedTask.value = task.copy(
                        status = TaskStatus.APPROVED,
                        updatedAt = Timestamp.now()
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to approve task"
            }
        }
    }

    fun rejectTask() {
        viewModelScope.launch {
            try {
                _selectedTask.value?.let { task ->
                    taskRepository.updateTaskStatus(task.id, TaskStatus.IN_REVIEW)
                    _selectedTask.value = task.copy(
                        status = TaskStatus.IN_REVIEW,
                        updatedAt = Timestamp.now()
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reject task"
            }
        }
    }

    fun updateTaskStatus(status: TaskStatus) {
        viewModelScope.launch {
            try {
                _selectedTask.value?.let { task ->
                    taskRepository.updateTaskStatus(task.id, status)
                    _selectedTask.value = task.copy(
                        status = status,
                        updatedAt = Timestamp.now()
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update task status"
            }
        }
    }

    fun updateTaskPriority(priority: com.bda.projectpulse.models.TaskPriority) {
        viewModelScope.launch {
            try {
                val task = _selectedTask.value
                if (task != null) {
                    val updatedTask = task.copy(
                        priority = priority,
                        updatedAt = Timestamp.now()
                    )
                    taskRepository.updateTask(updatedTask)
                    _selectedTask.value = updatedTask
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update task priority"
            }
        }
    }

    fun getUserName(userId: String): String {
        return _users.value.find { it.uid == userId }?.displayName ?: "Unknown User"
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.createTask(task)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create task"
            }
        }
    }

    fun submitTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.createTask(task)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to submit task"
            }
        }
    }

    fun addAttachment(projectId: String, taskId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val attachment = taskRepository.addTaskAttachment(projectId, taskId, uri)
                _attachments.value = _attachments.value + attachment
                taskRepository.updateTaskWithAttachments(taskId, _attachments.value)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add attachment"
            }
        }
    }

    fun deleteAttachment(projectId: String, taskId: String, attachmentId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTaskAttachment(projectId, taskId, attachmentId)
                _attachments.value = _attachments.value.filter { it.id != attachmentId }
                taskRepository.updateTaskWithAttachments(taskId, _attachments.value)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete attachment"
            }
        }
    }

    fun clearAttachments() {
        _attachments.value = emptyList()
    }
} 