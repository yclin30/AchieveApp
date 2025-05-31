package com.yclin.achieveapp.data.repository

import com.yclin.achieveapp.data.database.dao.HabitCompletionDao
import com.yclin.achieveapp.data.database.dao.HabitDao
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.database.entity.HabitCompletion
import com.yclin.achieveapp.data.model.HabitWithCompletions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 习惯仓库接口，定义习惯相关操作
 */
interface HabitRepository {
    /**
     * 获取所有习惯
     */
    fun getAllHabits(): Flow<List<Habit>>

    /**
     * 获取今天需要完成的习惯
     */
    fun getHabitsForToday(): Flow<List<Habit>>

    /**
     * 获取指定ID的习惯
     */
    suspend fun getHabitById(habitId: Long): Habit?

    /**
     * 获取习惯及其完成记录
     */
    fun getHabitWithCompletions(habitId: Long): Flow<HabitWithCompletions?>

    /**
     * 添加习惯
     */
    suspend fun addHabit(habit: Habit): Long

    /**
     * 更新习惯
     */
    suspend fun updateHabit(habit: Habit)

    /**
     * 删除习惯
     */
    suspend fun deleteHabit(habit: Habit)

    /**
     * 检查习惯在指定日期是否已完成
     */
    suspend fun isHabitCompletedOnDate(habitId: Long, date: LocalDate): Boolean

    /**
     * 标记习惯在指定日期为已完成
     */
    suspend fun completeHabit(habitId: Long, date: LocalDate)

    /**
     * 取消标记习惯在指定日期为已完成
     */
    suspend fun uncompleteHabit(habitId: Long, date: LocalDate)

    /**
     * 获取习惯在指定日期范围内的完成记录
     */
    suspend fun getHabitCompletionsInRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitCompletion>

    /**
     * 计算并更新习惯的连续天数
     */
    suspend fun updateHabitStreaks(habitId: Long)
}

/**
 * HabitRepository接口的实现类
 */
@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabitsFlow()

    override fun getHabitsForToday(): Flow<List<Habit>> {
        val today = LocalDate.now().dayOfWeek
        val todayBit = 1 shl (today.value - 1)
        return habitDao.getHabitsForTodayFlow(todayBit)
    }

    override suspend fun getHabitById(habitId: Long): Habit? = habitDao.getHabitById(habitId)

    override fun getHabitWithCompletions(habitId: Long): Flow<HabitWithCompletions?> {
        return habitDao.getAllHabitsFlow()
            .map { habits ->
                val habit = habits.find { it.id == habitId } ?: return@map null
                val completions = habitCompletionDao.getCompletionsForHabitFlow(habitId)
                    .map { it.sortedByDescending { completion -> completion.date } }

                HabitWithCompletions(
                    habit = habit,
                    completions = completions.firstOrNull() ?: emptyList()
                )
            }
    }

    override suspend fun addHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    override suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    override suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    override suspend fun isHabitCompletedOnDate(habitId: Long, date: LocalDate): Boolean =
        habitCompletionDao.isHabitCompletedOnDate(habitId, date)

    override suspend fun completeHabit(habitId: Long, date: LocalDate) {
        habitCompletionDao.setHabitCompletionStatus(habitId, date, true)
        updateHabitStreaks(habitId)
    }

    override suspend fun uncompleteHabit(habitId: Long, date: LocalDate) {
        habitCompletionDao.setHabitCompletionStatus(habitId, date, false)
        updateHabitStreaks(habitId)
    }

    override suspend fun getHabitCompletionsInRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitCompletion> =
        habitCompletionDao.getCompletionsInRange(habitId, startDate, endDate)

    override suspend fun updateHabitStreaks(habitId: Long) {
        val habit = habitDao.getHabitById(habitId) ?: return

        // 获取当前日期
        val today = LocalDate.now()

        // 获取最近30天的完成记录
        val completions = habitCompletionDao.getCompletionsInRange(
            habitId,
            today.minusDays(30),
            today
        )

        // 创建日期到完成状态的映射
        val completedDates = completions.filter { it.isCompleted }
            .associate { it.date to true }

        // 计算当前连续天数
        var currentStreak = 0
        var dateToCheck = today

        // 计算应该检查哪些日期
        while (true) {
            // 对于每日习惯，每天都需要检查
            val shouldCheck = if (habit.frequencyType == 1) {
                true
            }
            // 对于每周特定几天的习惯，只检查设定的那几天
            else if (habit.frequencyType == 2) {
                val dayBit = 1 shl (dateToCheck.dayOfWeek.value - 1)
                (habit.weekDays and dayBit) != 0
            } else {
                false
            }

            // 如果这一天需要检查且已完成，增加连续天数
            if (shouldCheck && completedDates[dateToCheck] == true) {
                currentStreak++
                dateToCheck = dateToCheck.minusDays(1)
            }
            // 如果这一天需要检查但未完成，停止计算
            else if (shouldCheck) {
                break
            }
            // 如果这一天不需要检查，继续检查前一天
            else {
                dateToCheck = dateToCheck.minusDays(1)
            }

            // 如果已经检查了30天，停止计算
            if (dateToCheck.isBefore(today.minusDays(30))) {
                break
            }
        }

        // 计算最长连续天数
        val longestStreak = maxOf(habit.longestStreak, currentStreak)

        // 更新数据库
        habitDao.updateHabitStreaks(habitId, currentStreak, longestStreak)
    }
}