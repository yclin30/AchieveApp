package com.yclin.achieveapp.ui.feature_notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yclin.achieveapp.AchieveApp
import com.yclin.achieveapp.data.model.NotificationSettings
import com.yclin.achieveapp.data.repository.NotificationSettingsRepository
import com.yclin.achieveapp.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

class NotificationSettingsViewModel(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _shouldRequestPermission = MutableStateFlow(false)
    val shouldRequestPermission: StateFlow<Boolean> = _shouldRequestPermission.asStateFlow()

    fun requestNotificationPermission() {
        _shouldRequestPermission.value = true
    }

    fun onPermissionRequestHandled() {
        _shouldRequestPermission.value = false
    }

    fun onPermissionResult(granted: Boolean) {
        _permissionGranted.value = granted
        onPermissionRequestHandled()
    }

    private val _settings = MutableStateFlow(NotificationSettings())
    val settings: StateFlow<NotificationSettings> = _settings.asStateFlow()

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            notificationSettingsRepository.getSettings().collect { settings ->
                _settings.value = settings
            }
        }
    }

    fun checkNotificationPermission(context: Context) {
        _permissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 以下默认有权限
        }
    }

    fun requestNotificationPermission(context: Context) {
        // 这里需要在 Activity 中处理权限请求
        // 可以通过回调或事件来实现
    }

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            val newSettings = _settings.value.copy(isEnabled = enabled)
            notificationSettingsRepository.saveSettings(newSettings)

            if (enabled) {
                notificationScheduler.enableDailyNotification(
                    newSettings.notificationTime.hour,
                    newSettings.notificationTime.minute
                )
            } else {
                notificationScheduler.disableDailyNotification()
            }
        }
    }

    fun updateNotificationTime(time: LocalTime) {
        viewModelScope.launch {
            val newSettings = _settings.value.copy(notificationTime = time)
            notificationSettingsRepository.saveSettings(newSettings)

            if (newSettings.isEnabled) {
                notificationScheduler.enableDailyNotification(
                    time.hour,
                    time.minute
                )
            }
        }
    }

    fun sendTestNotification() {
        notificationScheduler.sendTestNotification()
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as AchieveApp
                return NotificationSettingsViewModel(
                    notificationSettingsRepository = application.notificationSettingsRepository,
                    notificationScheduler = application.notificationScheduler
                ) as T
            }
        }
    }
}