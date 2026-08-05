package com.example.anilistapp.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.data.KitsuRepository
import com.example.anilistapp.data.KitsuSearchResult
import com.example.anilistapp.data.MediaRepository
import com.example.anilistapp.data.SeerrProfile
import com.example.anilistapp.data.SeerrRepository
import com.example.anilistapp.data.SeerrSearchResult
import com.example.anilistapp.data.SettingsRepository
import com.example.anilistapp.data.ComplementRepository
import com.example.anilistapp.data.JellyfinRepository
import com.example.anilistapp.data.PlexRepository
import com.example.anilistapp.data.StreamProvider
import com.example.anilistapp.data.MetadataProvider
import com.example.anilistapp.type.MediaListStatus
import com.example.anilistapp.ui.components.LocalizationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaDetailState(
    val mediaId: Int? = null,
    val isInWatchlist: Boolean = false,
    val title: String = "",
    val kitsuDetails: KitsuSearchResult? = null,
    val seerrMatch: SeerrSearchResult? = null,
    val seerrSeasons: List<Int> = emptyList(),
    val selectedSeasons: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isSeerrEnabled: Boolean = false,
    val seerrProfiles: List<SeerrProfile> = emptyList(),
    val selectedProfileId: Int? = null,
    val requestMessage: String? = null,
    val titleLanguage: String = "ROMAJI",
    val showMultipleTitles: Boolean = false,
    val appLanguages: Set<String> = setOf("ENGLISH"),
    val randomizeUiLanguage: Boolean = false,
    val streamLinks: List<Pair<String, String>> = emptyList(), // Name to URL
    val extraMetadata: Map<String, String> = emptyMap(),
    val youtubeVideoId: String? = null
)

