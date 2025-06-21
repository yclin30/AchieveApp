package com.yclin.achieveapp.ui.feature_habits.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddEditHabitViewModel(
    private val userId: Long,
    private val habitRepository: HabitRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 表单状态
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _frequencyType = MutableStateFlow(1)
    val frequencyType: StateFlow<Int> = _frequencyType.asStateFlow()

    private val _weekDays = MutableStateFlow(0)
    val weekDays: StateFlow<Int> = _weekDays.asStateFlow()

    // 正在编辑的习惯ID
    private val _habitId = MutableStateFlow<Long>(-1)
    val habitId: StateFlow<Long> = _habitId.asStateFlow()

    // 习惯加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 保存状态
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // 操作结果状态
    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        val habitId = savedStateHandle.get<Long>("habitId") ?: -1L
        if (habitId != -1L) {
            _habitId.value = habitId
            loadHabit(habitId)
        }
    }

    private fun loadHabit(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val habit = habitRepository.getHabitById(id, userId)
                habit?.let {
                    _name.value = it.name
                    _description.value = it.description
                    _frequencyType.value = it.frequencyType
                    _weekDays.value = it.weekDays
                } ?: run {
                    _error.value = "习惯不存在"
                }
            } catch (e: Exception) {
                _error.value = "加载习惯失败: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateName(name: String) {
        _name.value = name
        // 清除之前的错误
        if (_error.value != null) {
            _error.value = null
        }
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateFrequencyType(type: Int) {
        _frequencyType.value = type
        // 如果改为每日，清除周几选择
        if (type == 1) {
            _weekDays.value = 0
        }
    }

    fun updateWeekDays(days: Int) {
        _weekDays.value = days
    }

    fun saveHabit() {
        // 验证输入
        if (_name.value.isBlank()) {
            _error.value = "习惯名称不能为空"
            return
        }

        if (_frequencyType.value == 2 && _weekDays.value == 0) {
            _error.value = "每周频率至少需要选择一天"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            try {
                val habit = Habit(
                    id = if (_habitId.value != -1L) _habitId.value else 0,
                    userId = userId,
                    name = _name.value.trim(),
                    description = _description.value.trim(),
                    frequencyType = _frequencyType.value,
                    weekDays = _weekDays.value
                )

                if (_habitId.value != -1L) {
                    // 更新现有习惯
                    habitRepository.updateHabit(habit)
                } else {
                    // 创建新习惯并保存新ID
                    val newHabitId = habitRepository.addHabit(habit)
                    _habitId.value = newHabitId
                }

                _operationSuccess.value = true

            } catch (e: Exception) {
                _error.value = "保存失败: ${e.message}"
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * 验证表单数据
     */
    fun validateForm(): Boolean {
        when {
            _name.value.isBlank() -> {
                _error.value = "习惯名称不能为空"
                return false
            }
            _frequencyType.value == 2 && _weekDays.value == 0 -> {
                _error.value = "每周频率至少需要选择一天"
                return false
            }
            else -> {
                _error.value = null
                return true
            }
        }
    }

    companion object {
        fun provideFactory(userId: Long, habitId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as AchieveApp
                val savedStateHandle = extras.createSavedStateHandle()
                savedStateHandle["habitId"] = habitId

                return AddEditHabitViewModel(
                    userId = userId,
                    habitRepository = application.habitRepository,
                    savedStateHandle = savedStateHandle
                ) as T
            }
        }
    }
}