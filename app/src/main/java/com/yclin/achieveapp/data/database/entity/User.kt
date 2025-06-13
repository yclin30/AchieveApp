package com.yclin.achieveapp.data.database.entity

/**
 * 用户实体类
 * 适用于前端与 JSON Server 的用户交互（注册、登录、展示）
 */
data class User(
    val id: Long = 0L,              // 用户ID，JSON Server自增
    val username: String,           // 用户名
    val email: String,              // 邮箱
    val password: String? = null    // 密码，注册/登录时用，正常展示不用
)