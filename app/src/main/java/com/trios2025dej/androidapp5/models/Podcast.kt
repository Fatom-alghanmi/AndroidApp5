package com.trios2025dej.models

import java.io.Serializable

data class Podcast(
    val id: String,
    val title_original: String,
    val image: String?,
    val audio: String? = null
) : Serializable
