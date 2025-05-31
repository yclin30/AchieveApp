package com.yclin.achieveapp.data.model
import androidx.room.Embedded
import androidx.room.Relation
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.database.entity.HabitCompletion

/**
 * 表示一个习惯及其所有完成记录的关系类
 */
data class HabitWithCompletions(
    @Embedded
    val habit: Habit,

    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val completions: List<HabitCompletion> = emptyList()
)