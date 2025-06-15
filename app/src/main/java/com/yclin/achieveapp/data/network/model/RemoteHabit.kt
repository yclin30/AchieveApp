package com.yclin.achieveapp.data.network.model

import com.yclin.achieveapp.data.database.entity.Habit
import java.time.LocalDateTime


data class RemoteHabit(
    val id: Long = 0,
    val userId: Long = 0L,
    val name: String,
    val description: String = "",
    val frequencyType: Int,
    val weekDays: Int = 0,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
fun Habit.toRemoteHabit(): RemoteHabit = RemoteHabit(
    id = id,
    userId = userId,
    name = name,
    description = description,
    frequencyType = frequencyType,
    weekDays = weekDays,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

fun RemoteHabit.toHabit(): Habit = Habit(
    id = id,
    userId = userId,
    name = name,
    description = description,
    frequencyType = frequencyType,
    weekDays = weekDays,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt)
)