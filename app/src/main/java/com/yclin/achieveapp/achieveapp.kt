package com.yclin.achieveapp

import android.app.Application
import com.yclin.achieveapp.data.database.AchieveDatabase
import com.yclin.achieveapp.data.repository.HabitRepository
import com.yclin.achieveapp.data.repository.HabitRepositoryImpl
import com.yclin.achieveapp.data.repository.TaskRepository
import com.yclin.achieveapp.data.repository.TaskRepositoryImpl

class AchieveApp : Application() {

    // 懒加载数据库实例
    val database by lazy { AchieveDatabase.getDatabase(this) }

    // 懒加载任务仓库实例
    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database.taskDao())
    }

    // 懒加载习惯仓库实例
    val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(
            database.habitDao(),
            database.habitCompletionDao()
        )
    }
}