package com.yclin.achieveapp.data.network.model

import com.yclin.achieveapp.data.database.entity.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class RemoteTask(
    val id: Long? = null,
    val userId: Long,
    val title: String,
    val description: String = "",
    val dueDate: String? = null, // ISO date format
    val isImportant: Boolean = false,
    val isUrgent: Boolean = false,
    val isCompleted: Boolean = false,
    val deleted: Boolean = false,
    val createdAt: String, // ISO datetime format
    val updatedAt: String  // ISO datetime format
)

fun RemoteTask.toTask(): Task {
    return Task(
        id = this.id ?: 0L,
        userId = this.userId,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate?.let { LocalDate.parse(it) },
        isImportant = this.isImportant,
        isUrgent = this.isUrgent,
        isCompleted = this.isCompleted,
        deleted = this.deleted,
        createdAt = LocalDateTime.parse(this.createdAt),
        updatedAt = LocalDateTime.parse(this.updatedAt)
    )
}

fun Task.toRemoteTask(): RemoteTask {
    return RemoteTask(
        id = if (this.id == 0L) null else this.id,
        userId = this.userId,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate?.toString(),
        isImportant = this.isImportant,
        isUrgent = this.isUrgent,
        isCompleted = this.isCompleted,
        deleted = this.deleted,
        createdAt = this.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        updatedAt = this.updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}