package com.yclin.achieveapp.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room数据库类型转换器，用于在数据库类型和Kotlin类型之间进行转换
 */
class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    // 修正这里：使用 "yyyy-MM-dd HH:mm:ss" 格式，中间是空格
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * 将LocalDate转换为String存储
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    /**
     * 将String转换回LocalDate
     */
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, dateFormatter) }
    }

    /**
     * 将LocalDateTime转换为String存储
     */
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        // 确保保存时也使用这个自定义的格式化器
        return dateTime?.format(dateTimeFormatter)
    }

    /**
     * 将String转换回LocalDateTime
     */
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        // 确保读取时也使用这个自定义的格式化器
        return dateTimeString?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
}