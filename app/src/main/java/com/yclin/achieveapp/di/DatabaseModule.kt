package com.yclin.achieveapp.di

import android.content.Context
import com.yclin.achieveapp.data.database.AchieveDatabase
import com.yclin.achieveapp.data.database.dao.HabitCompletionDao
import com.yclin.achieveapp.data.database.dao.HabitDao
import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.repository.HabitRepository
import com.yclin.achieveapp.data.repository.HabitRepositoryImpl
import com.yclin.achieveapp.data.repository.TaskRepository
import com.yclin.achieveapp.data.repository.TaskRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt依赖注入模块，提供数据库相关依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供数据库实例
     */
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AchieveDatabase {
        return AchieveDatabase.getDatabase(context)
    }

    /**
     * 提供TaskDao实例
     */
    @Singleton
    @Provides
    fun provideTaskDao(database: AchieveDatabase): TaskDao {
        return database.taskDao()
    }

    /**
     * 提供HabitDao实例
     */
    @Singleton
    @Provides
    fun provideHabitDao(database: AchieveDatabase): HabitDao {
        return database.habitDao()
    }

    /**
     * 提供HabitCompletionDao实例
     */
    @Singleton
    @Provides
    fun provideHabitCompletionDao(database: AchieveDatabase): HabitCompletionDao {
        return database.habitCompletionDao()
    }

    /**
     * 提供TaskRepository实例
     */
    @Singleton
    @Provides
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

    /**
     * 提供HabitRepository实例
     */
    @Singleton
    @Provides
    fun provideHabitRepository(
        habitDao: HabitDao,
        habitCompletionDao: HabitCompletionDao
    ): HabitRepository {
        return HabitRepositoryImpl(habitDao, habitCompletionDao)
    }
}