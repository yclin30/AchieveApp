package com.yclin.achieveapp.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

/**
 * 习惯完成记录实体类，记录用户每次完成习惯的日期
 */
@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class HabitCompletion(
    // 关联的习惯ID
    val habitId: Long,

    // 完成日期
    val date: LocalDate,

    // 是否已完成
    val isCompleted: Boolean = true
)