package com.yclin.achieveapp.data.repository


import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.database.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务仓库接口，定义任务相关操作
 */
interface TaskRepository {
    /**
     * 获取所有任务
     */
    fun getAllTasks(): Flow<List<Task>>

    /**
     * 获取未完成的任务
     */
    fun getIncompleteTasks(): Flow<List<Task>>

    /**
     * 获取已完成的任务
     */
    fun getCompletedTasks(): Flow<List<Task>>

    /**
     * 获取今天到期的任务
     */
    fun getTodayTasks(): Flow<List<Task>>

    /**
     * 获取逾期的任务
     */
    fun getOverdueTasks(): Flow<List<Task>>

    /**
     * 获取指定日期的任务
     */
    fun getTasksByDueDate(date: LocalDate): Flow<List<Task>>

    /**
     * 获取指定优先级的任务
     */
    fun getTasksByPriority(priority: Int): Flow<List<Task>>

    /**
     * 获取指定ID的任务
     */
    suspend fun getTaskById(taskId: Long): Task?

    /**
     * 添加任务
     */
    suspend fun addTask(task: Task): Long

    /**
     * 更新任务
     */
    suspend fun updateTask(task: Task)

    /**
     * 删除任务
     */
    suspend fun deleteTask(task: Task)

    /**
     * 设置任务完成状态
     */
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
}

/**
 * TaskRepository接口的实现类
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasksFlow()

    override fun getIncompleteTasks(): Flow<List<Task>> = taskDao.getIncompleteTasksFlow()

    override fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasksFlow()

    override fun getTodayTasks(): Flow<List<Task>> = taskDao.getTodayTasksFlow()

    override fun getOverdueTasks(): Flow<List<Task>> = taskDao.getOverdueTasksFlow()

    override fun getTasksByDueDate(date: LocalDate): Flow<List<Task>> =
        taskDao.getTasksByDueDateFlow(date)

    override fun getTasksByPriority(priority: Int): Flow<List<Task>> =
        taskDao.getTasksByPriorityFlow(priority)

    override suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    override suspend fun addTask(task: Task): Long = taskDao.insertTask(task)

    override suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    override suspend fun setTaskCompleted(taskId: Long, completed: Boolean) =
        taskDao.setTaskCompleted(taskId, completed)
}