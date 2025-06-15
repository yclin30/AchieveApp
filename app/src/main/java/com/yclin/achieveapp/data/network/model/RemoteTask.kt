package com.yclin.achieveapp.data.network.model

import com.yclin.achieveapp.data.database.entity.Task
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 远程任务数据模型，对应网络接口的Task结构
 */
data class RemoteTask(
    val id: Long? = null,
    val userId: Long = 0L,
    val title: String,
    val description: String = "",
    val dueDate: String? = null,           // 使用String以便序列化，格式如"2025-06-15"
    val priority: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: String? = null,         // 使用String（如"2025-06-15T11:14:15"）
    val updatedAt: String? = null
)


fun Task.toRemoteTask(): RemoteTask = RemoteTask(
    id = if (this.id == 0L) null else this.id,
    userId = this.userId,
    title = this.title,
    description = this.description,
    dueDate = this.dueDate?.toString(),
    priority = this.priority,
    isCompleted = this.isCompleted,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)

fun RemoteTask.toTask(): Task = Task(
    id = this.id ?: 0L,
    userId = this.userId,
    title = this.title,
    description = this.description,
    dueDate = this.dueDate?.let { LocalDate.parse(it) },
    priority = this.priority,
    isCompleted = this.isCompleted,
    createdAt = this.createdAt?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now(),
    updatedAt = this.updatedAt?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now()
)