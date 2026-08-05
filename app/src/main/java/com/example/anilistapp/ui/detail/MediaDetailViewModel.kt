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

data class RelatedMedia(
    val id: Int,
    val title: String,
    val relationType: String,
    val type: String, // ANIME, MANGA
    val format: String?,
    val coverImage: String? = null
)

data class MediaDetailState(
    val mediaId: Int? = null,
    val isInWatchlist: Boolean = false,
    val title: String = "",
    val synopsis: String = "",
    val posterUrl: String? = null,
    val kitsuDetails: KitsuSearchResult? = null,
    val seerrMatch: SeerrSearchResult? = null,
    val seerrSeasons: List<com.example.anilistapp.data.SeerrSeasonInfo> = emptyList(),
    val availableSeasons: Set<Int> = emptySet(),
    val selectedSeasons: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isSeerrEnabled: Boolean = false,
    val seerrLoading: Boolean = false,
    val seerrError: String? = null,
    val seerrProfiles: List<SeerrProfile> = emptyList(),
    val selectedProfileId: Int? = null,
    val requestMessage: String? = null,
    val titleLanguage: String = "ROMAJI",
    val showMultipleTitles: Boolean = false,
    val appLanguages: Set<String> = setOf("ENGLISH"),
    val randomizeUiLanguage: Boolean = false,
    val streamLinks: List<Pair<String, String>> = emptyList(), // Name to URL
    val extraMetadata: Map<String, String> = emptyMap(),
    val youtubeVideoId: String? = null,
    val relatedMedia: List<RelatedMedia> = emptyList(),
    val showMoreContentSection: Boolean = true
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
            _state.update { it.copy(title = title, mediaId = mediaId, isLoading = true, seerrLoading = true, seerrError = null) }
            
            val preferredType = try { 
                mediaTypeStr?.let { com.example.anilistapp.type.MediaType.valueOf(it) } 
            } catch (e: Exception) { null }
            
            val aniMedia = fetchAniListDetails(title, mediaId, preferredType)
            
            val finalTitle = aniMedia?.title?.userPreferred ?: title
            val romajiTitle = aniMedia?.title?.romaji
            val englishTitle = aniMedia?.title?.english
            val finalSynopsis = aniMedia?.description ?: ""
            val finalPosterUrl = aniMedia?.coverImage?.extraLarge
            val finalTrailerId = if (aniMedia?.trailer?.site == "youtube") aniMedia.trailer.id else null
            val mediaType = aniMedia?.type
            val mediaFormat = aniMedia?.format?.name
            val alreadyInList = aniMedia?.mediaListEntry != null
            
            val aniListTmdbId = aniMedia?.externalLinks?.filterNotNull()
                ?.find { it.site.contains("TMDB", ignoreCase = true) || it.url.contains("themoviedb.org", ignoreCase = true) }
                ?.url?.removeSuffix("/")?.split("/")?.lastOrNull()?.let { lastPart ->
                    Regex("^(\\d+)").find(lastPart)?.groupValues?.get(1)?.toIntOrNull()
                }
            
            val aniListTvdbId = aniMedia?.externalLinks?.filterNotNull()
                ?.find { it.site.contains("TheTVDB", ignoreCase = true) || it.site.equals("TVDB", ignoreCase = true) || it.url.contains("thetvdb.com", ignoreCase = true) }
                ?.url?.removeSuffix("/")?.split("/")?.lastOrNull()?.let { lastPart ->
                    Regex("^(\\d+)").find(lastPart)?.groupValues?.get(1)?.toIntOrNull()
                }

            aniMedia?.externalLinks?.filterNotNull()?.forEach { link ->
                Log.d("MediaDetailVM", "External Link: Site='${link.site}', URL='${link.url}'")
            }

            val related = aniMedia?.relations?.edges?.filterNotNull()?.mapNotNull { edge ->
                val node = edge.node ?: return@mapNotNull null
                RelatedMedia(
                    id = node.id,
                    title = node.title?.userPreferred ?: "Unknown",
                    relationType = edge.relationType?.name ?: "UNKNOWN",
                    type = node.type?.name ?: "ANIME",
                    format = node.format?.name,
                    coverImage = node.coverImage?.large
                )
            } ?: emptyList()

            val kitsu = kitsuRepository.getDetailsByTitle(finalTitle, mediaType != com.example.anilistapp.type.MediaType.MANGA)

            val seerrData = fetchSeerrDetails(finalTitle, romajiTitle, englishTitle, kitsu?.title, aniListTmdbId, aniListTvdbId, mediaFormat)

            val language = settingsRepository.titleLanguage.first()
            val multiple = settingsRepository.showMultipleTitles.first()
            val appLangs = settingsRepository.appLanguages.first()
            val randomize = settingsRepository.randomizeUiLanguage.first()
            val showMore = settingsRepository.showMoreContent.first()
            
            val finalYoutubeId = finalTrailerId ?: kitsu?.youtubeVideoId

            _state.update { it.copy(
                mediaId = aniMedia?.id ?: mediaId,
                isInWatchlist = alreadyInList,
                title = finalTitle,
                synopsis = finalSynopsis,
                posterUrl = finalPosterUrl,
                kitsuDetails = kitsu,
                seerrMatch = seerrData.match,
                seerrSeasons = seerrData.seasons,
                availableSeasons = seerrData.availableSeasons,
                selectedSeasons = seerrData.seasons.map { it.seasonNumber }.filter { !seerrData.availableSeasons.contains(it) }.toSet(),
                isSeerrEnabled = seerrData.isEnabled,
                seerrLoading = false,
                seerrError = seerrData.error,
                seerrProfiles = seerrData.profiles,
                selectedProfileId = seerrData.profiles.firstOrNull()?.id,
                titleLanguage = language,
                showMultipleTitles = multiple,
                appLanguages = appLangs,
                randomizeUiLanguage = randomize,
                isLoading = false,
                youtubeVideoId = if (finalYoutubeId.isNullOrEmpty()) null else finalYoutubeId,
                relatedMedia = related,
                showMoreContentSection = showMore
            ) }

            loadComplements(finalTitle, romajiTitle ?: finalTitle, mediaType)
        }
    }

    private suspend fun fetchAniListDetails(title: String, mediaId: Int?, preferredType: com.example.anilistapp.type.MediaType?): com.example.anilistapp.GetMediaDetailsQuery.Media? {
        try {
            if (mediaId != null) {
                val detailResponse = mediaRepository.getMediaDetails(mediaId)
                detailResponse.data?.Media?.let { return it }
            }
            
            val aniSearch = mediaRepository.searchAniList(title, preferredType)
            val results = aniSearch.data?.Page?.media?.filterNotNull() ?: emptyList()
            val topResult = if (preferredType != null) {
                results.find { it.type == preferredType } ?: results.firstOrNull()
            } else {
                results.find { it.type == com.example.anilistapp.type.MediaType.ANIME } ?: results.firstOrNull()
            }
            
            if (topResult != null) {
                return mediaRepository.getMediaDetails(topResult.id).data?.Media
            }
        } catch (e: Exception) {
            Log.e("MediaDetailVM", "AniList loading failed", e)
        }
        return null
    }

    private data class SeerrData(
        val isEnabled: Boolean = false,
        val match: SeerrSearchResult? = null,
        val seasons: List<com.example.anilistapp.data.SeerrSeasonInfo> = emptyList(),
        val availableSeasons: Set<Int> = emptySet(),
        val profiles: List<SeerrProfile> = emptyList(),
        val error: String? = null
    )

    private suspend fun fetchSeerrDetails(
        title: String,
        romaji: String?,
        english: String?,
        kitsuTitle: String?,
        tmdbId: Int?,
        tvdbId: Int?,
        format: String?
    ): SeerrData {
        val seerrEnabled = settingsRepository.enableSeerr.first()
        val seerrUrl = settingsRepository.seerrUrl.first()
        val isSeerrConfigured = seerrEnabled && seerrUrl.isNotEmpty()
        
        if (!isSeerrConfigured || format == "MANGA") return SeerrData()

        try {
            Log.d("MediaDetailVM", "Seerr matching for: $title, TMDB: $tmdbId, TVDB: $tvdbId, Format: $format")

            val globalStatuses = try {
                seerrRepository.getAllMediaStatuses()
            } catch (e: Exception) {
                emptyMap<String, Int>()
            }
            
            val preferredSeerrType = if (format == "MOVIE") "movie" else "tv"
            var seerrMatch: SeerrSearchResult? = null

            // 1. TMDB ID Match
            if (tmdbId != null) {
                val typesToCheck = listOf(preferredSeerrType, if (preferredSeerrType == "tv") "movie" else "tv")
                for (type in typesToCheck) {
                    val status = globalStatuses["${type}_$tmdbId"]
                    if (status != null && status >= 5) {
                        seerrMatch = SeerrSearchResult(id = tmdbId, title = title, type = type, overview = "Available in your library", status = status)
                        break
                    }
                }

                if (seerrMatch == null) {
                    for (type in typesToCheck) {
                        seerrMatch = seerrRepository.getMediaDetails(tmdbId, type)
                        if (seerrMatch != null) break
                    }
                }
                
                if (seerrMatch == null) {
                    seerrMatch = seerrRepository.findInCache(tmdbId, title)
                }
            }

            // 2. TVDB ID Match
            if (seerrMatch == null && tvdbId != null) {
                Log.d("MediaDetailVM", "Trying TVDB lookup for: $tvdbId")
                seerrMatch = seerrRepository.lookupByExternalId("tvdb", tvdbId)
            }

            // 3. Title Match
            if (seerrMatch == null) {
                val baseTitles = listOfNotNull(english, title, romaji, kitsuTitle).distinct()
                val searchQueries = mutableListOf<String>()
                baseTitles.forEach { t ->
                    searchQueries.add(t)
                    val cleaned = t.replace(Regex("\\(.*?\\)"), "")
                        .replace(Regex("(?i)\\s+(season|part|cour|vol|volume)\\s+\\d+.*"), "")
                        .replace(Regex("(?i)\\s+\\d+$"), "")
                        .trim()
                    searchQueries.add(cleaned)
                    if (t.contains(":")) searchQueries.add(t.substringBefore(":").trim())
                }
                
                val finalQueries = searchQueries.distinct()
                Log.d("MediaDetailVM", "Trying search queries: $finalQueries")

                for (query in finalQueries) {
                    if (query.length < 2) continue
                    val results = seerrRepository.searchShow(query)
                    Log.d("MediaDetailVM", "Search for '$query' returned ${results.size} items")
                    
                    // Try to find a match across all titles we have
                    val match = results.find { result ->
                        baseTitles.any { baseTitle -> isTitleMatch(baseTitle, result.title) } ||
                        isTitleMatch(query, result.title)
                    }

                    if (match != null) {
                        Log.d("MediaDetailVM", "Match found: ${match.title} (${match.type})")
                        seerrMatch = seerrRepository.getMediaDetails(match.id, match.type) ?: match
                        break
                    }
                }
            }

            // 4. Profiles and Seasons
            var profiles = emptyList<SeerrProfile>()
            var seasons = emptyList<com.example.anilistapp.data.SeerrSeasonInfo>()
            var availableSeasons = emptySet<Int>()
            
            seerrMatch?.let { match ->
                Log.d("MediaDetailVM", "Final Match: ${match.title} ID: ${match.id} Type: ${match.type}")
                
                if ((match.status ?: 1) < 5 && settingsRepository.enableMediaServerFallback.first()) {
                    val titlesToTry = listOfNotNull(english, title, romaji, kitsuTitle).distinct()
                    var foundInMediaServer = false
                    for (st in titlesToTry) {
                        if (jellyfinRepository.searchItem(st) || plexRepository.searchItem(st)) {
                            foundInMediaServer = true
                            break
                        }
                    }
                    if (foundInMediaServer) {
                        seerrMatch = match.copy(status = 6, overview = "Detected via media server (Jellyfin/Plex)")
                    }
                }

                val servers = if (match.type == "movie") seerrRepository.getRadarrSettings() else seerrRepository.getSonarrSettings()
                val defaultServer = servers.firstOrNull { it.isDefault } ?: servers.firstOrNull()
                
                if (defaultServer != null) {
                    profiles = seerrRepository.getProfilesForServer(match.type, defaultServer.id)
                } else if (servers.isNotEmpty()) {
                    profiles = seerrRepository.getProfilesForServer(match.type, servers.first().id)
                }
                
                if (profiles.isEmpty()) profiles = seerrRepository.getQualityProfiles()

                if (profiles.isEmpty()) return SeerrData(isEnabled = true, match = match, error = "Match found (${match.title}), but no quality profiles found on Seerr.")

                // Always fetch seasons for TV shows to check for partial availability
                if (match.type == "tv") {
                    val details = seerrRepository.getShowDetails(match.id, "tv")
                    seasons = details.first.takeIf { it.isNotEmpty() } ?: listOf(com.example.anilistapp.data.SeerrSeasonInfo(1, null))
                    availableSeasons = details.second
                    Log.d("MediaDetailVM", "Seasons for ${match.title}: All=$seasons, Available=$availableSeasons")
                }
            } ?: return SeerrData(isEnabled = true, error = "Could not find a match on Seerr for '$title'.")

            return SeerrData(isEnabled = true, match = seerrMatch, seasons = seasons, availableSeasons = availableSeasons, profiles = profiles)
        } catch (e: Exception) {
            Log.e("MediaDetailVM", "Seerr matching failed", e)
            return SeerrData(isEnabled = true, error = "Connection error: ${e.message}")
        }
    }

    private fun loadComplements(title: String, romaji: String, type: com.example.anilistapp.type.MediaType?) {
        viewModelScope.launch {
            complementRepository.installedComplements.collect { complements ->
                val streamLinks = mutableListOf<Pair<String, String>>()
                val extraMetadata = mutableMapOf<String, String>()
                
                complements.forEach { comp ->
                    comp.streamProviders.forEach { prov ->
                        if (prov.type == type?.name || (type == null && prov.type == "ANIME")) {
                            val url = prov.watchUrl.replace("%s", java.net.URLEncoder.encode(romaji, "UTF-8"))
                            streamLinks.add(prov.name to url)
                        }
                    }
                    comp.metadataProviders.forEach { prov ->
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

    fun selectAllSeasons() {
        _state.update { it.copy(selectedSeasons = it.seerrSeasons.map { s -> s.seasonNumber }.filter { num -> !it.availableSeasons.contains(num) }.toSet()) }
    }

    fun deselectAllSeasons() {
        _state.update { it.copy(selectedSeasons = emptySet()) }
    }

    fun switchSeason(mediaId: Int) {
        val targetMedia = _state.value.relatedMedia.find { it.id == mediaId }
        val title = targetMedia?.title ?: _state.value.title
        loadDetails(title, mediaId)
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
            try {
                val isTv = match.type == "tv"
                val serverId = if (isTv) {
                    settingsRepository.seerrSonarrServerId.first()
                } else {
                    settingsRepository.seerrRadarrServerId.first()
                }
                
                val rootFolder = if (isTv) {
                    settingsRepository.seerrSonarrRootFolder.first()
                } else {
                    settingsRepository.seerrRadarrRootFolder.first()
                }

                if (serverId == null || serverId == -1) {
                    _state.update { it.copy(requestMessage = "Error: No ${if(isTv) "Sonarr" else "Radarr"} server ID configured in settings.") }
                    return@launch
                }

                _state.update { it.copy(requestMessage = "Sending request to Seerr...") }

                Log.d("MediaDetailVM", "Requesting ${match.title} (${match.type}) with ID ${match.id}, Seasons: ${_state.value.selectedSeasons}")

                val success = seerrRepository.requestMedia(
                    tmdbId = match.id,
                    type = match.type,
                    seasons = _state.value.selectedSeasons.toList(),
                    profileId = profileId,
                    serverId = serverId,
                    rootFolder = rootFolder.takeIf { it.isNotEmpty() }
                )

                if (success) {
                    val currentMediaId = _state.value.mediaId
                    if (currentMediaId != null && !_state.value.isInWatchlist) {
                        mediaRepository.saveMediaListEntry(currentMediaId, MediaListStatus.PLANNING)
                        _state.update { it.copy(isInWatchlist = true, requestMessage = "Requested & Added to Watchlist!") }
                    } else {
                        _state.update { it.copy(requestMessage = "Request Sent Successfully!") }
                    }
                } else {
                    _state.update { it.copy(requestMessage = "Request failed. Check Seerr settings.") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(requestMessage = "Request Error: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(requestMessage = null) }
    }

    private fun isTitleMatch(query: String, resultTitle: String): Boolean {
        fun clean(t: String) = t.lowercase()
            .replace(Regex("\\(.*?\\)"), "") 
            .replace(Regex("(?i)season \\d+"), "")
            .replace(Regex("(?i)part \\d+"), "")
            .replace(Regex("(?i)cour \\d+"), "")
            .replace(Regex("(?i)\\(tv\\)"), "")
            .replace(Regex("(?i)the movie"), "")
            .replace(Regex("[^\\p{L}\\p{N}]"), "") // Remove EVERYTHING except letters and numbers (works for Japanese too)
            .trim()

        val s1 = clean(query)
        val s2 = clean(resultTitle)
        
        val match = s1.isNotEmpty() && s2.isNotEmpty() && (s1 == s2 || s1.contains(s2) || s2.contains(s1))
        
        Log.d("MediaDetailVM", "Comparing: '$query' -> '$s1' vs '$resultTitle' -> '$s2' | Match: $match")
        
        return match
    }
}
