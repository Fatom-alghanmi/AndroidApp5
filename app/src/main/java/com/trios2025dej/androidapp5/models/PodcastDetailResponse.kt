package com.trios2025dej.models

data class PodcastDetailResponse(
    val id: String,
    val title: String,
    val image: String?,
    val episodes: List<Episode>
)