package com.yclin.achieveapp.ui.feature_habits.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.model.HabitWithCompletions
import com.yclin.achieveapp.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val habitId: Long
) : ViewModel() {

    val habitWithCompletions: StateFlow<HabitWithCompletions?> =
        habitRepository.getHabitWithCompletions(habitId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun isCompletedOn(date: LocalDate, completions: List<com.yclin.achieveapp.data.database.entity.HabitCompletion>): Boolean {
        return completions.any { it.date == date && it.isCompleted }
    }

    fun toggleCompletion(date: LocalDate) {
        viewModelScope.launch {
            val completed = habitRepository.isHabitCompletedOnDate(habitId, date)
            if (completed) {
                habitRepository.uncompleteHabit(habitId, date)
            } else {
                habitRepository.completeHabit(habitId, date)
            }
        }
    }

    companion object {
        fun provideFactory(habitId: Long, application: AchieveApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HabitDetailViewModel(
                        habitRepository = application.habitRepository,
                        habitId = habitId
                    ) as T
                }
            }
    }
}