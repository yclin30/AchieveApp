package com.yclin.achieveapp.di

import com.google.gson.GsonBuilder
import com.yclin.achieveapp.data.network.AuthApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // 你的 JSON Server 基础 URL
    // 如果在模拟器上运行，通常是 "http://10.0.2.2:端口号/"
    // 如果在真机上，并且电脑和手机在同一网络，则是电脑的局域网 IP 地址
    private const val BASE_URL = "http://10.0.2.2:3001/" // 假设 JSON server 运行在 3001 端口

    private val gson = GsonBuilder().create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 设置日志级别为 BODY，会打印请求和响应的详细信息
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // 添加日志拦截器
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // 使用配置好的 OkHttpClient
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}