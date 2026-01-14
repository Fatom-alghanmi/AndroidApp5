package com.trios2025dej.network

import com.trios2025dej.models.PodcastDetailResponse
import com.trios2025dej.models.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PodcastApi {

    @GET("search")
    fun searchPodcasts(
        @Header("X-ListenAPI-Key") apiKey: String,
        @Query("q") query: String,
        @Query("type") type: String = "podcast"
    ): Call<SearchResponse>

    @GET("podcasts/{id}")
    fun getPodcastDetails(
        @Header("X-ListenAPI-Key") apiKey: String,
        @Path("id") podcastId: String
    ): Call<PodcastDetailResponse>
}
