package com.yclin.achieveapp.ui.feature_dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.data.database.dao.HabitCompletionDao
import com.yclin.achieveapp.data.database.dao.HabitDao
import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.database.entity.HabitCompletion
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.util.StreakCalculationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val userId: Long,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val application: Application
) : ViewModel() {

    val todayTasks: StateFlow<List<Task>> = taskDao.getTodayTasksFlow(userId)
        .catch { e ->
            System.err.println("获取今日任务时出错: ${e.message}")
            emit(emptyList<Task>())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList<Task>()
        )

    val todayHabits: StateFlow<List<HabitDashboardItem>> = flow { emit(LocalDate.now(ZoneOffset.UTC)) }
        .flatMapLatest { date ->
            val todayBit = 1 shl (date.dayOfWeek.value - 1) // Int 类型
            habitDao.getHabitsForTodayFlow(userId, todayBit)
                .catch { e ->
                    System.err.println("获取今日习惯时出错: ${e.message}")
                    emit(emptyList<Habit>())
                }
                .map { habitsList ->
                    if (habitsList.isEmpty()) {
                        emptyList<HabitDashboardItem>()
                    } else {
                        val habitIds = habitsList.map { it.id }
                        // 你需要在 HabitCompletionDao 实现 getCompletionsByHabitIdsAndDate
                        val completions = habitCompletionDao.getCompletionsByHabitIdsAndDate(habitIds, date)
                        val completedMap = completions.associateBy { it.habitId }
                        habitsList.map { habit ->
                            HabitDashboardItem(habit, completedMap.containsKey(habit.id), date)
                        }
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList<HabitDashboardItem>())

    fun toggleHabitCompletion(habitId: Long, date: LocalDate, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            val habit = habitDao.getHabitById(habitId, userId)
            if (habit == null) {
                System.err.println("切换习惯完成状态时未找到习惯: $habitId")
                return@launch
            }

            if (isCurrentlyCompleted) {
                habitCompletionDao.deleteCompletionByHabitIdAndDate(habitId, date)
            } else {
                val completionRecord = HabitCompletion(
                    habitId = habitId,
                    date = date
                )
                habitCompletionDao.insertCompletion(completionRecord)
            }
            updateStreakAfterToggle(habit)
        }
    }

    private suspend fun updateStreakAfterToggle(habitToUpdate: Habit) {
        val allCompletionDatesForHabit = habitCompletionDao.getCompletionsForHabitFlow(habitToUpdate.id)
            .first()
            .map { it.date }
            .sorted()

        if (allCompletionDatesForHabit.isEmpty()) {
            if (habitToUpdate.currentStreak != 0) {
                habitDao.updateHabitStreaks(
                    habitId = habitToUpdate.id,
                    userId = userId,
                    currentStreak = 0,
                    longestStreak = habitToUpdate.longestStreak
                )
            }
            return
        }

        val (newCurrentStreak, calculatedLongestStreak) = StreakCalculationHelper.calculateStreaks(
            completedDates = allCompletionDatesForHabit,
            frequencyType = habitToUpdate.frequencyType,
            weekDays = habitToUpdate.weekDays,
            habitCreatedAt = habitToUpdate.createdAt.toLocalDate()
        )
        val finalLongestStreak = maxOf(calculatedLongestStreak, habitToUpdate.longestStreak)
        if (habitToUpdate.currentStreak != newCurrentStreak || habitToUpdate.longestStreak != finalLongestStreak) {
            habitDao.updateHabitStreaks(
                habitId = habitToUpdate.id,
                userId = userId,
                currentStreak = newCurrentStreak,
                longestStreak = finalLongestStreak
            )
        }
    }

    companion object {
        fun provideFactory(
            userId: Long,
            taskDao: TaskDao,
            habitDao: HabitDao,
            habitCompletionDao: HabitCompletionDao,
            application: Application
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(userId, taskDao, habitDao, habitCompletionDao, application) as T
                    }
                    throw IllegalArgumentException("未知的 ViewModel 类: ${modelClass.name}")
                }
            }
        }
    }
}