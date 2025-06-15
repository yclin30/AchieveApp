package com.yclin.achieveapp.ui.feature_tasks.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class AddEditTaskViewModel(
    private val userId: Long,
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 表单状态
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _dueDate = MutableStateFlow<LocalDate?>(null)
    val dueDate: StateFlow<LocalDate?> = _dueDate.asStateFlow()

    private val _priority = MutableStateFlow(0)
    val priority: StateFlow<Int> = _priority.asStateFlow()

    // 正在编辑的任务ID
    private val _taskId = MutableStateFlow<Long>(-1)
    val taskId: StateFlow<Long> = _taskId.asStateFlow()

    // 任务加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 操作结果状态
    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    init {
        val taskId = savedStateHandle.get<Long>("taskId") ?: -1L
        if (taskId != -1L) {
            _taskId.value = taskId
            loadTask(taskId)
        }
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val task = taskRepository.getTaskById(id)
                task?.let {
                    _title.value = it.title
                    _description.value = it.description
                    _dueDate.value = it.dueDate
                    _priority.value = it.priority
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(title: String) {
        _title.value = title
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateDueDate(dueDate: LocalDate?) {
        _dueDate.value = dueDate
    }

    fun updatePriority(priority: Int) {
        _priority.value = priority
    }

    fun saveTask() {
        if (_title.value.isBlank()) return

        viewModelScope.launch {
            if (_taskId.value != -1L) {
                // 更新现有任务，保留原有 createdAt 和 isCompleted
                val oldTask = taskRepository.getTaskById(_taskId.value)
                val task = Task(
                    id = _taskId.value,
                    userId = userId,
                    title = _title.value,
                    description = _description.value,
                    dueDate = _dueDate.value,
                    priority = _priority.value,
                    isCompleted = oldTask?.isCompleted ?: false,
                    createdAt = oldTask?.createdAt ?: LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                taskRepository.updateTask(task)
            } else {
                // 创建新任务
                val task = Task(
                    userId = userId,
                    title = _title.value,
                    description = _description.value,
                    dueDate = _dueDate.value,
                    priority = _priority.value,
                    isCompleted = false,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                taskRepository.addTask(task)
            }
            _operationSuccess.value = true
        }
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }

    companion object {
        fun provideFactory(userId: Long, taskId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as AchieveApp
                val savedStateHandle = extras.createSavedStateHandle()
                savedStateHandle["taskId"] = taskId

                return AddEditTaskViewModel(
                    userId = userId,
                    taskRepository = application.taskRepository,
                    savedStateHandle = savedStateHandle
                ) as T
            }
        }
    }
}