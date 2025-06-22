package com.yclin.achieveapp.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

class NotificationScheduler(private val context: Context) {

    companion object {
        const val DAILY_NOTIFICATION_WORK_NAME = "daily_notification_work"
    }

    /**
     * 启用每日通知
     */
    fun enableDailyNotification(hour: Int = 8, minute: Int = 0) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 如果设定时间已过，则安排到明天
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_NOTIFICATION_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
            )
    }

    /**
     * 禁用每日通知
     */
    fun disableDailyNotification() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(DAILY_NOTIFICATION_WORK_NAME)

        // 取消当前显示的通知
        AchieveNotificationManager(context).cancelDailyNotification()
    }

    /**
     * 立即发送测试通知
     */
    fun sendTestNotification() {
        val testWorkRequest = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .build()

        WorkManager.getInstance(context)
            .enqueue(testWorkRequest)
    }
}