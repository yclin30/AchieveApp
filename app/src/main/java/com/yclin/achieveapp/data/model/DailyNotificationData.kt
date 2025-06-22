package com.yclin.achieveapp.data.model

/**
 * 通知内容数据
 */
data class DailyNotificationData(
    val taskCount: Int,
    val habitCount: Int,
    val taskTitles: List<String>,
    val habitTitles: List<String>
) {
    fun getNotificationTitle(): String {
        return when {
            taskCount > 0 && habitCount > 0 -> "今日待办：$taskCount 个任务，$habitCount 个习惯"
            taskCount > 0 -> "今日待办：$taskCount 个任务"
            habitCount > 0 -> "今日待办：$habitCount 个习惯"
            else -> "今日无待办事项"
        }
    }

    fun getNotificationContent(): String {
        val items = mutableListOf<String>()

        if (taskTitles.isNotEmpty()) {
            items.add("任务：${taskTitles.take(3).joinToString("、")}")
            if (taskTitles.size > 3) {
                items[items.size - 1] += "等${taskTitles.size}项"
            }
        }

        if (habitTitles.isNotEmpty()) {
            items.add("习惯：${habitTitles.take(3).joinToString("、")}")
            if (habitTitles.size > 3) {
                items[items.size - 1] += "等${habitTitles.size}项"
            }
        }

        return if (items.isEmpty()) {
            "今天没有待办事项，享受轻松的一天吧！"
        } else {
            items.joinToString("\n")
        }
    }
}