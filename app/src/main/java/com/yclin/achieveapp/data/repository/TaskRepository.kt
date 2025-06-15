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
    suspend fun deleteTask(task: Task)       // 变为软删除
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)

    // 同步相关
    suspend fun syncTasksFromRemote(userId: Long)
    suspend fun syncTaskToRemote(task: Task)
    suspend fun syncDeleteTaskRemote(task: Task)
    suspend fun safeSyncTasksToCloud(userId: Long)
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

    override suspend fun addTask(task: Task): Long {
        val id = taskDao.insertTask(task)
        try {
            syncTaskToRemote(task.copy(id = id))
        } catch (_: Exception) { }
        return id
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
        try {
            syncTaskToRemote(task)
        } catch (_: Exception) {}
    }

    // 软删除
    override suspend fun deleteTask(task: Task) {
        taskDao.markTaskDeleted(task.id, task.userId)
        // 不立即物理删除
    }

    override suspend fun setTaskCompleted(taskId: Long, completed: Boolean) {
        taskDao.setTaskCompleted(taskId, completed)
        // 可选：同步到云端
    }

    // ========== 同步相关 ==========

    override suspend fun syncTasksFromRemote(userId: Long) {
        val remoteTasks = api.getTasks(userId).map { it.toTask() }
        if (remoteTasks.isEmpty()) return
        taskDao.replaceAllTasksByUser(userId, remoteTasks)
    }

    override suspend fun syncTaskToRemote(task: Task) {
        val remoteTask = task.toRemoteTask()
        try {
            if (remoteTask.id == 0L) {
                api.addTask(remoteTask)
            } else {
                try {
                    remoteTask.id?.let { api.updateTask(it, remoteTask) }
                } catch (e: Exception) {
                    if (isNotFoundError(e)) {
                        api.addTask(remoteTask)
                    } else {
                        throw e
                    }
                }
            }
        } catch (e: Exception) {}
    }

    override suspend fun syncDeleteTaskRemote(task: Task) {
        try {
            if (task.id != 0L) {
                api.deleteTask(task.id)
            }
        } catch (e: Exception) {}
    }

    override suspend fun safeSyncTasksToCloud(userId: Long) {
        // 1. 先同步本地已删除的任务到云端
        val deletedTasks = taskDao.getAllDeletedTasks(userId)
        for (task in deletedTasks) {
            try {
                syncDeleteTaskRemote(task)
            } catch (_: Exception) {}
            // 可选：同步后本地物理删除
            // taskDao.deleteTask(task)
        }
        // 2. 再同步未删除的任务到云端
        val notDeletedTasks = taskDao.getAllNotDeletedTasks(userId)
        for (task in notDeletedTasks) {
            syncTaskToRemote(task)
        }
    }

    private fun isNotFoundError(e: Exception): Boolean {
        return e is retrofit2.HttpException && e.code() == 404
    }
}