@HiltViewModel
class MediaDetailViewModel @Inject constructor(
    private val kitsuRepository: KitsuRepository,
    private val seerrRepository: SeerrRepository,
    private val settingsRepository: SettingsRepository,
    private val mediaRepository: MediaRepository,
    private val complementRepository: ComplementRepository,
    private val jellyfinRepository: JellyfinRepository,
    private val plexRepository: PlexRepository,
    val localizationManager: LocalizationManager
) : ViewModel() {

    private val _state = MutableStateFlow(MediaDetailState())
    val state: StateFlow<MediaDetailState> = _state.asStateFlow()

    fun loadDetails(title: String, mediaId: Int? = null, mediaTypeStr: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(title = title, mediaId = mediaId, isLoading = true) }
            
            val preferredType = mediaTypeStr?.let { 
                try { com.example.anilistapp.type.MediaType.valueOf(it) } catch (e: Exception) { null }
            }
            
            val kitsu = kitsuRepository.getDetailsByTitle(title, preferredType != com.example.anilistapp.type.MediaType.MANGA)
            
            var resolvedMediaId = mediaId
            var alreadyInList = mediaId != null
            var romajiTitle: String? = null
            var englishTitle: String? = null
            var mediaType: com.example.anilistapp.type.MediaType? = preferredType
            var mediaFormat: String? = null
            var aniListTmdbId: Int? = null
            var aniListTrailerId: String? = null
            
            // 1. Try to find on AniList to get ID, status, and better titles
            try {
                // Search specifically for the type if we know it
                val aniSearch = mediaRepository.searchAniList(title, preferredType)
                val results = aniSearch.data?.Page?.media?.filterNotNull() ?: emptyList()
                
                // Prioritize exact type match if we have a preferred type
                val topResult = if (preferredType != null) {
                    results.find { it.type == preferredType } ?: results.firstOrNull()
                } else {
                    results.find { it.type == com.example.anilistapp.type.MediaType.ANIME } ?: results.firstOrNull()
                }
                
                if (topResult != null) {
                    resolvedMediaId = topResult.id
                    alreadyInList = topResult.mediaListEntry != null
                    
                    romajiTitle = topResult.title?.romaji
                    englishTitle = topResult.title?.english
                    mediaType = topResult.type
                    mediaFormat = topResult.format?.name
                    
                    aniListTmdbId = topResult.externalLinks?.filterNotNull()
                        ?.find { it.site.contains("TMDB", ignoreCase = true) }
                        ?.url?.removeSuffix("/")?.split("/")?.lastOrNull()?.toIntOrNull()

                    aniListTrailerId = if (topResult.trailer?.site == "youtube") topResult.trailer.id else null
                }
            } catch (e: Exception) {
                Log.e("MediaDetailVM", "AniList search failed", e)
            }

            val seerrEnabled = settingsRepository.enableSeerr.first()
            val seerrUrl = settingsRepository.seerrUrl.first()
            val isSeerrConfigured = seerrEnabled && seerrUrl.isNotEmpty()
            
            var profiles = emptyList<SeerrProfile>()
            var seerrMatch: SeerrSearchResult? = null
            var seasons = emptyList<Int>()
            
            // FORCE Seerr enabled for all non-manga anime
            val isActuallyManga = mediaType == com.example.anilistapp.type.MediaType.MANGA || kitsu?.isAnime == false
            val finalSeerrEnabled = isSeerrConfigured && !isActuallyManga

            if (isSeerrConfigured && finalSeerrEnabled) {
                try {
                    // 0. QUICK CHECK: Use Global Library Cache first (Matches what's in the Library grid)
                    val globalStatuses = seerrRepository.getAllMediaStatuses()
                    
                    // Priority 1: Match by TMDB ID
                    if (aniListTmdbId != null) {
                        val status = globalStatuses["tv_$aniListTmdbId"] 
                            ?: globalStatuses["movie_$aniListTmdbId"]
                        
                        if (status != null && status >= 5) {
                            seerrMatch = SeerrSearchResult(
                                id = aniListTmdbId,
                                title = title,
                                type = if (globalStatuses.containsKey("tv_$aniListTmdbId")) "tv" else "movie",
                                overview = "Available in your library",
                                status = status
                            )
                        }
                    }

                    // Pre-fetch Sonarr profiles as a default
                    val sonarrServers = seerrRepository.getSonarrSettings()
                    val defaultSonarr = sonarrServers.firstOrNull { it.isDefault } ?: sonarrServers.firstOrNull()
                    if (defaultSonarr != null) {
                        profiles = seerrRepository.getProfilesForServer("tv", defaultSonarr.id)
                    }

                    // 1. Try mapping via TMDB ID from AniList if not found in quick check
                    if (seerrMatch == null && aniListTmdbId != null) {
                        seerrMatch = seerrRepository.getMediaDetails(aniListTmdbId, "tv") 
                            ?: seerrRepository.getMediaDetails(aniListTmdbId, "movie")
                            ?: seerrRepository.findInCache(aniListTmdbId, title)
                    }

                    // 2. If no TMDB match, try search
                    if (seerrMatch == null) {
                        val titlesToTry = mutableListOf<String>()
                        // PRIORITY: English first for search accuracy
                        englishTitle?.let { titlesToTry.add(it) }
                        title.let { titlesToTry.add(it) }
                        romajiTitle?.let { titlesToTry.add(it) }
                        kitsu?.title?.let { titlesToTry.add(it) }
                        
                        val baseTitle = title.replace(Regex("(?i)\\s+season\\s+\\d+.*"), "").trim()
                        if (baseTitle != title) titlesToTry.add(baseTitle)
                        
                        // Hard-fix for Frieren
                        if (title.contains("Frieren", ignoreCase = true)) titlesToTry.add("Frieren")

                        val preferredSeerrType = if (mediaFormat == "MOVIE") "movie" else "tv"

                        for (t in titlesToTry.distinct()) {
                            if (t.isEmpty()) continue
                            val results = seerrRepository.searchShow(t)
                            
                            // Check title match using more aggressive fuzzy logic AND forced type match
                            val match = results.find { it.type == preferredSeerrType && isTitleMatch(t, it.title) }
                            
                            if (match != null) {
                                // Important: We found it via search, but let's check if the search result itself 
                                // already has the correct library status (which we enriched in searchShow)
                                seerrMatch = if (match.status == 5 || match.status == 6) {
                                    match 
                                } else {
                                    seerrRepository.getMediaDetails(match.id, match.type) ?: match
                                }
                                break
                            }
                        }
                    }

                    // 3. FINAL STATUS OVERRIDE: Check the Global Library Cache for this TMDB ID
                    // This ensures that if the Library sync found it, the Detail screen shows it too.
                    seerrMatch?.let { currentMatch ->
                        val globalStatuses = seerrRepository.getAllMediaStatuses()
                        val overrideStatus = globalStatuses["${currentMatch.type}_${currentMatch.id}"]
                            ?: globalStatuses["tv_${currentMatch.id}"]
                            ?: globalStatuses["movie_${currentMatch.id}"]
                        
                        if (overrideStatus != null && overrideStatus >= 5) {
                            seerrMatch = currentMatch.copy(status = overrideStatus)
                        }
                    }

                    // 4. FORCE DETECTION: Check Jellyfin/Plex directly (Always check if Seerr doesn't say it's Available)
                    if ((seerrMatch == null || (seerrMatch?.status ?: 1) < 5) && settingsRepository.enableMediaServerFallback.first()) {
                        try {
                            // Also check manual overrides
                            val manualAvailable = settingsRepository.manualAvailableIds.first().contains(resolvedMediaId)
                            
                            if (manualAvailable) {
                                seerrMatch = SeerrSearchResult(
                                    id = aniListTmdbId ?: 0,
                                    title = title,
                                    type = if (mediaFormat == "MOVIE") "movie" else "tv",
                                    overview = "Manually marked as available",
                                    status = 5
                                )
                            } else {
                                // Try searching with all available titles
                                val titlesToTry = mutableSetOf<String>()
                                titlesToTry.add(title)
                                romajiTitle?.let { titlesToTry.add(it) }
                                englishTitle?.let { titlesToTry.add(it) }
                                kitsu?.title?.let { titlesToTry.add(it) }

                                var foundInMediaServer = false
                                for (st in titlesToTry) {
                                    if (st.isEmpty()) continue
                                    if (jellyfinRepository.searchItem(st) || plexRepository.searchItem(st)) {
                                        foundInMediaServer = true
                                        break
                                    }
                                }
                                
                                if (foundInMediaServer) {
                                    seerrMatch = SeerrSearchResult(
                                        id = aniListTmdbId ?: 0,
                                        title = title,
                                        type = if (mediaFormat == "MOVIE") "movie" else "tv",
                                        overview = "Detected via media server (Jellyfin/Plex)",
                                        status = 6 // Status 6 = Media Server Detected
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MediaDetailVM", "Force detection failed", e)
                        }
                    }

                    // 4. Fallback: If still no match but it's an anime, create a placeholder
                    if (seerrMatch == null && aniListTmdbId != null) {
                        seerrMatch = SeerrSearchResult(id = aniListTmdbId, title = title, type = "tv", overview = "", status = null)
                    }

                    // 5. Fetch Seasons for the match (or placeholder)
                    val match = seerrMatch
                    if (match != null && (match.status ?: 1) < 5) { // Only fetch seasons if NOT available (to allow requesting)
                        if (match.type == "tv") {
                            seasons = try {
                                val fetched = seerrRepository.getShowDetails(match.id, "tv")
                                if (fetched.isEmpty()) listOf(1) else fetched
                            } catch (e: Exception) {
                                listOf(1)
                            }
                        }
                        
                        // Switch to Radarr profiles if it's a movie
                        if (match.type == "movie") {
                            val radarrServers = seerrRepository.getRadarrSettings()
                            val defaultRadarr = radarrServers.firstOrNull { it.isDefault } ?: radarrServers.firstOrNull()
                            if (defaultRadarr != null) {
                                profiles = seerrRepository.getProfilesForServer("movie", defaultRadarr.id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MediaDetailVM", "Seerr detail loading failed", e)
                }
            }

            val language = settingsRepository.titleLanguage.first()
            val multiple = settingsRepository.showMultipleTitles.first()
            val appLangs = settingsRepository.appLanguages.first()
            val randomize = settingsRepository.randomizeUiLanguage.first()
            
            val finalYoutubeId = aniListTrailerId ?: kitsu?.youtubeVideoId

            _state.update { it.copy(
                mediaId = resolvedMediaId,
                isInWatchlist = alreadyInList,
                kitsuDetails = kitsu,
                seerrMatch = seerrMatch,
                seerrSeasons = seasons,
                selectedSeasons = if (seasons.isNotEmpty()) setOf(seasons.last()) else emptySet(), // Default to latest season
                isSeerrEnabled = finalSeerrEnabled,
                seerrProfiles = profiles,
                selectedProfileId = profiles.firstOrNull()?.id,
                titleLanguage = language,
                showMultipleTitles = multiple,
                appLanguages = appLangs,
                randomizeUiLanguage = randomize,
                isLoading = false,
                youtubeVideoId = if (finalYoutubeId.isNullOrEmpty()) null else finalYoutubeId
            ) }

            // 5. Load Complements (Streaming & Metadata)
            loadComplements(title, romajiTitle ?: title, mediaType)
        }
    }

    private fun loadComplements(title: String, romaji: String, type: com.example.anilistapp.type.MediaType?) {
        viewModelScope.launch {
            complementRepository.installedComplements.collect { complements ->
                val streamLinks = mutableListOf<Pair<String, String>>()
                val extraMetadata = mutableMapOf<String, String>()
                
                complements.forEach { comp ->
                    // Streaming
                    comp.streamProviders.forEach { prov ->
                        if (prov.type == type?.name || (type == null && prov.type == "ANIME")) {
                            val url = prov.watchUrl.replace("%s", java.net.URLEncoder.encode(romaji, "UTF-8"))
                            streamLinks.add(prov.name to url)
                        }
                    }
                    
                    // Metadata (simplified, just title-based for now)
                    comp.metadataProviders.forEach { prov ->
                        // In a real app we'd fetch JSON from detailUrl and map fields
                        // For now let's just show the provider exists as a "Source"
                        extraMetadata[prov.name] = "External Data Available"
                    }
                }
                
                _state.update { it.copy(
                    streamLinks = streamLinks,
                    extraMetadata = extraMetadata
                ) }
            }
        }
    }

    fun onProfileSelected(id: Int) {
        _state.update { it.copy(selectedProfileId = id) }
    }

    fun onSeasonToggle(season: Int) {
        _state.update { state ->
            val current = state.selectedSeasons
            val newSet = if (current.contains(season)) {
                current - season
            } else {
                current + season
            }
            state.copy(selectedSeasons = newSet)
        }
    }

    fun addToWatchlist() {
        val id = _state.value.mediaId ?: return
        viewModelScope.launch {
            try {
                val response = mediaRepository.saveMediaListEntry(id, MediaListStatus.PLANNING)
                if (response.data?.SaveMediaListEntry != null) {
                    _state.update { it.copy(requestMessage = "Added to Watchlist!", isInWatchlist = true) }
                } else {
                    _state.update { it.copy(requestMessage = "Failed to add to watchlist.") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(requestMessage = "Error: ${e.message}") }
            }
        }
    }

    fun requestOnSeerr(profileId: Int) {
        val match = _state.value.seerrMatch ?: return
        viewModelScope.launch {
            val serverId = if (match.type == "tv") {
                settingsRepository.seerrSonarrServerId.first()
            } else {
                settingsRepository.seerrRadarrServerId.first()
            }
            
            val rootFolder = if (match.type == "tv") {
                settingsRepository.seerrSonarrRootFolder.first()
            } else {
                settingsRepository.seerrRadarrRootFolder.first()
            }

            _state.update { it.copy(requestMessage = "Sending request...") }

            val success = seerrRepository.requestMedia(
                tmdbId = match.id,
                type = match.type,
                seasons = _state.value.selectedSeasons.toList(),
                profileId = profileId,
                serverId = serverId,
                rootFolder = rootFolder.takeIf { it.isNotEmpty() }
            )

            if (success) {
                // Make sure we have a mediaId before trying to sync to AniList
                var currentMediaId = _state.value.mediaId
                if (currentMediaId == null) {
                    // Try one last time to find the ID via AniList search
                    try {
                        val search = mediaRepository.searchAniList(_state.value.title)
                        currentMediaId = search.data?.Page?.media?.firstOrNull()?.id
                    } catch (e: Exception) { }
                }

                if (currentMediaId != null && !_state.value.isInWatchlist) {
                    try {
                        val aniResponse = mediaRepository.saveMediaListEntry(currentMediaId, MediaListStatus.PLANNING)
                        if (aniResponse.data?.SaveMediaListEntry != null) {
                            _state.update { it.copy(
                                isInWatchlist = true,
                                mediaId = currentMediaId,
                                requestMessage = "Requested & Added to Watchlist!"
                            ) }
                        } else {
                            _state.update { it.copy(requestMessage = "Requested! (AniList update failed)") }
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(requestMessage = "Requested! (AniList sync error)") }
                    }
                } else {
                    _state.update { it.copy(requestMessage = "Request Sent Successfully!") }
                }
            } else {
                _state.update { it.copy(requestMessage = "Request failed. Check server logs.") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(requestMessage = null) }
    }

    private fun isTitleMatch(aniTitle: String, seerrTitle: String): Boolean {
        // Clean titles: lowercase, remove non-alphanumeric, and remove common suffixes/prefixes
        fun clean(t: String) = t.lowercase()
            .replace(Regex("(?i)season \\d+"), "")
            .replace(Regex("(?i)part \\d+"), "")
            .replace(Regex("(?i)\\(tv\\)"), "")
            .replace(Regex("(?i)the movie"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()

        val s1 = clean(aniTitle)
        val s2 = clean(seerrTitle)
        
        if (s1.isEmpty() || s2.isEmpty()) return false
        
        // 1. Exact match after cleaning
        if (s1 == s2) return true
        
        // 2. Prevent matching short fragments
        if (s1.length < 4 || s2.length < 4) return false
        
        // 3. Containment check with length ratio (prevents "A" matching "Avatar")
        val ratio = if (s1.length > s2.length) s2.length.toFloat() / s1.length else s1.length.toFloat() / s2.length
        
        return (s1.contains(s2) || s2.contains(s1)) && ratio > 0.4f
    }
}
