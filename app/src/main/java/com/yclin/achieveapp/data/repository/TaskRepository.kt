package com.yclin.achieveapp.data.repository

import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.data.network.api.JsonServerApi
import com.yclin.achieveapp.data.network.model.RemoteTask
import com.yclin.achieveapp.data.network.model.toRemoteTask
import com.yclin.achieveapp.data.network.model.toTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    fun getAllTasks(userId: Long): Flow<List<Task>>
    fun getIncompleteTasks(userId: Long): Flow<List<Task>>
    fun getCompletedTasks(userId: Long): Flow<List<Task>>
    fun getTodayTasks(userId: Long): Flow<List<Task>>
    fun getOverdueTasks(userId: Long): Flow<List<Task>>
    fun getTasksByDueDate(userId: Long, date: LocalDate): Flow<List<Task>>
    fun getTasksByPriority(userId: Long, priority: Int): Flow<List<Task>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
    suspend fun syncTasks(userId: Long)
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val api: JsonServerApi
) : TaskRepository {

    override fun getAllTasks(userId: Long): Flow<List<Task>> = taskDao.getAllTasksFlow(userId)
    override fun getIncompleteTasks(userId: Long): Flow<List<Task>> = taskDao.getIncompleteTasksFlow(userId)
    override fun getCompletedTasks(userId: Long): Flow<List<Task>> = taskDao.getCompletedTasksFlow(userId)
    override fun getTodayTasks(userId: Long): Flow<List<Task>> = taskDao.getTodayTasksFlow(userId)
    override fun getOverdueTasks(userId: Long): Flow<List<Task>> = taskDao.getOverdueTasksFlow(userId)
    override fun getTasksByDueDate(userId: Long, date: LocalDate): Flow<List<Task>> =
        taskDao.getTasksByDueDateFlow(userId, date)
    override fun getTasksByPriority(userId: Long, priority: Int): Flow<List<Task>> =
        taskDao.getTasksByPriorityFlow(userId, priority)
    override suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)
    override suspend fun addTask(task: Task): Long = taskDao.insertTask(task)
    override suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    override suspend fun setTaskCompleted(taskId: Long, completed: Boolean) = taskDao.setTaskCompleted(taskId, completed)

    /**
     * 简单双向同步：先上传本地新增/变更任务，然后云端拉取覆盖本地
     */
    override suspend fun syncTasks(userId: Long) {
        // 1. 上传本地所有任务（如需更细粒度可做diff）
        val localTasks = taskDao.getAllTasks(userId)
        localTasks.forEach { task ->
            val remoteTask = task.toRemoteTask()
            if (task.id == 0L) {
                // 新建任务（本地新建未同步的建议id为0或null）
                api.addTask(remoteTask)
            } else {
                // 已有任务，尝试更新（如后端未找到则可add）
                try {
                    api.updateTask(task.id, remoteTask)
                } catch (e: Exception) {
                    // 处理404或失败等，可选降级为addTask
                }
            }
        }
        // 2. 拉取云端任务并覆盖本地
        val remoteTasks: List<RemoteTask> = api.getTasks(userId)
        val tasks = remoteTasks.map { it.toTask() }
        taskDao.replaceAllTasksByUser(userId, tasks)
    }
}