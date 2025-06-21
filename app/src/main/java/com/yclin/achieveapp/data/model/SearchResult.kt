package com.yclin.achieveapp.data.model

import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.database.entity.Task
import java.time.LocalDate

/**
 * 搜索结果统一模型
 */
sealed class SearchResult {
    abstract val id: Long
    abstract val title: String
    abstract val description: String
    abstract val type: SearchResultType

    data class TaskResult(
        override val id: Long,
        override val title: String,
        override val description: String,
        val task: Task,
        val dueDate: LocalDate?,
        val isCompleted: Boolean,
        val quadrantName: String,
        val quadrantColor: androidx.compose.ui.graphics.Color
    ) : SearchResult() {
        override val type = SearchResultType.TASK
    }

    data class HabitResult(
        override val id: Long,
        override val title: String,
        override val description: String,
        val habit: Habit,
        val frequencyText: String,
        val currentStreak: Int
    ) : SearchResult() {
        override val type = SearchResultType.HABIT
    }
}

enum class SearchResultType {
    TASK, HABIT
}

/**
 * 搜索筛选选项
 */
data class SearchFilter(
    val type: SearchResultType? = null, // null = 全部
    val includeCompletedTasks: Boolean = false
)