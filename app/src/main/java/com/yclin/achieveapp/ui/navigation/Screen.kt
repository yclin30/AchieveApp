package com.yclin.achieveapp.ui.navigation

/**
 * 定义应用中的所有屏幕导航路径
 */
sealed class Screen(val route: String) {
    // 底部导航项目
    object Dashboard : Screen("dashboard")
    object Tasks : Screen("tasks")
    object Habits : Screen("habits")

    // 后续可添加详情/编辑页面
    object TaskDetail : Screen("task/{taskId}") {
        fun createRoute(taskId: Long): String = "task/$taskId"
    }

    object AddEditTask : Screen("task/edit?taskId={taskId}") {
        fun createRoute(taskId: Long? = null): String =
            if (taskId != null) "task/edit?taskId=$taskId" else "task/edit"
    }

    object HabitDetail : Screen("habit/{habitId}") {
        fun createRoute(habitId: Long): String = "habit/$habitId"
    }

    object AddEditHabit : Screen("habit/edit?habitId={habitId}") {
        fun createRoute(habitId: Long? = null): String =
            if (habitId != null) "habit/edit?habitId=$habitId" else "habit/edit"
    }
}