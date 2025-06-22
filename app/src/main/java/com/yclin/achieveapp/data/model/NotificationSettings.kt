package com.yclin.achieveapp.data.model

import java.time.LocalTime

/**
 * 通知设置
 */
data class NotificationSettings(
    val isEnabled: Boolean = false,
    val notificationTime: LocalTime = LocalTime.of(8, 0) // 默认早上8点
)