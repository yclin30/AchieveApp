package com.yclin.achieveapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yclin.achieveapp.MainActivity
import com.yclin.achieveapp.R
import com.yclin.achieveapp.data.model.DailyNotificationData

class AchieveNotificationManager(private val context: Context) {

    companion object {
        const val DAILY_NOTIFICATION_CHANNEL_ID = "daily_notification"
        const val DAILY_NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DAILY_NOTIFICATION_CHANNEL_ID,
                "每日提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日任务和习惯提醒"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyNotification(data: DailyNotificationData) {
        // 创建点击意图 - 跳转到主页面
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, DAILY_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // 你需要添加这个图标
            .setContentTitle(data.getNotificationTitle())
            .setContentText(data.getNotificationContent())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(data.getNotificationContent())
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // 显示通知
        try {
            NotificationManagerCompat.from(context)
                .notify(DAILY_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // 用户未授予通知权限
            e.printStackTrace()
        }
    }

    fun cancelDailyNotification() {
        NotificationManagerCompat.from(context)
            .cancel(DAILY_NOTIFICATION_ID)
    }
}