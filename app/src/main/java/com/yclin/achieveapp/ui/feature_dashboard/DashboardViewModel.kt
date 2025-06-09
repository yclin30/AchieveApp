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
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val application: Application
) : ViewModel() {

    val todayTasks: StateFlow<List<Task>> = taskDao.getTodayTasksFlow()
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
        .flatMapLatest { date -> // date 是今天的日期
            val todayBit = 1 shl (date.dayOfWeek.value - 1)
            habitDao.getHabitsForTodayFlow(todayBit) // 这个返回 Flow<List<Habit>>
                .catch { e ->
                    System.err.println("获取今日习惯时出错: ${e.message}")
                    emit(emptyList<Habit>())
                }
                .map { habitsList -> // *** 修改点: 这里用 Flow 的 map 操作 habitsList (List<Habit>) ***
                    // 现在 habitsList 是一个 List<Habit>
                    // 我们需要为这个列表中的每个 habit 获取其完成状态
                    // 这将是一个 List<HabitDashboardItem>
                    if (habitsList.isEmpty()) {
                        emptyList<HabitDashboardItem>()
                    } else {
                        // 使用 List 的 map (非 Flow 的 map) 来转换每个 habit
                        // 假设 getCompletionByDate 是 suspend fun
                        habitsList.map { habit ->
                            try {
                                val completion = habitCompletionDao.getCompletionByDate(habit.id, date) // suspend call
                                HabitDashboardItem(habit, completion != null, date)
                            } catch (e: Exception) {
                                System.err.println("获取习惯 ${habit.id} 的完成状态时出错 (内部 map): ${e.message}")
                                HabitDashboardItem(habit, false, date) // 出错时默认未完成
                            }
                        }
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList<HabitDashboardItem>())

    fun toggleHabitCompletion(habitId: Long, date: LocalDate, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            val habit = habitDao.getHabitById(habitId)
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
                habitDao.updateHabitStreaks(habitToUpdate.id, 0, habitToUpdate.longestStreak)
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
            habitDao.updateHabitStreaks(habitToUpdate.id, newCurrentStreak, finalLongestStreak)
        }
    }

    companion object {
        fun provideFactory(
            taskDao: TaskDao,
            habitDao: HabitDao,
            habitCompletionDao: HabitCompletionDao,
            application: Application
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(taskDao, habitDao, habitCompletionDao, application) as T
                    }
                    throw IllegalArgumentException("未知的 ViewModel 类: ${modelClass.name}")
                }
            }
        }
    }
}