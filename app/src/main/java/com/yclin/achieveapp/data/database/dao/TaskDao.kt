package com.yclin.achieveapp.data.database.dao

import androidx.room.*
import com.yclin.achieveapp.data.database.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 任务数据访问对象，提供对Task表的CRUD操作
 */
@Dao
interface TaskDao {
    /**
     * 插入新任务
     * @param task 要插入的任务
     * @return 插入的任务ID
     */
    @Insert
    suspend fun insertTask(task: Task): Long

    /**
     * 更新任务
     * @param task 要更新的任务
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * 删除任务
     * @param task 要删除的任务
     */
    @Delete
    suspend fun deleteTask(task: Task)

    /**
     * 根据ID获取任务
     * @param id 任务ID
     * @return 任务对象（如果存在）
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    /**
     * 获取所有任务作为Flow
     * @return 所有任务的Flow
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    fun getAllTasksFlow(): Flow<List<Task>>

    /**
     * 获取所有未完成的任务作为Flow
     * @return 未完成任务的Flow
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    fun getIncompleteTasksFlow(): Flow<List<Task>>

    /**
     * 获取所有已完成的任务作为Flow
     * @return 已完成任务的Flow
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY dueDate ASC, priority ASC, createdAt DESC")
    fun getCompletedTasksFlow(): Flow<List<Task>>

    /**
     * 获取截止日期为指定日期的所有任务
     * @param date 指定日期
     * @return 该日期的任务列表Flow
     */
    @Query("SELECT * FROM tasks WHERE date(dueDate) = date(:date) ORDER BY priority ASC, createdAt DESC")
    fun getTasksByDueDateFlow(date: LocalDate): Flow<List<Task>>

    /**
     * 获取今天到期的任务
     * @return 今天到期的任务列表Flow
     */
    @Query("SELECT * FROM tasks WHERE date(dueDate) = date('now') AND isCompleted = 0 ORDER BY priority ASC, createdAt DESC")
    fun getTodayTasksFlow(): Flow<List<Task>>

    /**
     * 获取逾期未完成的任务
     * @return 逾期任务列表Flow
     */
    @Query("SELECT * FROM tasks WHERE date(dueDate) < date('now') AND isCompleted = 0 ORDER BY dueDate ASC, priority ASC")
    fun getOverdueTasksFlow(): Flow<List<Task>>

    /**
     * 根据优先级获取任务
     * @param priority 优先级
     * @return 指定优先级的任务列表Flow
     */
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY dueDate ASC, createdAt DESC")
    fun getTasksByPriorityFlow(priority: Int): Flow<List<Task>>

    /**
     * 将任务标记为已完成
     * @param taskId 任务ID
     * @param completed 完成状态
     */
    @Query("UPDATE tasks SET isCompleted = :completed, updatedAt = datetime('now') WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
}