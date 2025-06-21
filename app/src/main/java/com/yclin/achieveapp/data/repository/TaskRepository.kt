package com.yclin.achieveapp.data.repository

import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.database.entity.Task
import com.yclin.achieveapp.data.database.entity.QuadrantType
import com.yclin.achieveapp.data.network.api.JsonServerApi
import com.yclin.achieveapp.data.network.model.toRemoteTask
import com.yclin.achieveapp.data.network.model.toTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    // 基础查询
    fun getAllTasks(userId: Long): Flow<List<Task>>
    fun getIncompleteTasks(userId: Long): Flow<List<Task>>
    fun getCompletedTasks(userId: Long): Flow<List<Task>>

    // 四象限查询
    fun getUrgentAndImportantTasks(userId: Long): Flow<List<Task>>
    fun getImportantNotUrgentTasks(userId: Long): Flow<List<Task>>
    fun getUrgentNotImportantTasks(userId: Long): Flow<List<Task>>
    fun getNotUrgentNotImportantTasks(userId: Long): Flow<List<Task>>

    // 任务操作
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
    suspend fun moveTaskToQuadrant(taskId: Long, quadrant: QuadrantType)

    // 同步相关
    suspend fun syncTasksFromRemote(userId: Long)
    suspend fun syncTaskToRemote(task: Task)
    suspend fun safeSyncTasksToCloud(userId: Long)
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val api: JsonServerApi
) : TaskRepository {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    // 基础查询
    override fun getAllTasks(userId: Long): Flow<List<Task>> =
        taskDao.getAllTasksFlow(userId)

    override fun getIncompleteTasks(userId: Long): Flow<List<Task>> =
        taskDao.getIncompleteTasksFlow(userId)

    override fun getCompletedTasks(userId: Long): Flow<List<Task>> =
        taskDao.getCompletedTasksFlow(userId)

    // 四象限查询
    override fun getUrgentAndImportantTasks(userId: Long): Flow<List<Task>> =
        taskDao.getUrgentAndImportantTasksFlow(userId)

    override fun getImportantNotUrgentTasks(userId: Long): Flow<List<Task>> =
        taskDao.getImportantNotUrgentTasksFlow(userId)

    override fun getUrgentNotImportantTasks(userId: Long): Flow<List<Task>> =
        taskDao.getUrgentNotImportantTasksFlow(userId)

    override fun getNotUrgentNotImportantTasks(userId: Long): Flow<List<Task>> =
        taskDao.getNotUrgentNotImportantTasksFlow(userId)

    // 任务操作
    override suspend fun getTaskById(taskId: Long): Task? =
        taskDao.getTaskById(taskId)

    override suspend fun addTask(task: Task): Long {
        val currentTime = LocalDateTime.now().format(dateTimeFormatter)
        val taskWithTime = task.copy(
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val id = taskDao.insertTask(taskWithTime)

        // 异步同步到云端
        try {
            syncTaskToRemote(taskWithTime.copy(id = id))
        } catch (_: Exception) {
            // 同步失败不影响本地操作
        }

        return id
    }

    override suspend fun updateTask(task: Task) {
        val taskWithTime = task.copy(updatedAt = LocalDateTime.now())
        taskDao.updateTask(taskWithTime)

        try {
            syncTaskToRemote(taskWithTime)
        } catch (_: Exception) {
            // 同步失败不影响本地操作
        }
    }

    override suspend fun deleteTask(task: Task) {
        val currentTime = LocalDateTime.now().format(dateTimeFormatter)
        taskDao.markTaskDeleted(task.id, task.userId, currentTime)
    }

    override suspend fun setTaskCompleted(taskId: Long, completed: Boolean) {
        val currentTime = LocalDateTime.now().format(dateTimeFormatter)
        taskDao.setTaskCompleted(taskId, completed, currentTime)
    }

    override suspend fun moveTaskToQuadrant(taskId: Long, quadrant: QuadrantType) {
        val currentTime = LocalDateTime.now().format(dateTimeFormatter)
        when (quadrant) {
            QuadrantType.URGENT_IMPORTANT -> {
                taskDao.moveTaskToQuadrant(taskId, true, true, currentTime)
            }
            QuadrantType.IMPORTANT_NOT_URGENT -> {
                taskDao.moveTaskToQuadrant(taskId, true, false, currentTime)
            }
            QuadrantType.URGENT_NOT_IMPORTANT -> {
                taskDao.moveTaskToQuadrant(taskId, false, true, currentTime)
            }
            QuadrantType.NOT_URGENT_NOT_IMPORTANT -> {
                taskDao.moveTaskToQuadrant(taskId, false, false, currentTime)
            }
        }

        // 同步移动操作
        try {
            val task = taskDao.getTaskById(taskId)
            task?.let { syncTaskToRemote(it) }
        } catch (_: Exception) {
            // 同步失败不影响本地操作
        }
    }

    // 同步方法（简化实现）
    override suspend fun syncTasksFromRemote(userId: Long) {
        try {
            val remoteTasks = api.getTasks(userId).map { it.toTask() }
            if (remoteTasks.isNotEmpty()) {
                taskDao.replaceAllTasksByUser(userId, remoteTasks)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun syncTaskToRemote(task: Task) {
        try {
            val remoteTask = task.toRemoteTask()
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
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun safeSyncTasksToCloud(userId: Long) {
        // 同步已删除的任务
        val deletedTasks = taskDao.getAllDeletedTasks(userId)
        for (task in deletedTasks) {
            try {
                if (task.id != 0L) {
                    api.deleteTask(task.id)
                }
            } catch (_: Exception) {
                // 忽略删除失败
            }
        }

        // 同步未删除的任务
        val notDeletedTasks = taskDao.getAllNotDeletedTasks(userId)
        for (task in notDeletedTasks) {
            try {
                syncTaskToRemote(task)
            } catch (_: Exception) {
                // 继续同步其他任务
            }
        }

        // 清理已删除的任务
        taskDao.deletePhysicallyDeletedTasks(userId)
    }

    private fun isNotFoundError(e: Exception): Boolean {
        return e is retrofit2.HttpException && e.code() == 404
    }
}