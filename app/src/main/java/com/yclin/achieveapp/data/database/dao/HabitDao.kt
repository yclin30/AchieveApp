package com.yclin.achieveapp.data.database.dao

import androidx.room.*
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.model.HabitWithCompletions
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE id = :id AND userId = :userId")
    suspend fun getHabitById(id: Long, userId: Long): Habit?

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY name ASC")
    fun getAllHabitsFlow(userId: Long): Flow<List<Habit>>

    // 新增：获取所有习惯（非 Flow），用于同步
    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY name ASC")
    suspend fun getAllHabits(userId: Long): List<Habit>

    @Query("""
        SELECT * FROM habits 
        WHERE userId = :userId AND 
              (frequencyType = 1 OR (frequencyType = 2 AND (weekDays & :todayBit) != 0))
        ORDER BY name ASC
    """)
    fun getHabitsForTodayFlow(userId: Long, todayBit: Int): Flow<List<Habit>>

    @Query("""
        UPDATE habits 
        SET currentStreak = :currentStreak, 
            longestStreak = :longestStreak,
            updatedAt = datetime('now')
        WHERE id = :habitId AND userId = :userId
    """)
    suspend fun updateHabitStreaks(habitId: Long, userId: Long, currentStreak: Int, longestStreak: Int)

    // ====== 新增：同步相关 ======
    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun deleteAllHabitsByUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabits(habits: List<Habit>)

    @Transaction
    suspend fun replaceAllHabitsByUser(userId: Long, newHabits: List<Habit>) {
        deleteAllHabitsByUser(userId)
        insertAllHabits(newHabits)
    }
}