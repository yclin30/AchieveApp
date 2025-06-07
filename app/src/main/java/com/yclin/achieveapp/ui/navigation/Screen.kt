package com.yclin.achieveapp.ui.navigation

/**
 * 定义应用中的所有屏幕导航路径
 */
sealed class Screen(val route: String) {
    // 底部导航项目
    object Dashboard : Screen("dashboard")
    object Tasks : Screen("tasks")
    object Habits : Screen("habits")

    // 任务详情
    object TaskDetail : Screen("task/{taskId}") {
        fun createRoute(taskId: Long): String = "task/$taskId"
    }

    // 任务新增/编辑
    object AddEditTask : Screen("task/edit/{taskId}") {
        // taskId 为 null 表示新建，否则为编辑
        fun createRoute(taskId: Long? = null): String =
            if (taskId != null) "task/edit/$taskId" else "task/edit/-1"
    }

    // 习惯详情
    object HabitDetail : Screen("habit/{habitId}") {
        fun createRoute(habitId: Long): String = "habit/$habitId"
    }

    // 习惯新增/编辑
    object AddEditHabit : Screen("habit/edit/{habitId}") {
        fun createRoute(habitId: Long? = null): String =
            if (habitId != null) "habit/edit/$habitId" else "habit/edit/-1"
    }

}