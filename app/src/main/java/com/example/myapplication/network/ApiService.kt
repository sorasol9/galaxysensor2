package com.example.myapplication.network

import com.example.myapplication.HealthData
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {

    @POST("/hh/receive")
    suspend fun sendHealthData(@Body healthData: HealthData )
}