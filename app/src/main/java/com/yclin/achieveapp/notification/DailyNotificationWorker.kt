package com.yclin.achieveapp.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailyNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = applicationContext as AchieveApp
            val notificationRepository = app.notificationRepository
            val notificationManager = AchieveNotificationManager(applicationContext)

            // 获取当前用户ID（你需要根据实际情况获取）
            val userId = getUserId()

            // 获取今日数据
            val notificationData = notificationRepository.getTodayNotificationData(userId)

            // 只有当有待办事项时才发送通知
            if (notificationData.taskCount > 0 || notificationData.habitCount > 0) {
                notificationManager.showDailyNotification(notificationData)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    // 你需要实现获取当前用户ID的逻辑
    private fun getUserId(): Long {
        // 从 SharedPreferences 或其他地方获取当前用户ID
        val sharedPref = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("current_user_id", 1L) // 默认用户ID为1
    }
}