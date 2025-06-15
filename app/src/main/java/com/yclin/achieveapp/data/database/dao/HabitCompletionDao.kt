package com.yclin.achieveapp.data.database.dao
import androidx.room.*
import com.yclin.achieveapp.data.database.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 习惯完成记录数据访问对象，提供对HabitCompletion表的CRUD操作
 */
@Dao
interface HabitCompletionDao {
    /**
     * 插入习惯完成记录
     * @param completion 要插入的完成记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    /**
     * 删除习惯完成记录
     * @param completion 要删除的完成记录
     */
    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)

    /**
     * 获取特定习惯的所有完成记录
     * @param habitId 习惯ID
     * @return 该习惯的所有完成记录Flow
     */
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabitFlow(habitId: Long): Flow<List<HabitCompletion>>

    /**
     * 获取特定习惯在指定日期的完成记录
     * @param habitId 习惯ID
     * @param date 指定日期
     * @return 完成记录（如果存在）
     */
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun getCompletionByDate(habitId: Long, date: LocalDate): HabitCompletion?

    /**
     * 获取特定习惯在指定日期范围内的所有完成记录
     * @param habitId 习惯ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 该日期范围内的完成记录列表
     */
    @Query("""
        SELECT * FROM habit_completions 
        WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
    """)
    suspend fun getCompletionsInRange(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<HabitCompletion>

    /**
     * 获取指定日期所有习惯的完成记录
     * @param date 指定日期
     * @return 该日期的所有完成记录列表
     */
    @Query("SELECT * FROM habit_completions WHERE date = :date")
    suspend fun getCompletionsByDate(date: LocalDate): List<HabitCompletion>

    /**
     * 检查习惯在指定日期是否已完成
     * @param habitId 习惯ID
     * @param date 指定日期
     * @return 如果已完成则为true，否则为false
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM habit_completions 
            WHERE habitId = :habitId AND date = :date AND isCompleted = 1
        )
    """)
    suspend fun isHabitCompletedOnDate(habitId: Long, date: LocalDate): Boolean

   /**
    * 根据习惯ID和日期删除完成记录
    * @param habitId 习惯ID
    * @param date 指定日期
    */
   @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
   suspend fun deleteCompletionByHabitIdAndDate(habitId: Long, date: LocalDate)
    /**
     * 标记习惯在指定日期为已完成或未完成
     * @param habitId 习惯ID
     * @param date 指定日期
     * @param isCompleted 完成状态
     */
    @Query("""
        INSERT OR REPLACE INTO habit_completions (habitId, date, isCompleted)
        VALUES (:habitId, :date, :isCompleted)
    """)
    suspend fun setHabitCompletionStatus(habitId: Long, date: LocalDate, isCompleted: Boolean)

    /**
     * 获取习惯最后一次完成的日期
     * @param habitId 习惯ID
     * @return 最后完成日期（如果有）
     */
    @Query("""
        SELECT date FROM habit_completions
        WHERE habitId = :habitId AND isCompleted = 1
        ORDER BY date DESC
        LIMIT 1
    """)
    suspend fun getLastCompletionDate(habitId: Long): LocalDate?

    /**
     * 批量获取指定日期这些习惯的完成记录
     * @param habitIds 习惯ID列表
     * @param date 日期
     * @return 这些习惯在该日期的完成记录
     */
    @Query("SELECT * FROM habit_completions WHERE habitId IN (:habitIds) AND date = :date")
    suspend fun getCompletionsByHabitIdsAndDate(habitIds: List<Long>, date: LocalDate): List<HabitCompletion>
}