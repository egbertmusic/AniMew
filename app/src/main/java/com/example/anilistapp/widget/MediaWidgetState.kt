package com.example.anilistapp.widget

import com.example.anilistapp.ui.theme.AppTheme
import kotlinx.serialization.Serializable

@Serializable
enum class WidgetFilter {
    ANIME, MANGA, BOTH
}

@Serializable
data class MediaWidgetState(
    val watching: List<WidgetMediaItem> = emptyList(),
    val planning: List<WidgetMediaItem> = emptyList(),
    val seasonal: List<WidgetMediaItem> = emptyList(),
    val airing: List<WidgetAiringItem> = emptyList(),
    val stats: WidgetUserStats? = null,
    val filter: WidgetFilter = WidgetFilter.BOTH,
    val viewerName: String? = null,
    val themeMode: String = "DARK",
    val cornerRadius: Int = 16,
    val opacity: Float = 1.0f,
    val showProgress: Boolean = true,
    val lastUpdated: Long = 0
)

@Serializable
data class WidgetMediaItem(
    val id: Int,
    val title: String,
    val progress: Int,
    val totalEpisodes: Int?,
    val imageUrl: String,
    val type: String = "ANIME", // Default value to prevent serialization errors
    val isNewRelease: Boolean = false,
    val color: String? = null,
    val localImageUri: String? = null
)

@Serializable
data class WidgetAiringItem(
    val id: Int,
    val title: String,
    val episode: Int,
    val airingAt: Int,
    val imageUrl: String,
    val localImageUri: String? = null
)

@Serializable
data class WidgetUserStats(
    val count: Int,
    val episodesWatched: Int,
    val minutesWatched: Int,
    val meanScore: Double
)
