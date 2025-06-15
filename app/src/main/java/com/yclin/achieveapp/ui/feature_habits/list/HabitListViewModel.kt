package com.yclin.achieveapp.ui.feature_habits.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SyncUiEvent {
    data class Success(val message: String): SyncUiEvent()
    data class Error(val message: String): SyncUiEvent()
}

class HabitListViewModel(
    private val habitRepository: HabitRepository,
    private val userId: Long
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = habitRepository.getAllHabits(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _syncEvent = MutableSharedFlow<SyncUiEvent>()
    val syncEvent: SharedFlow<SyncUiEvent> = _syncEvent.asSharedFlow()

    fun syncHabits() {
        viewModelScope.launch {
            try {
                habitRepository.syncHabitsFromRemote(userId)
                _syncEvent.emit(SyncUiEvent.Success("同步成功"))
            } catch (e: Exception) {
                _syncEvent.emit(SyncUiEvent.Error("同步失败: ${e.message ?: "未知错误"}"))
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    class Factory(
        private val application: AchieveApp,
        private val userId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HabitListViewModel::class.java)) {
                return HabitListViewModel(application.habitRepository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}