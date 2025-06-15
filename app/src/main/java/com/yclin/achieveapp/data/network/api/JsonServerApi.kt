package com.yclin.achieveapp.data.network.api

import com.yclin.achieveapp.data.network.model.RemoteHabit
import com.yclin.achieveapp.data.network.model.RemoteTask
import retrofit2.http.*

interface JsonServerApi {

    @GET("habits")
    suspend fun getHabits(
        @Query("userId") userId: Long
    ): List<RemoteHabit>

    @POST("habits")
    suspend fun addHabit(
        @Body habit: RemoteHabit
    ): RemoteHabit

    @PUT("habits/{id}")
    suspend fun updateHabit(
        @Path("id") id: Long,
        @Body habit: RemoteHabit
    ): RemoteHabit

    @DELETE("habits/{id}")
    suspend fun deleteHabit(
        @Path("id") id: Long
    )

    @GET("tasks")
    suspend fun getTasks(
        @Query("userId") userId: Long
    ): List<RemoteTask>

    @POST("tasks")
    suspend fun addTask(
        @Body task: RemoteTask
    ): RemoteTask

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Long,
        @Body task: RemoteTask
    ): RemoteTask

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: Long
    )

}