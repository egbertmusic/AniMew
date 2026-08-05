package com.example.anilistapp.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomSource(
    val name: String,
    val searchUrl: String, // e.g. "https://api.jikan.moe/v4/anime?q=%s"
    val resultsPath: String = "data",
    val titlePath: String = "title",
    val summaryPath: String = "synopsis",
    val posterPath: String = "images.jpg.image_url"
)
