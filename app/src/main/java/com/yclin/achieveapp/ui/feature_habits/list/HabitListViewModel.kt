package com.yclin.achieveapp.ui.feature_habits.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitListViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = habitRepository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    class Factory(private val application: AchieveApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HabitListViewModel::class.java)) {
                return HabitListViewModel(application.habitRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}