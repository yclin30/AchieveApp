package com.yclin.achieveapp

import android.app.Application
import com.yclin.achieveapp.data.database.AchieveDatabase
import com.yclin.achieveapp.data.repository.TaskRepository
import com.yclin.achieveapp.data.repository.TaskRepositoryImpl

class AchieveApp : Application() {

    // 懒加载数据库实例
    private val database by lazy { AchieveDatabase.getDatabase(this) }

    // 懒加载任务仓库实例
    val taskRepository by lazy { TaskRepositoryImpl(database.taskDao()) }

    override fun onCreate() {
        super.onCreate()
        // 初始化通知渠道等
    }
}