package com.yclin.achieveapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 任务实体类，表示用户需要完成的一次性任务
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 任务标题
    val title: String,

    // 任务描述（可选）
    val description: String = "",

    // 截止日期（可选）
    val dueDate: LocalDate? = null,

    // 优先级（1-高，2-中，3-低，0-无）
    val priority: Int = 0,

    // 任务是否已完成
    val isCompleted: Boolean = false,

    // 创建时间
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // 最后修改时间
    val updatedAt: LocalDateTime = LocalDateTime.now()
)