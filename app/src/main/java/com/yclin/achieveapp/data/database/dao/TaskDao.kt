package com.yclin.achieveapp.data.database.dao

import androidx.room.*
import com.yclin.achieveapp.data.database.entity.Task
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

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    fun getAllTasksFlow(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    fun getIncompleteTasksFlow(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 1 ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    fun getCompletedTasksFlow(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND date(dueDate) = date(:date) ORDER BY priority ASC, createdAt DESC")
    fun getTasksByDueDateFlow(userId: Long, date: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND date(dueDate) = date('now') AND isCompleted = 0 ORDER BY priority ASC, createdAt DESC")
    fun getTodayTasksFlow(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND date(dueDate) < date('now') AND isCompleted = 0 ORDER BY dueDate ASC, priority ASC")
    fun getOverdueTasksFlow(userId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND priority = :priority ORDER BY dueDate ASC, createdAt DESC")
    fun getTasksByPriorityFlow(userId: Long, priority: Int): Flow<List<Task>>

    @Query("UPDATE tasks SET isCompleted = :completed, updatedAt = datetime('now') WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)

    // ========== 同步相关 ==========
    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllTasksByUser(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<Task>)

    @Transaction
    suspend fun replaceAllTasksByUser(userId: Long, newTasks: List<Task>) {
        deleteAllTasksByUser(userId)
        insertAllTasks(newTasks)
    }

    // 非Flow同步查询：获取本地全部任务（便于上传/同步用）
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    suspend fun getAllTasks(userId: Long): List<Task>
}