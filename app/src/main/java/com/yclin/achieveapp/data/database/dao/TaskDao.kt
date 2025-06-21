package com.yclin.achieveapp.data.database.dao

import androidx.room.*
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.data.database.entity.QuadrantType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id AND deleted = 0")
    suspend fun getTaskById(id: Long): Task?

    // 基础查询
    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0 ORDER BY createdAt DESC")
    fun getAllTasksFlow(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 AND deleted = 0 ORDER BY createdAt DESC")
    fun getIncompleteTasksFlow(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 1 AND deleted = 0 ORDER BY updatedAt DESC")
    fun getCompletedTasksFlow(userId: Long): Flow<List<Task>>

    // 四象限查询
    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId AND isImportant = 1 AND isUrgent = 1 AND deleted = 0 
        ORDER BY 
            CASE WHEN isCompleted = 0 THEN 0 ELSE 1 END,
            CASE WHEN dueDate IS NOT NULL THEN dueDate ELSE '9999-12-31' END,
            createdAt DESC
    """)
    fun getUrgentAndImportantTasksFlow(userId: Long): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId AND isImportant = 1 AND isUrgent = 0 AND deleted = 0 
        ORDER BY 
            CASE WHEN isCompleted = 0 THEN 0 ELSE 1 END,
            CASE WHEN dueDate IS NOT NULL THEN dueDate ELSE '9999-12-31' END,
            createdAt DESC
    """)
    fun getImportantNotUrgentTasksFlow(userId: Long): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId AND isImportant = 0 AND isUrgent = 1 AND deleted = 0 
        ORDER BY 
            CASE WHEN isCompleted = 0 THEN 0 ELSE 1 END,
            CASE WHEN dueDate IS NOT NULL THEN dueDate ELSE '9999-12-31' END,
            createdAt DESC
    """)
    fun getUrgentNotImportantTasksFlow(userId: Long): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId AND isImportant = 0 AND isUrgent = 0 AND deleted = 0 
        ORDER BY 
            CASE WHEN isCompleted = 0 THEN 0 ELSE 1 END,
            CASE WHEN dueDate IS NOT NULL THEN dueDate ELSE '9999-12-31' END,
            createdAt DESC
    """)
    fun getNotUrgentNotImportantTasksFlow(userId: Long): Flow<List<Task>>

    // 任务操作
    @Query("UPDATE tasks SET isCompleted = :completed, updatedAt = :updateTime WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean, updateTime: String)

    @Query("""
        UPDATE tasks 
        SET isImportant = :isImportant, isUrgent = :isUrgent, updatedAt = :updateTime 
        WHERE id = :taskId
    """)
    suspend fun moveTaskToQuadrant(
        taskId: Long,
        isImportant: Boolean,
        isUrgent: Boolean,
        updateTime: String
    )

    // 软删除相关
    @Query("UPDATE tasks SET deleted = 1, updatedAt = :updateTime WHERE id = :taskId AND userId = :userId")
    suspend fun markTaskDeleted(taskId: Long, userId: Long, updateTime: String)

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 1")
    suspend fun getAllDeletedTasks(userId: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND deleted = 0")
    suspend fun getAllNotDeletedTasks(userId: Long): List<Task>

    @Query("DELETE FROM tasks WHERE userId = :userId AND deleted = 1")
    suspend fun deletePhysicallyDeletedTasks(userId: Long)

    // 同步相关
    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllTasksByUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<Task>)

    @Query("""
    SELECT * FROM tasks 
    WHERE userId = :userId 
    AND dueDate = :today 
    AND deleted = 0 
    ORDER BY 
        CASE WHEN isCompleted = 0 THEN 0 ELSE 1 END,
        CASE WHEN isImportant = 1 AND isUrgent = 1 THEN 0
             WHEN isImportant = 1 AND isUrgent = 0 THEN 1
             WHEN isImportant = 0 AND isUrgent = 1 THEN 2
             ELSE 3 END,
        createdAt DESC
""")

    fun getTodayTasksFlow(userId: Long, today: LocalDate = LocalDate.now()): Flow<List<Task>>
    @Transaction
    suspend fun replaceAllTasksByUser(userId: Long, newTasks: List<Task>) {
        deleteAllTasksByUser(userId)
        if (newTasks.isNotEmpty()) {
            insertAllTasks(newTasks)
        }
    }
}