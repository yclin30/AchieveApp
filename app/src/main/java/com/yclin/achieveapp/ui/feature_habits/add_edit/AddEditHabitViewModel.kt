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

    // 操作结果状态
    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

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
            try {
                val habit = habitRepository.getHabitById(userId, id)
                habit?.let {
                    _name.value = it.name
                    _description.value = it.description
                    _frequencyType.value = it.frequencyType
                    _weekDays.value = it.weekDays
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateFrequencyType(type: Int) {
        _frequencyType.value = type
    }

    fun updateWeekDays(days: Int) {
        _weekDays.value = days
    }

    fun saveHabit() {
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            if (_habitId.value != -1L) {
                // 更新现有习惯
                habitRepository.updateHabit(
                    Habit(
                        id = _habitId.value,
                        userId = userId,
                        name = _name.value,
                        description = _description.value,
                        frequencyType = _frequencyType.value,
                        weekDays = _weekDays.value
                    )
                )
            } else {
                // 创建新习惯
                habitRepository.addHabit(
                    Habit(
                        name = _name.value,
                        userId = userId,
                        description = _description.value,
                        frequencyType = _frequencyType.value,
                        weekDays = _weekDays.value
                    )
                )
            }
            _operationSuccess.value = true
        }
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
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