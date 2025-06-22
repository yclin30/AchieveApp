package com.yclin.achieveapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.yclin.achieveapp.data.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationSettingsRepository {
    fun getSettings(): Flow<NotificationSettings>
    suspend fun saveSettings(settings: NotificationSettings)
}

@Singleton
class NotificationSettingsRepositoryImpl @Inject constructor(
    context: Context
) : NotificationSettingsRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())

    override fun getSettings(): Flow<NotificationSettings> = _settings.asStateFlow()

    override suspend fun saveSettings(settings: NotificationSettings) {
        sharedPreferences.edit()
            .putBoolean("is_enabled", settings.isEnabled)
            .putInt("notification_hour", settings.notificationTime.hour)
            .putInt("notification_minute", settings.notificationTime.minute)
            .apply()

        _settings.value = settings
    }

    private fun loadSettings(): NotificationSettings {
        return NotificationSettings(
            isEnabled = sharedPreferences.getBoolean("is_enabled", false),
            notificationTime = LocalTime.of(
                sharedPreferences.getInt("notification_hour", 8),
                sharedPreferences.getInt("notification_minute", 0)
            )
        )
    }
}