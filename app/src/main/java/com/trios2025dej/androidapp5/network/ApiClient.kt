package com.trios2025dej.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://listen-api.listennotes.com/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
