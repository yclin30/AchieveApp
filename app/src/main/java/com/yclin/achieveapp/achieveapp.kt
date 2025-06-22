package com.yclin.achieveapp

import android.app.Application
import com.yclin.achieveapp.data.database.AchieveDatabase
import com.yclin.achieveapp.data.network.api.RetrofitInstance // 如果你用 RetrofitInstance
import com.yclin.achieveapp.data.repository.HabitRepository
import com.yclin.achieveapp.data.repository.HabitRepositoryImpl
import com.yclin.achieveapp.data.repository.NotificationRepository
import com.yclin.achieveapp.data.repository.NotificationRepositoryImpl
import com.yclin.achieveapp.data.repository.NotificationSettingsRepository
import com.yclin.achieveapp.data.repository.NotificationSettingsRepositoryImpl
import com.yclin.achieveapp.data.repository.TaskRepository
import com.yclin.achieveapp.data.repository.TaskRepositoryImpl
import com.yclin.achieveapp.data.repository.SearchRepository
import com.yclin.achieveapp.data.repository.SearchRepositoryImpl
import com.yclin.achieveapp.notification.NotificationScheduler

class AchieveApp : Application() {

    val database by lazy { AchieveDatabase.getDatabase(this) }

    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database.taskDao(), RetrofitInstance.api) // 这里注入你的网络API
    }

    val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(
            database.habitDao(),
            database.habitCompletionDao(),
            RetrofitInstance.api // 这里注入你的网络API
        )
    }

    // ✅ 新增：搜索 Repository
    val searchRepository: SearchRepository by lazy {
        SearchRepositoryImpl(
            taskDao = database.taskDao(),
            habitDao = database.habitDao()
        )
    }
    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(
            taskDao = database.taskDao(),
            habitDao = database.habitDao()
        )
    }

    val notificationSettingsRepository: NotificationSettingsRepository by lazy {
        NotificationSettingsRepositoryImpl(this)
    }

    val notificationScheduler by lazy {
        NotificationScheduler(this)
    }
}