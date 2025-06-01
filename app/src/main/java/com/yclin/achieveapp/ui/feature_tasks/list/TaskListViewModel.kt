package com.yclin.achieveapp.ui.feature_tasks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class TaskListViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    // 过滤器状态
    private val _filterState = MutableStateFlow(TaskFilter.ALL)
    val filterState: StateFlow<TaskFilter> = _filterState

    // 任务列表状态
    val tasks: StateFlow<List<Task>> = combine(
        taskRepository.getAllTasks(),
        filterState
    ) { tasks, filter ->
        when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.TODAY -> tasks.filter {
                it.dueDate == LocalDate.now() && !it.isCompleted
            }
            TaskFilter.OVERDUE -> tasks.filter {
                it.dueDate != null && it.dueDate.isBefore(LocalDate.now()) && !it.isCompleted
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 设置过滤器
    fun setFilter(filter: TaskFilter) {
        _filterState.value = filter
    }

    // 删除任务
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    // 切换任务完成状态
    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(
                task.copy(
                    isCompleted = !task.isCompleted,
                    updatedAt = LocalDateTime.now()
                )
            )
        }
    }

    // ViewModel Factory，用于创建ViewModel实例
    class Factory(private val application: AchieveApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
                return TaskListViewModel(application.taskRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// 任务过滤枚举
enum class TaskFilter {
    ALL, ACTIVE, COMPLETED, TODAY, OVERDUE
}