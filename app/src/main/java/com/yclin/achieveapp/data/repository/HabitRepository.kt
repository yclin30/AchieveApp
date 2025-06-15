package com.yclin.achieveapp.data.repository

import com.yclin.achieveapp.data.database.dao.HabitCompletionDao
import com.yclin.achieveapp.data.database.dao.HabitDao
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.database.entity.HabitCompletion
import com.yclin.achieveapp.data.model.HabitWithCompletions
import com.yclin.achieveapp.data.network.model.toRemoteHabit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import com.yclin.achieveapp.data.network.api.JsonServerApi
import com.yclin.achieveapp.data.network.model.toHabit


interface HabitRepository {
    fun getAllHabits(userId: Long): Flow<List<Habit>>
    fun getHabitsForToday(userId: Long): Flow<List<Habit>>
    suspend fun getHabitById(habitId: Long, userId: Long): Habit?
    fun getHabitWithCompletions(habitId: Long, userId: Long): Flow<HabitWithCompletions?>
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun isHabitCompletedOnDate(habitId: Long, date: LocalDate, userId: Long): Boolean
    suspend fun completeHabit(habitId: Long, date: LocalDate, userId: Long)
    suspend fun uncompleteHabit(habitId: Long, date: LocalDate, userId: Long)
    suspend fun getHabitCompletionsInRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long
    ): List<HabitCompletion>
    suspend fun updateHabitStreaks(habitId: Long, userId: Long)

    // 新增：同步相关
    suspend fun syncHabitsFromRemote(userId: Long)
    suspend fun syncHabitToRemote(habit: Habit)
    suspend fun syncDeleteHabitRemote(habit: Habit)
}
/**
 * HabitRepository接口的实现类
 */

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val remoteApi: JsonServerApi // 新增
) : HabitRepository {

    override fun getAllHabits(userId: Long): Flow<List<Habit>> =
        habitDao.getAllHabitsFlow(userId)

    override fun getHabitsForToday(userId: Long): Flow<List<Habit>> {
        val today = LocalDate.now().dayOfWeek
        val todayBit = 1 shl (today.value - 1)
        return habitDao.getHabitsForTodayFlow(userId, todayBit)
    }

    override suspend fun getHabitById(habitId: Long, userId: Long): Habit? =
        habitDao.getHabitById(habitId, userId)

    override fun getHabitWithCompletions(habitId: Long, userId: Long): Flow<HabitWithCompletions?> {
        return habitDao.getAllHabitsFlow(userId)
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

    override suspend fun addHabit(habit: Habit): Long {
        val id = habitDao.insertHabit(habit)
        try {
            syncHabitToRemote(habit.copy(id = id))
        } catch (_: Exception) { /* 忽略同步失败，后续可重试 */ }
        return id
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
        try {
            syncHabitToRemote(habit)
        } catch (_: Exception) {}
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
        try {
            syncDeleteHabitRemote(habit)
        } catch (_: Exception) {}
    }

    override suspend fun isHabitCompletedOnDate(habitId: Long, date: LocalDate, userId: Long): Boolean =
        habitCompletionDao.isHabitCompletedOnDate(habitId, date)

    override suspend fun completeHabit(habitId: Long, date: LocalDate, userId: Long) {
        habitCompletionDao.setHabitCompletionStatus(habitId, date, true)
        updateHabitStreaks(habitId, userId)
    }

    override suspend fun uncompleteHabit(habitId: Long, date: LocalDate, userId: Long) {
        habitCompletionDao.setHabitCompletionStatus(habitId, date, false)
        updateHabitStreaks(habitId, userId)
    }

    override suspend fun getHabitCompletionsInRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        userId: Long
    ): List<HabitCompletion> =
        habitCompletionDao.getCompletionsInRange(habitId, startDate, endDate)

    override suspend fun updateHabitStreaks(habitId: Long, userId: Long) {
        val habit = habitDao.getHabitById(habitId, userId) ?: return

        val today = LocalDate.now()
        val completions = habitCompletionDao.getCompletionsInRange(
            habitId,
            today.minusDays(30),
            today
        )

        val completedDates = completions.filter { it.isCompleted }
            .associate { it.date to true }

        var currentStreak = 0
        var dateToCheck = today

        while (true) {
            val shouldCheck = if (habit.frequencyType == 1) {
                true
            } else if (habit.frequencyType == 2) {
                val dayBit = 1 shl (dateToCheck.dayOfWeek.value - 1)
                (habit.weekDays and dayBit) != 0
            } else {
                false
            }

            if (shouldCheck && completedDates[dateToCheck] == true) {
                currentStreak++
                dateToCheck = dateToCheck.minusDays(1)
            } else if (shouldCheck) {
                break
            } else {
                dateToCheck = dateToCheck.minusDays(1)
            }

            if (dateToCheck.isBefore(today.minusDays(30))) {
                break
            }
        }

        val longestStreak = maxOf(habit.longestStreak, currentStreak)
        habitDao.updateHabitStreaks(habitId, userId, currentStreak, longestStreak)
    }

    // ======== 新增：同步远程API相关 ========

    override suspend fun syncHabitsFromRemote(userId: Long) {
        val remoteHabits = remoteApi.getHabits(userId)
            .map { it.toHabit() }
        habitDao.replaceAllHabitsByUser(userId, remoteHabits)
    }

    override suspend fun syncHabitToRemote(habit: Habit) {
        val remoteHabit = habit.toRemoteHabit()
        try {
            // 如果 habit.id 为 0，使用 POST，否则 PUT
            if (remoteHabit.id == 0L) {
                remoteApi.addHabit(remoteHabit)
            } else {
                remoteApi.updateHabit(remoteHabit.id, remoteHabit)
            }
        } catch (e: Exception) {
            // 可以记录日志或抛出
        }
    }

    override suspend fun syncDeleteHabitRemote(habit: Habit) {
        try {
            if (habit.id != 0L) {
                remoteApi.deleteHabit(habit.id)
            }
        } catch (e: Exception) {}
    }
}