package com.yclin.achieveapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 任务实体类，支持四象限管理（重要性 x 紧急性）
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0L,

    // 基本信息
    val title: String,
    val description: String = "",
    val dueDate: LocalDate? = null,

    // 四象限分类
    val isImportant: Boolean = false,  // 是否重要
    val isUrgent: Boolean = false,     // 是否紧急

    // 状态
    val isCompleted: Boolean = false,
    val deleted: Boolean = false,

    // 时间戳
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 获取任务所属的象限
     */
    val quadrant: QuadrantType
        get() = when {
            isImportant && isUrgent -> QuadrantType.URGENT_IMPORTANT
            isImportant && !isUrgent -> QuadrantType.IMPORTANT_NOT_URGENT
            !isImportant && isUrgent -> QuadrantType.URGENT_NOT_IMPORTANT
            else -> QuadrantType.NOT_URGENT_NOT_IMPORTANT
        }
}

/**
 * 四象限类型枚举
 */
enum class QuadrantType(
    val displayName: String,
    val description: String
) {
    URGENT_IMPORTANT("重要且紧急", "危机处理，立即行动"),
    IMPORTANT_NOT_URGENT("重要不紧急", "计划安排，重点投入"),
    URGENT_NOT_IMPORTANT("紧急不重要", "减少干扰，学会拒绝"),
    NOT_URGENT_NOT_IMPORTANT("不重要不紧急", "娱乐放松，适度即可")
}