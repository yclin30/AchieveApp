package com.yclin.achieveapp

import android.app.Application
import com.yclin.achieveapp.data.database.AchieveDatabase
import com.yclin.achieveapp.data.network.api.RetrofitInstance // 如果你用 RetrofitInstance
import com.yclin.achieveapp.data.repository.HabitRepository
import com.yclin.achieveapp.data.repository.HabitRepositoryImpl
import com.yclin.achieveapp.data.repository.TaskRepository
import com.yclin.achieveapp.data.repository.TaskRepositoryImpl

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
}