package com.example.anilistapp.ui.shorts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.GetShortsMediaQuery
import com.example.anilistapp.data.KitsuRepository
import com.example.anilistapp.data.KitsuSearchResult
import com.example.anilistapp.data.LocalizedMediaDetails
import com.example.anilistapp.data.MediaRepository
import com.example.anilistapp.data.SeerrRepository
import com.example.anilistapp.data.TmdbRepository
import com.example.anilistapp.data.SeerrSearchResult
import com.example.anilistapp.data.SeerrProfile
import com.example.anilistapp.data.SeerrServer
import com.example.anilistapp.data.SeerrSeasonInfo
import com.example.anilistapp.data.SettingsRepository
import com.example.anilistapp.ui.components.LocalizationManager
import com.example.anilistapp.ui.components.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShortsState(
    val isLoading: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val mediaList: List<GetShortsMediaQuery.Medium> = emptyList(),
    val currentPage: Int = 1,
    val hasNextPage: Boolean = true,
    val error: String? = null,
    val requestMessages: Map<Int, String> = emptyMap(), // mediaId -> message
    val isSeerrEnabled: Boolean = false,
    val shortsNavigationStyle: String = "BOTTOM",
    val shortsFeedSource: String = "TRENDING",
    val shortsFeedType: String = "ANIME",
    val enableMewingChad: Boolean = false,
    val seerrSearchResults: List<SeerrSearchResult> = emptyList(),
    val isSearchingSeerr: Boolean = false,
    val seerrProfiles: List<SeerrProfile> = emptyList(),
    val seerrServers: List<SeerrServer> = emptyList(),
    val seerrDetails: Pair<List<SeerrSeasonInfo>, Set<Int>>? = null, // all seasons to available set
    val overriddenVideoIds: Map<Int, String> = emptyMap(), // mediaId -> overridden videoId
    val minCommunityScore: Int = 70,
    val appLanguages: Set<String> = setOf("ENGLISH"),
    val primaryAppLanguage: String = "ENGLISH",
    val localizedDetails: Map<Int, LocalizedMediaDetails> = emptyMap(),
    val enableLocalizedContent: Boolean = true
)

