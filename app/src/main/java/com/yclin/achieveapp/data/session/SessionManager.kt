package com.yclin.achieveapp.data.session

import android.content.Context
import android.content.SharedPreferences
import com.yclin.achieveapp.data.database.entity.User

/**
 * 用户会话管理类，用于本地保存和读取当前登录用户的信息
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }

    // 保存用户信息（登录/注册成功后调用）
    fun saveUser(user: User) {
        prefs.edit()
            .putLong(KEY_USER_ID, user.userId)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_EMAIL, user.email)
            .apply()
    }

    // 读取当前已登录用户信息
    fun getUser(): User? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        val username = prefs.getString(KEY_USERNAME, null)
        val email = prefs.getString(KEY_EMAIL, null)
        return if (id != -1L && username != null && email != null) {
            User(userId = id, username = username, email = email)
        } else {
            null
        }
    }

    // 清除用户信息（退出登录时调用）
    fun clearUser() {
        prefs.edit().clear().apply()
    }
}