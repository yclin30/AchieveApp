package com.yclin.achieveapp.ui.feature_auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yclin.achieveapp.data.database.entity.User
import com.yclin.achieveapp.data.database.entity.UserRegistrationRequest
import com.yclin.achieveapp.data.network.AuthApiService
import com.yclin.achieveapp.data.session.SessionManager
import com.yclin.achieveapp.di.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 认证相关 ViewModel，负责处理登录、注册、用户信息管理等逻辑
 */
class AuthViewModel(
    application: Application,
    private val api: AuthApiService = NetworkModule.authApiService,
    private val sessionManager: SessionManager = SessionManager(application)
) : AndroidViewModel(application) {

    // 登录/注册页面状态
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _user = MutableStateFlow<User?>(sessionManager.getUser())
    val user: StateFlow<User?> = _user

    // 登录
    fun login(username: String, password: String, onSuccess: () -> Unit) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = api.loginUser(username, password)
                if (response.isSuccessful) {
                    val userList = response.body()
                    if (!userList.isNullOrEmpty()) {
                        val user = userList[0]
                        sessionManager.saveUser(user)
                        _user.value = user
                        onSuccess()
                    } else {
                        _error.value = "用户名或密码错误"
                    }
                } else {
                    _error.value = "登录失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "网络异常: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // 注册
    fun register(username: String, email: String, password: String, onSuccess: () -> Unit) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                // 检查用户名是否存在
                val userResp = api.checkUsernameExists(username)
                val emailResp = api.checkEmailExists(email)
                if (!userResp.body().isNullOrEmpty()) {
                    _error.value = "用户名已存在"
                } else if (!emailResp.body().isNullOrEmpty()) {
                    _error.value = "邮箱已被注册"
                } else {
                    // 注册
                    val regResp = api.registerUser(
                        UserRegistrationRequest(username, email, password)
                    )
                    if (regResp.isSuccessful) {
                        onSuccess()
                    } else {
                        _error.value = "注册失败: ${regResp.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "网络异常: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // 获取当前用户
    fun loadUser() {
        _user.value = sessionManager.getUser()
    }

    // 退出登录
    fun logout(onLogout: () -> Unit) {
        sessionManager.clearUser()
        _user.value = null
        onLogout()
    }

    // 清除错误信息
    fun clearError() {
        _error.value = null
    }
}