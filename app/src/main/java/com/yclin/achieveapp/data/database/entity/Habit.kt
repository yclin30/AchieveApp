package com.yclin.achieveapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 习惯实体类，表示用户想要培养的重复性习惯
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Long = 0L, // 关联的用户ID，默认为0表示未关联
    // 习惯名称
    val name: String,

    // 习惯描述（可选）
    val description: String = "",

    // 频率类型（1-每日，2-每周特定几天）
    val frequencyType: Int,

    // 如果是每周特定几天，这是一个表示周几的位掩码
    // 例如：1(周一)=1, 2(周二)=2, 3(周三)=4, 4(周四)=8, 5(周五)=16, 6(周六)=32, 7(周日)=64
    // 如周一、三、五 = 1 + 4 + 16 = 21
    val weekDays: Int = 0,

    // 提醒时间（小时，24小时制）
    val reminderHour: Int? = null,

    // 提醒时间（分钟）
    val reminderMinute: Int? = null,

    // 当前连续天数
    val currentStreak: Int = 0,

    // 最长连续天数
    val longestStreak: Int = 0,

    // 创建时间
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // 最后修改时间
    val updatedAt: LocalDateTime = LocalDateTime.now()
)