package com.yclin.achieveapp.data.network.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // 请将 BASE_URL 替换为你的 json-server 启动地址，如 "http://10.0.2.2:3000/"
    private const val BASE_URL = "http://10.0.2.2:3001/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: JsonServerApi by lazy {
        retrofit.create(JsonServerApi::class.java)
    }
}