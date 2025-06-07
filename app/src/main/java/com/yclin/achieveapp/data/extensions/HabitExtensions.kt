// com/yclin/achieveapp/data/extensions/HabitExtensions.kt
package com.yclin.achieveapp.data.extensions

import com.yclin.achieveapp.data.database.entity.Habit
import java.time.LocalDate

fun Habit.shouldCheckOnDate(date: LocalDate): Boolean {
    return when (frequencyType) {
        1 -> true
        2 -> {
            val dayBit = 1 shl (date.dayOfWeek.value - 1)
            (weekDays and dayBit) != 0
        }
        else -> false
    }
}

fun Habit.getFrequencyDescription(): String {
    return when (frequencyType) {
        1 -> "每日"
        2 -> "每周 ${weekDaysToString(weekDays)}"
        else -> "自定义"
    }
}

private fun weekDaysToString(weekDays: Int): String {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")
    return days.withIndex()
        .filter { (i, _) -> weekDays and (1 shl i) != 0 }
        .joinToString("、") { it.value }
}