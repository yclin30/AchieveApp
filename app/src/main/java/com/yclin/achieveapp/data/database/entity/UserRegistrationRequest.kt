package com.yclin.achieveapp.data.database.entity

/**
 * 用户注册请求体
 * 用于用户注册时向后端提交的数据模型
 */
data class UserRegistrationRequest(
    val username: String,   // 用户名
    val email: String,      // 邮箱
    val password: String    // 密码
)