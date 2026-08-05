package com.example.anilistapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Complement(
    val id: String,
    val name: String,
    val description: String,
    val version: String = "1.0.0",
    val author: String = "Unknown",
    val iconUrl: String? = null,
    val searchProviders: List<CustomSource> = emptyList(),
    val metadataProviders: List<MetadataProvider> = emptyList(),
    val streamProviders: List<StreamProvider> = emptyList()
)

@Serializable
data class MetadataProvider(
    val name: String,
    val detailUrl: String, // e.g. "https://api.jikan.moe/v4/anime/%s"
    val titlePath: String? = null,
    val summaryPath: String? = null,
    val extraFields: Map<String, String> = emptyMap() // "Score" -> "data.score"
)

@Serializable
data class StreamProvider(
    val name: String,
    val watchUrl: String, // e.g. "https://crunchyroll.com/series/%s"
    val type: String = "ANIME"
)
