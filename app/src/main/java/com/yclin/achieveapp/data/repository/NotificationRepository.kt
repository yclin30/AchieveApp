package com.yclin.achieveapp.data.repository

import com.yclin.achieveapp.data.database.dao.HabitDao
import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.model.DailyNotificationData
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationRepository {
    suspend fun getTodayNotificationData(userId: Long): DailyNotificationData
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao
) : NotificationRepository {

    override suspend fun getTodayNotificationData(userId: Long): DailyNotificationData {
        // 获取今日未完成任务
        val todayTasks = taskDao.getTodayTasksFlow(userId).first()
            .filter { !it.isCompleted }

        // 获取今日需要执行的习惯
        val today = LocalDate.now().dayOfWeek
        val todayBit = 1 shl (today.value - 1)
        val todayHabits = habitDao.getHabitsForTodayFlow(userId, todayBit).first()

        return DailyNotificationData(
            taskCount = todayTasks.size,
            habitCount = todayHabits.size,
            taskTitles = todayTasks.map { it.title },
            habitTitles = todayHabits.map { it.name }
        )
    }
}