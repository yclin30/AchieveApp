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
import com.yclin.achieveapp.data.repository.TaskRepository
import com.yclin.achieveapp.util.StreakCalculationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val userId: Long,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val taskRepository: TaskRepository,
    private val application: Application
) : ViewModel() {

    // 今日任务（截止日期为今天的任务）
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

    // 四象限统计数据
    val quadrantStats: StateFlow<QuadrantStats> = combine(
        taskRepository.getUrgentAndImportantTasks(userId),
        taskRepository.getImportantNotUrgentTasks(userId),
        taskRepository.getUrgentNotImportantTasks(userId),
        taskRepository.getNotUrgentNotImportantTasks(userId)
    ) { urgentImportant, importantNotUrgent, urgentNotImportant, notUrgentNotImportant ->
        QuadrantStats(
            urgentImportantCount = urgentImportant.count { !it.isCompleted },
            importantNotUrgentCount = importantNotUrgent.count { !it.isCompleted },
            urgentNotImportantCount = urgentNotImportant.count { !it.isCompleted },
            notUrgentNotImportantCount = notUrgentNotImportant.count { !it.isCompleted },
            totalActiveTasks = (urgentImportant + importantNotUrgent + urgentNotImportant + notUrgentNotImportant)
                .count { !it.isCompleted }
        )
    }.catch { e ->
        System.err.println("获取四象限统计时出错: ${e.message}")
        emit(QuadrantStats())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = QuadrantStats()
    )

    // 今日习惯
    val todayHabits: StateFlow<List<HabitDashboardItem>> = flow {
        emit(LocalDate.now(ZoneOffset.UTC))
    }.flatMapLatest { date ->
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
                    try {
                        val completions = habitCompletionDao.getCompletionsByHabitIdsAndDate(habitIds, date)
                        val completedMap = completions.associateBy { it.habitId }
                        habitsList.map { habit ->
                            HabitDashboardItem(habit, completedMap.containsKey(habit.id), date)
                        }
                    } catch (e: Exception) {
                        System.err.println("获取习惯完成状态时出错: ${e.message}")
                        habitsList.map { habit ->
                            HabitDashboardItem(habit, false, date)
                        }
                    }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList<HabitDashboardItem>())

    // 切换任务完成状态
    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                task?.let {
                    taskRepository.setTaskCompleted(taskId, !it.isCompleted)
                }
            } catch (e: Exception) {
                System.err.println("切换任务完成状态时出错: ${e.message}")
            }
        }
    }

    // 切换习惯完成状态
    fun toggleHabitCompletion(habitId: Long, date: LocalDate, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                System.err.println("切换习惯完成状态时出错: ${e.message}")
            }
        }
    }

    private suspend fun updateStreakAfterToggle(habitToUpdate: Habit) {
        try {
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
        } catch (e: Exception) {
            System.err.println("更新习惯连续记录时出错: ${e.message}")
        }
    }

    // 获取用户姓名（可以从用户信息中获取，这里先硬编码）
    fun getUserGreeting(): String {
        return "你好，yclin30！今天是"
    }

    // 获取紧急任务提醒
    fun getUrgentTasksAlert(): StateFlow<String?> = quadrantStats.map { stats ->
        when {
            stats.urgentImportantCount > 0 -> "您有 ${stats.urgentImportantCount} 个重要且紧急的任务需要立即处理！"
            stats.urgentNotImportantCount > 3 -> "您有较多紧急但不重要的任务，建议合理安排时间。"
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // 获取建议信息
    fun getProductivityTip(): StateFlow<String> = quadrantStats.map { stats ->
        when {
            stats.importantNotUrgentCount == 0 -> "建议制定一些重要但不紧急的长期目标和计划。"
            stats.urgentImportantCount > stats.importantNotUrgentCount -> "试着将更多时间投入到重要但不紧急的任务上，这样可以减少紧急情况。"
            stats.notUrgentNotImportantCount > 5 -> "考虑减少或删除一些不重要不紧急的任务，专注于更有价值的事情。"
            else -> "保持良好的任务管理习惯，合理分配时间到各个象限。"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "合理规划您的任务，保持工作与生活的平衡。"
    )

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
                        // 获取 TaskRepository 实例
                        val achieveApp = application as com.yclin.achieveapp.AchieveApp
                        return DashboardViewModel(
                            userId = userId,
                            taskDao = taskDao,
                            habitDao = habitDao,
                            habitCompletionDao = habitCompletionDao,
                            taskRepository = achieveApp.taskRepository,
                            application = application
                        ) as T
                    }
                    throw IllegalArgumentException("未知的 ViewModel 类: ${modelClass.name}")
                }
            }
        }
    }
}

// 四象限统计数据类
data class QuadrantStats(
    val urgentImportantCount: Int = 0,
    val importantNotUrgentCount: Int = 0,
    val urgentNotImportantCount: Int = 0,
    val notUrgentNotImportantCount: Int = 0,
    val totalActiveTasks: Int = 0
) {
    val hasUrgentTasks: Boolean get() = urgentImportantCount > 0 || urgentNotImportantCount > 0
    val hasImportantTasks: Boolean get() = urgentImportantCount > 0 || importantNotUrgentCount > 0
    val isWellBalanced: Boolean get() = importantNotUrgentCount > urgentImportantCount && notUrgentNotImportantCount < 3
}
