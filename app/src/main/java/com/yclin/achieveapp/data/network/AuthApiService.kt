package com.yclin.achieveapp.data.network

import com.yclin.achieveapp.data.database.entity.User // 确保路径正确
import com.yclin.achieveapp.data.database.entity.UserRegistrationRequest // 确保路径正确
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApiService {

    @POST("users")
    suspend fun registerUser(@Body userRequest: UserRegistrationRequest): Response<User> // JSON Server 会返回创建的用户对象

    // 使用 GET 请求模拟登录，查询用户名和密码匹配的用户
    // 在真实应用中，登录通常是 POST 请求，并且会返回 token
    @GET("users")
    suspend fun loginUser(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<List<User>> // 期望列表包含0个或1个用户

    // 检查用户名是否已存在
    @GET("users")
    suspend fun checkUsernameExists(@Query("username") username: String): Response<List<User>>

    // 检查邮箱是否已存在
    @GET("users")
    suspend fun checkEmailExists(@Query("email") email: String): Response<List<User>>

    // 根据ID获取用户信息 (例如用于 "我的" 页面)
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<User>
}