package com.yclin.achieveapp.ui.navigation

sealed class Screen(val route: String) {
    // 底部导航项目
    object Dashboard : Screen("dashboard")
    object Tasks : Screen("tasks")
    object Habits : Screen("habits")
    object Profile : Screen("profile")
    object Search : Screen("search")
    object NotificationSettings : Screen("notification_settings")

    // 任务新增/编辑
    object AddEditTask : Screen("task/edit") {
        const val TASK_ID_ARG = "taskId"
        const val ROUTE_WITH_ARG = "task/edit/{$TASK_ID_ARG}"

        fun createRoute(taskId: Long? = null): String =
            if (taskId != null && taskId != -1L) "task/edit/$taskId" else "task/edit/-1"
    }

    // 习惯新增/编辑
    object AddEditHabit : Screen("habit/edit") {
        const val HABIT_ID_ARG = "habitId"
        const val ROUTE_WITH_ARG = "habit/edit/{$HABIT_ID_ARG}"

        fun createRoute(habitId: Long? = null): String =
            if (habitId != null && habitId != -1L) "habit/edit/$habitId" else "habit/edit/-1"
    }

    // 习惯详情
    object HabitDetail : Screen("habit/{habitId}") {
        fun createRoute(habitId: Long): String = "habit/$habitId"
    }

    // 认证相关
    object Login : Screen("login")
    object Register : Screen("register")


}