@HiltViewModel
class ShortsViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val seerrRepository: SeerrRepository,
    private val settingsRepository: SettingsRepository,
    private val kitsuRepository: KitsuRepository,
    private val tmdbRepository: TmdbRepository,
    val localizationManager: LocalizationManager,
    val soundManager: SoundManager
) : ViewModel() {

    private val _state = MutableStateFlow(ShortsState())
    val state: StateFlow<ShortsState> = _state.asStateFlow()

    private val seenMediaIds = mutableSetOf<Int>()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val enabled = settingsRepository.enableSeerr.first()
            val navStyle = settingsRepository.shortsNavigationStyle.first()
            val source = settingsRepository.shortsFeedSource.first()
            val type = settingsRepository.shortsFeedType.first()
            val mewingChad = settingsRepository.enableMewingChad.first()
            val minScore = settingsRepository.minCommunityScore.first()
            val appLangs = settingsRepository.appLanguages.first()
            val primaryLang = settingsRepository.primaryAppLanguage.first()
            val localizedContent = settingsRepository.enableLocalizedContent.first()
            
            _state.update { it.copy(
                isSeerrEnabled = enabled, 
                shortsNavigationStyle = navStyle,
                shortsFeedSource = source,
                shortsFeedType = type,
                enableMewingChad = mewingChad,
                minCommunityScore = minScore,
                appLanguages = appLangs,
                primaryAppLanguage = primaryLang,
                enableLocalizedContent = localizedContent
            ) }
            
            // Re-fetch when settings change? Or just initially.
            fetchShorts()
        }
    }

    fun fetchShorts(isPagination: Boolean = false) {
        if (isPagination && (!state.value.hasNextPage || state.value.isPaginationLoading)) return

        viewModelScope.launch {
            if (isPagination) {
                _state.update { it.copy(isPaginationLoading = true) }
            } else {
                seenMediaIds.clear()
                _state.update { it.copy(isLoading = true, error = null, currentPage = 1, mediaList = emptyList()) }
            }
            
            try {
                val source = settingsRepository.shortsFeedSource.first()
                val trailerLang = settingsRepository.preferredTrailerLanguage.first()
                val typeStr = settingsRepository.shortsFeedType.first()
                val localizedContent = settingsRepository.enableLocalizedContent.first()
                val type = when (typeStr) {
                    "ANIME" -> com.example.anilistapp.type.MediaType.ANIME
                    "MANGA" -> com.example.anilistapp.type.MediaType.MANGA
                    else -> null
                }

                val page = if (isPagination) state.value.currentPage + 1 else 1
                var currentFetchPage = if (source == "RANDOM" && !isPagination) (1..100).random() else page

                val fetchedItems = mutableListOf<GetShortsMediaQuery.Medium>()
                val targetSize = if (source == "FOR YOU") 15 else 5
                val maxPagesToFetch = if (source == "FOR YOU") 2 else 1
                val perPage = if (source == "FOR YOU") 50 else 20
                
                var pagesFetched = 0
                var hasMore = true

                if (source == "FOR YOU") {
                    // NEW ALGORITHM FOR "FOR YOU" - INFINITE & TASTE-BASED
                    val userGenreWeights = mutableMapOf<String, Double>()
                    try {
                        val viewer = mediaRepository.getViewer().data?.Viewer
                        if (viewer != null) {
                            val stats = mediaRepository.getUserStats(viewer.id).data?.User?.statistics
                            val animeGenres = stats?.anime?.genres?.filterNotNull() ?: emptyList()
                            val mangaGenres = stats?.manga?.genres?.filterNotNull() ?: emptyList()
                            
                            animeGenres.forEach { g ->
                                g.genre?.let { name ->
                                    // Weight = count * (meanScore / 10). This prioritizes what the user watches most and likes.
                                    val weight = (g.count ?: 0) * ((g.meanScore ?: 0.0) / 10.0)
                                    userGenreWeights[name] = (userGenreWeights[name] ?: 0.0) + weight
                                }
                            }
                            mangaGenres.forEach { g ->
                                g.genre?.let { name ->
                                    val weight = (g.count ?: 0) * ((g.meanScore ?: 0.0) / 10.0)
                                    userGenreWeights[name] = (userGenreWeights[name] ?: 0.0) + weight
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ShortsViewModel", "Failed to fetch user stats for FOR YOU", e)
                    }

                    val scoredItems = mutableListOf<Pair<GetShortsMediaQuery.Medium, Double>>()
                    val targetBatchSize = 10
                    // Start from a random page for variety on refresh, but stay within first 20 for quality
                    var currentSearchPage = if (!isPagination) (1..20).random() else currentFetchPage

                    // We search until we find enough unique items that match the user's taste and community score
                    while (scoredItems.size < targetBatchSize && pagesFetched < 10 && hasMore) {
                        val response = mediaRepository.getShortsMedia(
                            page = currentSearchPage,
                            perPage = 50, // Fetch large batches to find matches
                            type = type,
                            sort = listOf(com.example.anilistapp.type.MediaSort.TRENDING_DESC), // Trending as a baseline
                            genres = null
                        )
                        
                        val rawList = response.data?.Page?.media?.filterNotNull() ?: emptyList()
                        if (rawList.isEmpty()) break

                        val filteredAndScored = rawList.filter { media ->
                            media.trailer?.site == "youtube" && 
                            !media.trailer.id.isNullOrEmpty() && 
                            media.mediaListEntry == null &&
                            !seenMediaIds.contains(media.id) &&
                            (media.averageScore ?: 0) >= state.value.minCommunityScore
                        }.map { media ->
                            val genreMatchScore = media.genres?.filterNotNull()?.sumOf { userGenreWeights[it] ?: 0.0 } ?: 0.0
                            // Score formula: TasteScore (dominant) + CommunityScore (secondary) + HUGE Random Boost (freshness)
                            val randomBoost = (0..2000).random().toDouble() 
                            val totalScore = (genreMatchScore * 1000.0) + (media.averageScore ?: 0).toDouble() + randomBoost
                            media to totalScore
                        }
                        
                        scoredItems.addAll(filteredAndScored)
                        hasMore = response.data?.Page?.pageInfo?.hasNextPage ?: false
                        pagesFetched++
                        currentSearchPage++
                    }

                    // Shuffle the final batch for maximum variety
                    val sortedItems = scoredItems.sortedByDescending { it.second }.map { it.first }.shuffled()
                    fetchedItems.addAll(sortedItems)
                    currentFetchPage = currentSearchPage 
                    
                } else {
                    val sort = when (source) {
                        "TRENDING" -> listOf(com.example.anilistapp.type.MediaSort.TRENDING_DESC)
                        "RECOMMENDED", "GEMINI" -> listOf(com.example.anilistapp.type.MediaSort.SCORE_DESC)
                        "PLANNING" -> listOf(com.example.anilistapp.type.MediaSort.UPDATED_AT_DESC)
                        "RANDOM" -> {
                            val allSorts = listOf(
                                com.example.anilistapp.type.MediaSort.ID,
                                com.example.anilistapp.type.MediaSort.ID_DESC,
                                com.example.anilistapp.type.MediaSort.SCORE_DESC,
                                com.example.anilistapp.type.MediaSort.POPULARITY_DESC,
                                com.example.anilistapp.type.MediaSort.UPDATED_AT_DESC
                            )
                            listOf(allSorts.random())
                        }
                        else -> listOf(com.example.anilistapp.type.MediaSort.TRENDING_DESC)
                    }

                    while (fetchedItems.size < targetSize && pagesFetched < maxPagesToFetch && hasMore) {
                        val response = mediaRepository.getShortsMedia(
                            page = currentFetchPage,
                            perPage = perPage,
                            type = type,
                            sort = sort,
                            genres = null
                        )
                        
                        val rawList = response.data?.Page?.media?.filterNotNull() ?: emptyList()
                        val filteredList = rawList.filter { 
                            it.trailer?.site == "youtube" && !it.trailer.id.isNullOrEmpty() 
                        }
                        
                        fetchedItems.addAll(filteredList)
                        hasMore = response.data?.Page?.pageInfo?.hasNextPage ?: false
                        
                        pagesFetched++
                        if (fetchedItems.size < targetSize && pagesFetched < maxPagesToFetch && hasMore) {
                            currentFetchPage++
                        }
                    }
                }

                _state.update { it.copy(
                    isLoading = false, 
                    isPaginationLoading = false,
                    mediaList = if (isPagination) it.mediaList + fetchedItems else fetchedItems,
                    currentPage = currentFetchPage,
                    hasNextPage = hasMore
                ) }
                
                // Track seen items
                fetchedItems.forEach { seenMediaIds.add(it.id) }

                // Localized Overrides (Synopsis, Posters & Titles) from TMDB
                val preferredAppLangForContent = settingsRepository.primaryAppLanguage.first()
                
                if (preferredAppLangForContent != "ENGLISH" && localizedContent) {
                    viewModelScope.launch {
                        fetchedItems.forEach { media ->
                            launch { // Parallelize fetches
                                try {
                                    val titleToSearch = media.title?.userPreferred ?: media.title?.romaji ?: media.title?.english ?: ""
                                    if (titleToSearch.isEmpty()) return@launch
                                    
                                    Log.d("ShortsVM", "Searching TMDB for: $titleToSearch in $preferredAppLangForContent")

                                    val results = seerrRepository.searchShow(titleToSearch)
                                    val match = results.find { 
                                        it.title.equals(media.title?.userPreferred, ignoreCase = true) ||
                                        it.title.equals(media.title?.romaji, ignoreCase = true) ||
                                        it.title.equals(media.title?.english, ignoreCase = true)
                                    } ?: results.firstOrNull()
                                    
                                    if (match != null) {
                                        Log.d("ShortsVM", "Found TMDB match: ${match.title} (${match.id})")
                                        val localized = tmdbRepository.getLocalizedDetails(match.id, match.type, preferredAppLangForContent)
                                        if (localized != null) {
                                            Log.d("ShortsVM", "Got localized title: ${localized.title}")
                                            _state.update { it.copy(localizedDetails = it.localizedDetails + (media.id to localized)) }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("ShortsVM", "TMDB fetch failed for ${media.id}", e)
                                }
                            }
                        }
                    }
                }

                // Try to enrich trailers if preferred language is not Japanese
                if (trailerLang != "JAPANESE") {
                    viewModelScope.launch {
                        fetchedItems.forEach { media ->
                            if (media.trailer?.site == "youtube") {
                                // Background search for localized trailer
                                val kitsuSearchTitle = if (trailerLang == "ENGLISH" && !media.title?.english.isNullOrEmpty()) media.title?.english!! else media.title?.userPreferred ?: ""
                                
                                val localizedId = kitsuRepository.searchYouTubeTrailer(
                                    title = kitsuSearchTitle, 
                                    language = trailerLang,
                                    type = media.type?.name ?: "ANIME",
                                    format = media.format?.name
                                )
                                if (localizedId != null) {
                                    _state.update { it.copy(overriddenVideoIds = it.overriddenVideoIds + (media.id to localizedId)) }
                                } else {
                                    // Fallback to Kitsu title-based
                                    val kitsu = kitsuRepository.getDetailsByTitle(kitsuSearchTitle)
                                    if (kitsu?.youtubeVideoId != null && kitsu.youtubeVideoId.isNotEmpty() && kitsu.youtubeVideoId != media.trailer.id) {
                                        _state.update { it.copy(overriddenVideoIds = it.overriddenVideoIds + (media.id to kitsu.youtubeVideoId)) }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ShortsViewModel", "Failed to fetch shorts", e)
                _state.update { it.copy(isLoading = false, isPaginationLoading = false, error = e.message) }
            }
        }
    }

    fun searchOnSeerr(title: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearchingSeerr = true, seerrSearchResults = emptyList()) }
            try {
                val results = seerrRepository.searchShow(title)
                _state.update { it.copy(isSearchingSeerr = false, seerrSearchResults = results) }
            } catch (e: Exception) {
                _state.update { it.copy(isSearchingSeerr = false) }
            }
        }
    }

    fun loadSeerrOptions(type: String) {
        viewModelScope.launch {
            try {
                val servers = if (type == "movie") seerrRepository.getRadarrSettings() else seerrRepository.getSonarrSettings()
                val serverId = if (type == "movie") settingsRepository.seerrRadarrServerId.first() else settingsRepository.seerrSonarrServerId.first()
                
                val profiles = if (serverId != null && serverId != -1) {
                    seerrRepository.getProfilesForServer(type, serverId)
                } else {
                    seerrRepository.getQualityProfiles()
                }
                
                _state.update { it.copy(seerrServers = servers, seerrProfiles = profiles) }
            } catch (e: Exception) {}
        }
    }

    fun loadSeerrDetails(tmdbId: Int, type: String) {
        viewModelScope.launch {
            try {
                val details = seerrRepository.getShowDetails(tmdbId, type)
                _state.update { it.copy(seerrDetails = details) }
            } catch (e: Exception) {}
        }
    }

    fun requestOnSeerr(
        mediaId: Int,
        tmdbId: Int,
        type: String,
        seasons: List<Int>,
        profileId: Int,
        serverId: Int?,
        rootFolder: String?
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(requestMessages = it.requestMessages + (mediaId to "Requesting...")) }
                val success = seerrRepository.requestMedia(tmdbId, type, seasons, profileId, serverId, rootFolder)
                if (success) {
                    _state.update { it.copy(requestMessages = it.requestMessages + (mediaId to "Requested!")) }
                } else {
                    _state.update { it.copy(requestMessages = it.requestMessages + (mediaId to "Failed.")) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(requestMessages = it.requestMessages + (mediaId to "Error: ${e.message}")) }
            }
        }
    }

    fun addToWatchlist(mediaId: Int) {
        viewModelScope.launch {
            try {
                mediaRepository.saveMediaListEntry(mediaId, com.example.anilistapp.type.MediaListStatus.PLANNING)
                _state.update { it.copy(requestMessages = it.requestMessages + (mediaId to "Added to Watchlist!")) }
            } catch (e: Exception) {
                _state.update { it.copy(requestMessages = it.requestMessages + (mediaId to "Failed to add.")) }
            }
        }
    }
}
