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
import com.yclin.achieveapp.ui.navigation.Screen
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

    // 四象限字段
    private val _isImportant = MutableStateFlow(false)
    val isImportant: StateFlow<Boolean> = _isImportant.asStateFlow()

    private val _isUrgent = MutableStateFlow(false)
    val isUrgent: StateFlow<Boolean> = _isUrgent.asStateFlow()

    // 任务状态
    private val _taskId = MutableStateFlow<Long>(-1)
    val taskId: StateFlow<Long> = _taskId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        val taskId = savedStateHandle.get<Long>(Screen.AddEditTask.TASK_ID_ARG) ?: -1L
        if (taskId != -1L) {
            _taskId.value = taskId
            loadTask(taskId)
        }
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val task = taskRepository.getTaskById(id)
                task?.let {
                    _title.value = it.title
                    _description.value = it.description
                    _dueDate.value = it.dueDate
                    _isImportant.value = it.isImportant
                    _isUrgent.value = it.isUrgent
                }
            } catch (e: Exception) {
                _error.value = "加载任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(title: String) {
        _title.value = title
        _error.value = null
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateDueDate(dueDate: LocalDate?) {
        _dueDate.value = dueDate
    }

    fun updateImportant(isImportant: Boolean) {
        _isImportant.value = isImportant
    }

    fun updateUrgent(isUrgent: Boolean) {
        _isUrgent.value = isUrgent
    }

    fun saveTask() {
        if (_title.value.isBlank()) {
            _error.value = "标题不能为空"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (_taskId.value != -1L) {
                    // 更新现有任务
                    val oldTask = taskRepository.getTaskById(_taskId.value)
                    val task = Task(
                        id = _taskId.value,
                        userId = userId,
                        title = _title.value,
                        description = _description.value,
                        dueDate = _dueDate.value,
                        isImportant = _isImportant.value,
                        isUrgent = _isUrgent.value,
                        isCompleted = oldTask?.isCompleted ?: false,
                        deleted = oldTask?.deleted ?: false, // 保持删除状态
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
                        isImportant = _isImportant.value,
                        isUrgent = _isUrgent.value,
                        isCompleted = false,
                        deleted = false,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    taskRepository.addTask(task)
                }
                _operationSuccess.value = true
            } catch (e: Exception) {
                _error.value = "保存失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }

    // 便捷方法：直接设置象限
    fun setQuadrant(isImportant: Boolean, isUrgent: Boolean) {
        _isImportant.value = isImportant
        _isUrgent.value = isUrgent
    }

    // 获取当前象限类型
    fun getCurrentQuadrant(): com.yclin.achieveapp.data.database.entity.QuadrantType {
        return when {
            _isImportant.value && _isUrgent.value -> com.yclin.achieveapp.data.database.entity.QuadrantType.URGENT_IMPORTANT
            _isImportant.value && !_isUrgent.value -> com.yclin.achieveapp.data.database.entity.QuadrantType.IMPORTANT_NOT_URGENT
            !_isImportant.value && _isUrgent.value -> com.yclin.achieveapp.data.database.entity.QuadrantType.URGENT_NOT_IMPORTANT
            else -> com.yclin.achieveapp.data.database.entity.QuadrantType.NOT_URGENT_NOT_IMPORTANT
        }
    }

    companion object {
        fun provideFactory(userId: Long, taskId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as AchieveApp
                    val savedStateHandle = extras.createSavedStateHandle()

                    // 设置 taskId 参数
                    if (taskId != -1L) {
                        savedStateHandle[Screen.AddEditTask.TASK_ID_ARG] = taskId
                    }

                    return AddEditTaskViewModel(
                        userId = userId,
                        taskRepository = application.taskRepository,
                        savedStateHandle = savedStateHandle
                    ) as T
                }
            }
    }
}