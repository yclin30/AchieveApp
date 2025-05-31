package com.yclin.achieveapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yclin.achieveapp.data.database.dao.HabitCompletionDao
import com.yclin.achieveapp.data.database.dao.HabitDao
import com.yclin.achieveapp.data.database.dao.TaskDao
import com.yclin.achieveapp.data.database.entity.Habit
import com.yclin.achieveapp.data.database.entity.HabitCompletion
import com.yclin.achieveapp.data.database.entity.Task

/**
 * Room数据库主类，是应用程序的单一数据库入口点
 */
@Database(
    entities = [Task::class, Habit::class, HabitCompletion::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AchieveDatabase : RoomDatabase() {

    /**
     * 获取任务DAO
     */
    abstract fun taskDao(): TaskDao

    /**
     * 获取习惯DAO
     */
    abstract fun habitDao(): HabitDao

    /**
     * 获取习惯完成记录DAO
     */
    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {
        // 单例实现防止同时打开多个数据库实例
        @Volatile
        private var INSTANCE: AchieveDatabase? = null

        /**
         * 获取数据库实例，如果不存在则创建
         */
        fun getDatabase(context: Context): AchieveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AchieveDatabase::class.java,
                    "achieve_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}