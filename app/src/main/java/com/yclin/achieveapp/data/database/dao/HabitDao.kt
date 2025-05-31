package com.yclin.achieveapp.data.database.dao

import androidx.room.*
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.model.HabitWithCompletions
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 习惯数据访问对象，提供对Habit表的CRUD操作
 */
@Dao
interface HabitDao {
    /**
     * 插入新习惯
     * @param habit 要插入的习惯
     * @return 插入的习惯ID
     */
    @Insert
    suspend fun insertHabit(habit: Habit): Long

    /**
     * 更新习惯
     * @param habit 要更新的习惯
     */
    @Update
    suspend fun updateHabit(habit: Habit)

    /**
     * 删除习惯
     * @param habit 要删除的习惯
     */
    @Delete
    suspend fun deleteHabit(habit: Habit)

    /**
     * 根据ID获取习惯
     * @param id 习惯ID
     * @return 习惯对象（如果存在）
     */
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    /**
     * 获取所有习惯作为Flow
     * @return 所有习惯的Flow
     */
    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabitsFlow(): Flow<List<Habit>>

    /**
     * 获取今天需要完成的习惯
     * 对于每日习惯，每天都包括
     * 对于每周特定几天的习惯，只包括当天是设定的那几天的习惯
     * @return 今天需要完成的习惯列表Flow
     */
    @Query("""
        SELECT * FROM habits 
        WHERE frequencyType = 1 OR 
        (frequencyType = 2 AND (weekDays & :todayBit) != 0)
        ORDER BY name ASC
    """)
    fun getHabitsForTodayFlow(todayBit: Int): Flow<List<Habit>>

    /**
     * 更新习惯的连续天数信息
     * @param habitId 习惯ID
     * @param currentStreak 当前连续天数
     * @param longestStreak 最长连续天数
     */
    @Query("""
        UPDATE habits 
        SET currentStreak = :currentStreak, 
        longestStreak = :longestStreak,
        updatedAt = datetime('now')
        WHERE id = :habitId
    """)
    suspend fun updateHabitStreaks(habitId: Long, currentStreak: Int, longestStreak: Int)
}