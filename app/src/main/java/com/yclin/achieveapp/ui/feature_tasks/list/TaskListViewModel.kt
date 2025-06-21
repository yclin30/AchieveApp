package com.yclin.achieveapp.ui.feature_tasks.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.data.database.entity.QuadrantType
import com.yclin.achieveapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SyncUiEvent {
    data class Success(val message: String): SyncUiEvent()
    data class Error(val message: String): SyncUiEvent()
}

enum class TaskFilter {
    ALL, ACTIVE, COMPLETED, TODAY, OVERDUE
}

sealed class TaskAction {
    data class ToggleCompleted(val taskId: Long): TaskAction()
    data class DeleteTask(val taskId: Long): TaskAction()
    data class MoveTask(val taskId: Long, val targetQuadrant: QuadrantType): TaskAction()
}

data class TaskListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCompleted: Boolean = false,
    val filter: TaskFilter = TaskFilter.ALL
)

class TaskListViewModel(
    private val taskRepository: TaskRepository,
    private val userId: Long
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    // 过滤状态（用于传统列表视图）
    private val _filterState = MutableStateFlow(TaskFilter.ALL)
    val filterState: StateFlow<TaskFilter> = _filterState.asStateFlow()

    // 显示已完成任务的状态（用于四象限视图）
    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

    // 同步事件
    private val _syncEvent = MutableSharedFlow<SyncUiEvent>()
    val syncEvent: SharedFlow<SyncUiEvent> = _syncEvent.asSharedFlow()

    // 传统列表视图的任务数据（基于过滤器）
    val tasks: StateFlow<List<Task>> = combine(
        taskRepository.getAllTasks(userId),
        _filterState
    ) { tasks, filter ->
        when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilter.TODAY -> tasks.filter {
                it.dueDate == java.time.LocalDate.now() && !it.isCompleted
            }
            TaskFilter.OVERDUE -> tasks.filter {
                it.dueDate != null &&
                        it.dueDate.isBefore(java.time.LocalDate.now()) &&
                        !it.isCompleted
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 四象限任务数据
    val urgentAndImportantTasks: StateFlow<List<Task>> = combine(
        taskRepository.getUrgentAndImportantTasks(userId),
        _showCompleted
    ) { tasks, showCompleted ->
        if (showCompleted) tasks else tasks.filter { !it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val importantNotUrgentTasks: StateFlow<List<Task>> = combine(
        taskRepository.getImportantNotUrgentTasks(userId),
        _showCompleted
    ) { tasks, showCompleted ->
        if (showCompleted) tasks else tasks.filter { !it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val urgentNotImportantTasks: StateFlow<List<Task>> = combine(
        taskRepository.getUrgentNotImportantTasks(userId),
        _showCompleted
    ) { tasks, showCompleted ->
        if (showCompleted) tasks else tasks.filter { !it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notUrgentNotImportantTasks: StateFlow<List<Task>> = combine(
        taskRepository.getNotUrgentNotImportantTasks(userId),
        _showCompleted
    ) { tasks, showCompleted ->
        if (showCompleted) tasks else tasks.filter { !it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 加载状态（来自 UI 状态）
    val isLoading: StateFlow<Boolean> = _uiState.map { it.isLoading }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // 传统列表视图的方法
    fun setFilter(filter: TaskFilter) {
        _filterState.value = filter
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    // 四象限视图的方法
    fun toggleShowCompleted() {
        val newValue = !_showCompleted.value
        _showCompleted.value = newValue
        _uiState.value = _uiState.value.copy(showCompleted = newValue)
    }

    // 任务操作
    fun handleTaskAction(action: TaskAction) {
        viewModelScope.launch {
            try {
                when (action) {
                    is TaskAction.ToggleCompleted -> {
                        val task = taskRepository.getTaskById(action.taskId)
                        task?.let {
                            taskRepository.setTaskCompleted(action.taskId, !it.isCompleted)
                        }
                    }
                    is TaskAction.DeleteTask -> {
                        val task = taskRepository.getTaskById(action.taskId)
                        task?.let {
                            taskRepository.deleteTask(it)
                            _syncEvent.emit(SyncUiEvent.Success("任务已删除"))
                        }
                    }
                    is TaskAction.MoveTask -> {
                        taskRepository.moveTaskToQuadrant(action.taskId, action.targetQuadrant)
                        _syncEvent.emit(SyncUiEvent.Success("任务已移动到${action.targetQuadrant.displayName}"))
                    }
                }
            } catch (e: Exception) {
                _syncEvent.emit(SyncUiEvent.Error("操作失败: ${e.message}"))
            }
        }
    }

    // 传统列表视图的便捷方法
    fun deleteTask(task: Task) {
        handleTaskAction(TaskAction.DeleteTask(task.id))
    }

    fun toggleTaskCompleted(task: Task) {
        handleTaskAction(TaskAction.ToggleCompleted(task.id))
    }

    // 同步功能
    fun syncTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 双向同步
                taskRepository.syncTasksFromRemote(userId)
                taskRepository.safeSyncTasksToCloud(userId)
                _syncEvent.emit(SyncUiEvent.Success("同步成功"))
            } catch (e: Exception) {
                val errorMessage = "同步失败: ${e.message ?: "未知错误"}"
                _uiState.value = _uiState.value.copy(error = errorMessage)
                _syncEvent.emit(SyncUiEvent.Error(errorMessage))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // 错误处理
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // 获取任务统计信息
    fun getTaskStats(): StateFlow<TaskStats> = combine(
        urgentAndImportantTasks,
        importantNotUrgentTasks,
        urgentNotImportantTasks,
        notUrgentNotImportantTasks
    ) { urgentImportant, importantNotUrgent, urgentNotImportant, notUrgentNotImportant ->
        TaskStats(
            urgentImportantCount = urgentImportant.count { !it.isCompleted },
            importantNotUrgentCount = importantNotUrgent.count { !it.isCompleted },
            urgentNotImportantCount = urgentNotImportant.count { !it.isCompleted },
            notUrgentNotImportantCount = notUrgentNotImportant.count { !it.isCompleted },
            totalActiveTasks = (urgentImportant + importantNotUrgent + urgentNotImportant + notUrgentNotImportant)
                .count { !it.isCompleted }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStats()
    )

    // 工厂类
    class Factory(
        private val application: AchieveApp,
        private val userId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
                return TaskListViewModel(application.taskRepository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    // 专门为四象限视图的工厂类
    class QuadrantFactory(
        private val application: AchieveApp,
        private val userId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
                return TaskListViewModel(application.taskRepository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

// 任务统计数据类
data class TaskStats(
    val urgentImportantCount: Int = 0,
    val importantNotUrgentCount: Int = 0,
    val urgentNotImportantCount: Int = 0,
    val notUrgentNotImportantCount: Int = 0,
    val totalActiveTasks: Int = 0
) {
    val hasUrgentTasks: Boolean get() = urgentImportantCount > 0 || urgentNotImportantCount > 0
    val hasImportantTasks: Boolean get() = urgentImportantCount > 0 || importantNotUrgentCount > 0

    fun getQuadrantCount(quadrant: QuadrantType): Int = when (quadrant) {
        QuadrantType.URGENT_IMPORTANT -> urgentImportantCount
        QuadrantType.IMPORTANT_NOT_URGENT -> importantNotUrgentCount
        QuadrantType.URGENT_NOT_IMPORTANT -> urgentNotImportantCount
        QuadrantType.NOT_URGENT_NOT_IMPORTANT -> notUrgentNotImportantCount
    }
}