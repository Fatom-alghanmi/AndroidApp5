package com.trios2025dej.network

import com.trios2025dej.models.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PodcastApi {
    @GET("search")
    fun searchPodcasts(
        @Header("X-ListenAPI-Key") apiKey: String,
        @Query("q") query: String,
        @Query("type") type: String = "podcast"
    ): Call<SearchResponse>
}
