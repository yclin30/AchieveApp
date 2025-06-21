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

    // ========= 关键：加 deleted = 0 =========
    @Query("SELECT * FROM habits WHERE userId = :userId AND deleted = 0 ORDER BY name ASC")
    fun getAllHabitsFlow(userId: Long): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND deleted = 0 ORDER BY name ASC")
    suspend fun getAllHabits(userId: Long): List<Habit>

    @Query("""
        SELECT * FROM habits 
        WHERE userId = :userId AND deleted = 0 AND
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


    // ===== 软删除相关 =====
    @Query("SELECT * FROM habits WHERE userId = :userId AND deleted = 1")
    suspend fun getAllDeletedHabits(userId: Long): List<Habit>

    @Query("UPDATE habits SET deleted = 1 WHERE id = :habitId AND userId = :userId")
    suspend fun markHabitDeleted(habitId: Long, userId: Long)

    // ====== 新增：同步相关 ======
    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun deleteAllHabitsByUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabits(habits: List<Habit>)

    @Transaction
    suspend fun replaceAllHabitsByUser(userId: Long, newHabits: List<Habit>) {
        deleteAllHabitsByUser(userId)
        if (newHabits.isNotEmpty()) {
            insertAllHabits(newHabits)
        }
    }
    @Query("SELECT * FROM habits WHERE userId = :userId AND deleted = 0 ORDER BY name ASC")
    suspend fun getAllNotDeletedHabits(userId: Long): List<Habit>

    // 在 HabitDao 中添加以下方法：

    @Query("""
    SELECT * FROM habits 
    WHERE userId = :userId 
    AND (name LIKE :query OR description LIKE :query)
    AND deleted = 0
    ORDER BY name ASC
""")
    fun searchHabitsFlow(userId: Long, query: String): Flow<List<Habit>>
}