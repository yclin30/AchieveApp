package com.yclin.achieveapp.ui.feature_dashboard

import com.yclin.achieveapp.data.database.entity.Habit
import java.time.LocalDate

data class HabitDashboardItem(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val today: LocalDate // 记录是哪一天的状态，用于打卡
)