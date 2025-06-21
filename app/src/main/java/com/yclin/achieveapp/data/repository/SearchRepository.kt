package com.yclin.achieveapp.data.repository

import androidx.compose.ui.graphics.Color
import com.yclin.achieveapp.data.database.dao.HabitDao
import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.database.entity.QuadrantType
import com.yclin.achieveapp.data.model.SearchFilter
import com.yclin.achieveapp.data.model.SearchResult
import com.yclin.achieveapp.data.model.SearchResultType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

interface SearchRepository {
    fun searchAll(userId: Long, query: String, filter: SearchFilter): Flow<List<SearchResult>>
}

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao
) : SearchRepository {

    override fun searchAll(userId: Long, query: String, filter: SearchFilter): Flow<List<SearchResult>> {
        if (query.isBlank()) {
            return flowOf(emptyList())
        }

        val taskFlow = when (filter.type) {
            SearchResultType.HABIT -> flowOf(emptyList())
            else -> searchTasks(userId, query, filter.includeCompletedTasks)
        }

        val habitFlow = when (filter.type) {
            SearchResultType.TASK -> flowOf(emptyList())
            else -> searchHabits(userId, query)
        }

        return combine(taskFlow, habitFlow) { tasks, habits ->
            (tasks + habits).sortedBy { it.title.lowercase() }
        }
    }

    private fun searchTasks(userId: Long, query: String, includeCompleted: Boolean): Flow<List<SearchResult.TaskResult>> {
        val searchQuery = "%$query%"
        return if (includeCompleted) {
            taskDao.searchAllTasksFlow(userId, searchQuery)
        } else {
            taskDao.searchUncompletedTasksFlow(userId, searchQuery)
        }.let { flow ->
            combine(flow, flowOf(Unit)) { tasks, _ ->
                tasks.map { task ->
                    // 🔧 修复：根据标志位确定象限类型
                    val quadrant = getQuadrantType(task.isImportant, task.isUrgent)
                    SearchResult.TaskResult(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        task = task,
                        dueDate = task.dueDate,
                        isCompleted = task.isCompleted,
                        quadrantName = getQuadrantDisplayName(quadrant),
                        quadrantColor = getQuadrantColor(quadrant)
                    )
                }
            }
        }
    }

    private fun searchHabits(userId: Long, query: String): Flow<List<SearchResult.HabitResult>> {
        val searchQuery = "%$query%"
        return habitDao.searchHabitsFlow(userId, searchQuery).let { flow ->
            combine(flow, flowOf(Unit)) { habits, _ ->
                habits.map { habit ->
                    SearchResult.HabitResult(
                        id = habit.id,
                        title = habit.name,
                        description = habit.description,
                        habit = habit,
                        frequencyText = getFrequencyText(habit.frequencyType, habit.weekDays),
                        currentStreak = habit.currentStreak
                    )
                }
            }
        }
    }

    // ✅ 新增：根据标志位确定象限类型
    private fun getQuadrantType(isImportant: Boolean, isUrgent: Boolean): QuadrantType {
        return when {
            isImportant && isUrgent -> QuadrantType.URGENT_IMPORTANT
            isImportant && !isUrgent -> QuadrantType.IMPORTANT_NOT_URGENT
            !isImportant && isUrgent -> QuadrantType.URGENT_NOT_IMPORTANT
            else -> QuadrantType.NOT_URGENT_NOT_IMPORTANT
        }
    }

    // ✅ 新增：获取象限显示名称
    private fun getQuadrantDisplayName(quadrant: QuadrantType): String {
        return when (quadrant) {
            QuadrantType.URGENT_IMPORTANT -> "第一象限"
            QuadrantType.IMPORTANT_NOT_URGENT -> "第二象限"
            QuadrantType.URGENT_NOT_IMPORTANT -> "第三象限"
            QuadrantType.NOT_URGENT_NOT_IMPORTANT -> "第四象限"
        }
    }

    private fun getQuadrantColor(quadrant: QuadrantType): Color {
        return when (quadrant) {
            QuadrantType.URGENT_IMPORTANT -> Color(0xFFFF5252) // 红色
            QuadrantType.IMPORTANT_NOT_URGENT -> Color(0xFF2196F3) // 蓝色
            QuadrantType.URGENT_NOT_IMPORTANT -> Color(0xFFFF9800) // 橙色
            QuadrantType.NOT_URGENT_NOT_IMPORTANT -> Color(0xFF4CAF50) // 绿色
        }
    }

    private fun getFrequencyText(frequencyType: Int, weekDays: Int): String {
        return when (frequencyType) {
            1 -> "每日"
            2 -> {
                val days = mutableListOf<String>()
                val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
                for (i in 0..6) {
                    if ((weekDays and (1 shl i)) != 0) {
                        days.add("周${dayNames[i]}")
                    }
                }
                if (days.size == 7) "每日" else days.joinToString("、")
            }
            else -> "自定义"
        }
    }
}