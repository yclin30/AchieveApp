package com.yclin.achieveapp.ui.feature_tasks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class SyncUiEvent {
    data class Success(val message: String): SyncUiEvent()
    data class Error(val message: String): SyncUiEvent()
}

enum class TaskFilter { ALL, ACTIVE, COMPLETED, TODAY, OVERDUE }

class TaskListViewModel(
    private val taskRepository: TaskRepository,
    private val userId: Long
) : ViewModel() {

    private val _filterState = MutableStateFlow(TaskFilter.ALL)
    val filterState: StateFlow<TaskFilter> = _filterState.asStateFlow()

    val tasks: StateFlow<List<Task>> = combine(
        taskRepository.getAllTasks(userId),
        filterState
    ) { tasks, filter ->
        when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.TODAY -> tasks.filter { it.dueDate == LocalDate.now() && !it.isCompleted }
            TaskFilter.OVERDUE -> tasks.filter { it.dueDate != null && it.dueDate.isBefore(LocalDate.now()) && !it.isCompleted }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _syncEvent = MutableSharedFlow<SyncUiEvent>()
    val syncEvent: SharedFlow<SyncUiEvent> = _syncEvent.asSharedFlow()

    fun setFilter(filter: TaskFilter) {
        _filterState.value = filter
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task) // 软删除
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            try {
                taskRepository.safeSyncTasksToCloud(userId)
                _syncEvent.emit(SyncUiEvent.Success("同步成功"))
            } catch (e: Exception) {
                _syncEvent.emit(SyncUiEvent.Error("同步失败: ${e.message ?: "未知错误"}"))
            }
        }
    }

    class Factory(
        private val application: AchieveApp,
        private val userId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
                return TaskListViewModel(application.taskRepository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}