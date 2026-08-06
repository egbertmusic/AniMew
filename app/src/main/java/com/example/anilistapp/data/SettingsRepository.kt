package com.example.anilistapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val SEERR_URL_KEY = stringPreferencesKey("seerr_url")
    private val SEERR_API_KEY_KEY = stringPreferencesKey("seerr_api_key")
    private val DISABLE_ANIME_UPDATE_KEY = booleanPreferencesKey("disable_anime_update")
    private val DISABLE_MANGA_UPDATE_KEY = booleanPreferencesKey("disable_manga_update")
    private val TITLE_LANGUAGE_KEY = stringPreferencesKey("title_language")
    private val SHOW_MULTIPLE_TITLES_KEY = booleanPreferencesKey("show_multiple_titles")
    private val SEERR_RADARR_SERVER_ID_KEY = intPreferencesKey("seerr_radarr_server_id")
    private val SEERR_SONARR_SERVER_ID_KEY = intPreferencesKey("seerr_sonarr_server_id")
    private val SEERR_RADARR_ROOT_FOLDER_KEY = stringPreferencesKey("seerr_radarr_root_folder")
    private val SEERR_SONARR_ROOT_FOLDER_KEY = stringPreferencesKey("seerr_sonarr_root_folder")
    private val APP_LANGUAGES_KEY = stringSetPreferencesKey("app_languages")
    private val RANDOMIZE_UI_LANGUAGE_KEY = booleanPreferencesKey("randomize_ui_language")
    private val ENABLE_SEERR_KEY = booleanPreferencesKey("enable_seerr")
    private val SHOW_SEERR_CLOUD_IN_LIBRARY_KEY = booleanPreferencesKey("show_seerr_cloud_in_library")
    private val AUTO_ADD_DOWNLOADED_TO_WATCHLIST_KEY = booleanPreferencesKey("auto_add_downloaded_to_watchlist")
    private val ENABLE_MEDIA_SERVER_FALLBACK_KEY = booleanPreferencesKey("enable_media_server_fallback")
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val JELLYFIN_URL_KEY = stringPreferencesKey("jellyfin_url")
    private val JELLYFIN_API_KEY_KEY = stringPreferencesKey("jellyfin_api_key")
    private val PLEX_URL_KEY = stringPreferencesKey("plex_url")
    private val PLEX_TOKEN_KEY = stringPreferencesKey("plex_token")
    private val CUSTOM_THEME_JSON_KEY = stringPreferencesKey("custom_theme_json")
    private val INSTALLED_COMPLEMENTS_KEY = stringSetPreferencesKey("installed_complements_urls")
    private val WIDGET_THEME_MODE_KEY = stringPreferencesKey("widget_theme_mode")
    private val SHOW_SEARCH_TAGS_KEY = booleanPreferencesKey("show_search_tags")
    private val JELLYFIN_LIBRARY_ID_KEY = stringPreferencesKey("jellyfin_library_id")
    private val PLEX_LIBRARY_ID_KEY = stringPreferencesKey("plex_library_id")
    private val MANUAL_AVAILABLE_IDS_KEY = stringSetPreferencesKey("manual_available_ids")
    private val ENABLE_DISCOVER_FEED_KEY = booleanPreferencesKey("enable_discover_feed")
    private val ENABLE_SHORTS_FEED_KEY = booleanPreferencesKey("enable_shorts_feed")
    private val ENABLE_PROFILE_TAB_KEY = booleanPreferencesKey("enable_profile_tab")
    private val GROUP_SEASONS_KEY = booleanPreferencesKey("group_seasons")
    private val SHOW_MORE_CONTENT_KEY = booleanPreferencesKey("show_more_content")
    private val SHOW_APP_TITLE_KEY = booleanPreferencesKey("show_app_title")
    private val SHORTS_NAVIGATION_STYLE_KEY = stringPreferencesKey("shorts_navigation_style")
    private val SHORTS_FEED_SOURCE_KEY = stringPreferencesKey("shorts_feed_source")
    private val SHORTS_FEED_TYPE_KEY = stringPreferencesKey("shorts_feed_type")
    private val ENABLE_MEWING_CHAD_KEY = booleanPreferencesKey("enable_mewing_chad")
    private val MIN_COMMUNITY_SCORE_KEY = intPreferencesKey("min_community_score")
    private val ENABLE_SFX_KEY = booleanPreferencesKey("enable_sfx")
    private val ENABLE_BGM_KEY = booleanPreferencesKey("enable_bgm")
    private val PREFERRED_TRAILER_LANGUAGE_KEY = stringPreferencesKey("preferred_trailer_language")
    private val ENABLE_LOCALIZED_CONTENT_KEY = booleanPreferencesKey("enable_localized_content")
    private val PRIMARY_APP_LANGUAGE_KEY = stringPreferencesKey("primary_app_language")

    val enableDiscoverFeed: Flow<Boolean> = dataStore.data.map { it[ENABLE_DISCOVER_FEED_KEY] ?: true }
    val enableShortsFeed: Flow<Boolean> = dataStore.data.map { it[ENABLE_SHORTS_FEED_KEY] ?: true }
    val enableProfileTab: Flow<Boolean> = dataStore.data.map { it[ENABLE_PROFILE_TAB_KEY] ?: true }
    val groupSeasons: Flow<Boolean> = dataStore.data.map { it[GROUP_SEASONS_KEY] ?: true }
    val showMoreContent: Flow<Boolean> = dataStore.data.map { it[SHOW_MORE_CONTENT_KEY] ?: true }
    val showAppTitle: Flow<Boolean> = dataStore.data.map { it[SHOW_APP_TITLE_KEY] ?: true }
    val shortsNavigationStyle: Flow<String> = dataStore.data.map { it[SHORTS_NAVIGATION_STYLE_KEY] ?: "BOTTOM" }
    val shortsFeedSource: Flow<String> = dataStore.data.map { it[SHORTS_FEED_SOURCE_KEY] ?: "TRENDING" }
    val shortsFeedType: Flow<String> = dataStore.data.map { it[SHORTS_FEED_TYPE_KEY] ?: "ANIME" }
    val enableMewingChad: Flow<Boolean> = dataStore.data.map { it[ENABLE_MEWING_CHAD_KEY] ?: false }
    val minCommunityScore: Flow<Int> = dataStore.data.map { it[MIN_COMMUNITY_SCORE_KEY] ?: 70 }
    val enableSfx: Flow<Boolean> = dataStore.data.map { it[ENABLE_SFX_KEY] ?: true }
    val enableBgm: Flow<Boolean> = dataStore.data.map { it[ENABLE_BGM_KEY] ?: false }
    val preferredTrailerLanguage: Flow<String> = dataStore.data.map { it[PREFERRED_TRAILER_LANGUAGE_KEY] ?: "JAPANESE" }
    val enableLocalizedContent: Flow<Boolean> = dataStore.data.map { it[ENABLE_LOCALIZED_CONTENT_KEY] ?: true }
    val primaryAppLanguage: Flow<String> = dataStore.data.map { it[PRIMARY_APP_LANGUAGE_KEY] ?: "ENGLISH" }

    val seerrUrl: Flow<String> = dataStore.data.map { prefs ->
        prefs[SEERR_URL_KEY] ?: ""
    }

    val seerrApiKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[SEERR_API_KEY_KEY] ?: ""
    }

    val disableAnimeUpdate: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DISABLE_ANIME_UPDATE_KEY] ?: false
    }

    val disableMangaUpdate: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DISABLE_MANGA_UPDATE_KEY] ?: false
    }

    val titleLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[TITLE_LANGUAGE_KEY] ?: "ROMAJI"
    }

    val showMultipleTitles: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SHOW_MULTIPLE_TITLES_KEY] ?: false
    }

    val seerrRadarrServerId: Flow<Int?> = dataStore.data.map { it[SEERR_RADARR_SERVER_ID_KEY] }
    val seerrSonarrServerId: Flow<Int?> = dataStore.data.map { it[SEERR_SONARR_SERVER_ID_KEY] }
    val seerrRadarrRootFolder: Flow<String> = dataStore.data.map { it[SEERR_RADARR_ROOT_FOLDER_KEY] ?: "" }
    val seerrSonarrRootFolder: Flow<String> = dataStore.data.map { it[SEERR_SONARR_ROOT_FOLDER_KEY] ?: "" }

    val appLanguages: Flow<Set<String>> = dataStore.data.map { it[APP_LANGUAGES_KEY] ?: setOf("ENGLISH") }
    val randomizeUiLanguage: Flow<Boolean> = dataStore.data.map { it[RANDOMIZE_UI_LANGUAGE_KEY] ?: false }
    val enableSeerr: Flow<Boolean> = dataStore.data.map { it[ENABLE_SEERR_KEY] ?: false }
    val showSeerrCloudInLibrary: Flow<Boolean> = dataStore.data.map { it[SHOW_SEERR_CLOUD_IN_LIBRARY_KEY] ?: true }
    val autoAddDownloadedToWatchlist: Flow<Boolean> = dataStore.data.map { it[AUTO_ADD_DOWNLOADED_TO_WATCHLIST_KEY] ?: false }
    val enableMediaServerFallback: Flow<Boolean> = dataStore.data.map { it[ENABLE_MEDIA_SERVER_FALLBACK_KEY] ?: true }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE_KEY] ?: "DARK" }
    
    val jellyfinUrl: Flow<String> = dataStore.data.map { it[JELLYFIN_URL_KEY] ?: "" }
    val jellyfinApiKey: Flow<String> = dataStore.data.map { it[JELLYFIN_API_KEY_KEY] ?: "" }
    val plexUrl: Flow<String> = dataStore.data.map { it[PLEX_URL_KEY] ?: "" }
    val plexToken: Flow<String> = dataStore.data.map { it[PLEX_TOKEN_KEY] ?: "" }

    val customThemeJson: Flow<String?> = dataStore.data.map { it[CUSTOM_THEME_JSON_KEY] }
    val installedComplementsUrls: Flow<Set<String>> = dataStore.data.map { it[INSTALLED_COMPLEMENTS_KEY] ?: emptySet() }
    val widgetThemeMode: Flow<String> = dataStore.data.map { it[WIDGET_THEME_MODE_KEY] ?: "DARK" }
    val showSearchTags: Flow<Boolean> = dataStore.data.map { it[SHOW_SEARCH_TAGS_KEY] ?: true }
    val jellyfinLibraryId: Flow<String> = dataStore.data.map { it[JELLYFIN_LIBRARY_ID_KEY] ?: "" }
    val plexLibraryId: Flow<String> = dataStore.data.map { it[PLEX_LIBRARY_ID_KEY] ?: "" }
    val manualAvailableIds: Flow<Set<Int>> = dataStore.data.map { 
        (it[MANUAL_AVAILABLE_IDS_KEY] ?: emptySet()).mapNotNull { id -> id.toIntOrNull() }.toSet() 
    }

    suspend fun saveSeerrSettings(url: String, apiKey: String) {
        dataStore.edit { prefs ->
            prefs[SEERR_URL_KEY] = url
            prefs[SEERR_API_KEY_KEY] = apiKey
        }
    }

    suspend fun setDisableAnimeUpdate(disable: Boolean) {
        dataStore.edit { prefs ->
            prefs[DISABLE_ANIME_UPDATE_KEY] = disable
        }
    }

    suspend fun setDisableMangaUpdate(disable: Boolean) {
        dataStore.edit { prefs ->
            prefs[DISABLE_MANGA_UPDATE_KEY] = disable
        }
    }

    suspend fun setTitleLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[TITLE_LANGUAGE_KEY] = language
        }
    }

    suspend fun setShowMultipleTitles(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[SHOW_MULTIPLE_TITLES_KEY] = show
        }
    }

    suspend fun setSeerrAdvancedSettings(
        radarrId: Int?,
        sonarrId: Int?,
        radarrRoot: String,
        sonarrRoot: String
    ) {
        dataStore.edit { prefs ->
            if (radarrId != null) prefs[SEERR_RADARR_SERVER_ID_KEY] = radarrId
            if (sonarrId != null) prefs[SEERR_SONARR_SERVER_ID_KEY] = sonarrId
            prefs[SEERR_RADARR_ROOT_FOLDER_KEY] = radarrRoot
            prefs[SEERR_SONARR_ROOT_FOLDER_KEY] = sonarrRoot
        }
    }

    suspend fun setAppLanguages(languages: Set<String>) {
        dataStore.edit { prefs ->
            prefs[APP_LANGUAGES_KEY] = languages
        }
    }

    suspend fun setRandomizeUiLanguage(randomize: Boolean) {
        dataStore.edit { prefs ->
            prefs[RANDOMIZE_UI_LANGUAGE_KEY] = randomize
        }
    }

    suspend fun setEnableSeerr(enable: Boolean) {
        dataStore.edit { prefs ->
            prefs[ENABLE_SEERR_KEY] = enable
        }
    }

    suspend fun setShowSeerrCloudInLibrary(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[SHOW_SEERR_CLOUD_IN_LIBRARY_KEY] = show
        }
    }

    suspend fun setAutoAddDownloadedToWatchlist(autoAdd: Boolean) {
        dataStore.edit { prefs ->
            prefs[AUTO_ADD_DOWNLOADED_TO_WATCHLIST_KEY] = autoAdd
        }
    }

    suspend fun setEnableMediaServerFallback(enable: Boolean) {
        dataStore.edit { prefs ->
            prefs[ENABLE_MEDIA_SERVER_FALLBACK_KEY] = enable
        }
    }

    suspend fun setThemeMode(theme: String) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = theme
        }
    }

    suspend fun saveJellyfinSettings(url: String, apiKey: String) {
        dataStore.edit { prefs ->
            prefs[JELLYFIN_URL_KEY] = url
            prefs[JELLYFIN_API_KEY_KEY] = apiKey
        }
    }

    suspend fun savePlexSettings(url: String, token: String) {
        dataStore.edit { prefs ->
            prefs[PLEX_URL_KEY] = url
            prefs[PLEX_TOKEN_KEY] = token
        }
    }

    suspend fun setCustomThemeJson(json: String) {
        dataStore.edit { prefs ->
            prefs[CUSTOM_THEME_JSON_KEY] = json
        }
    }

    suspend fun installComplement(url: String) {
        dataStore.edit { prefs ->
            val current = prefs[INSTALLED_COMPLEMENTS_KEY] ?: emptySet()
            prefs[INSTALLED_COMPLEMENTS_KEY] = current + url
        }
    }

    suspend fun uninstallComplement(url: String) {
        dataStore.edit { prefs ->
            val current = prefs[INSTALLED_COMPLEMENTS_KEY] ?: emptySet()
            prefs[INSTALLED_COMPLEMENTS_KEY] = current - url
        }
    }

    suspend fun setWidgetThemeMode(theme: String) {
        dataStore.edit { prefs ->
            prefs[WIDGET_THEME_MODE_KEY] = theme
        }
    }

    suspend fun setShowSearchTags(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[SHOW_SEARCH_TAGS_KEY] = show
        }
    }

    suspend fun setJellyfinLibraryId(id: String) {
        dataStore.edit { it[JELLYFIN_LIBRARY_ID_KEY] = id }
    }

    suspend fun setPlexLibraryId(id: String) {
        dataStore.edit { it[PLEX_LIBRARY_ID_KEY] = id }
    }

    suspend fun setEnableDiscoverFeed(enable: Boolean) {
        dataStore.edit { it[ENABLE_DISCOVER_FEED_KEY] = enable }
    }

    suspend fun setEnableShortsFeed(enable: Boolean) {
        dataStore.edit { it[ENABLE_SHORTS_FEED_KEY] = enable }
    }

    suspend fun setEnableProfileTab(enable: Boolean) {
        dataStore.edit { it[ENABLE_PROFILE_TAB_KEY] = enable }
    }

    suspend fun setGroupSeasons(group: Boolean) {
        dataStore.edit { it[GROUP_SEASONS_KEY] = group }
    }

    suspend fun setShowMoreContent(show: Boolean) {
        dataStore.edit { it[SHOW_MORE_CONTENT_KEY] = show }
    }

    suspend fun setShowAppTitle(show: Boolean) {
        dataStore.edit { it[SHOW_APP_TITLE_KEY] = show }
    }

    suspend fun setShortsNavigationStyle(style: String) {
        dataStore.edit { it[SHORTS_NAVIGATION_STYLE_KEY] = style }
    }

    suspend fun setShortsFeedSource(source: String) {
        dataStore.edit { it[SHORTS_FEED_SOURCE_KEY] = source }
    }

    suspend fun setShortsFeedType(type: String) {
        dataStore.edit { it[SHORTS_FEED_TYPE_KEY] = type }
    }

    suspend fun setEnableMewingChad(enable: Boolean) {
        dataStore.edit { it[ENABLE_MEWING_CHAD_KEY] = enable }
    }

    suspend fun setMinCommunityScore(score: Int) {
        dataStore.edit { it[MIN_COMMUNITY_SCORE_KEY] = score }
    }

    suspend fun setEnableSfx(enable: Boolean) {
        dataStore.edit { it[ENABLE_SFX_KEY] = enable }
    }

    suspend fun setEnableBgm(enable: Boolean) {
        dataStore.edit { it[ENABLE_BGM_KEY] = enable }
    }

    suspend fun setPreferredTrailerLanguage(language: String) {
        dataStore.edit { it[PREFERRED_TRAILER_LANGUAGE_KEY] = language }
    }

    suspend fun setEnableLocalizedContent(enable: Boolean) {
        dataStore.edit { it[ENABLE_LOCALIZED_CONTENT_KEY] = enable }
    }

    suspend fun setPrimaryAppLanguage(language: String) {
        dataStore.edit { it[PRIMARY_APP_LANGUAGE_KEY] = language }
    }

    suspend fun toggleManualAvailable(mediaId: Int) {
        dataStore.edit { prefs ->
            val current = prefs[MANUAL_AVAILABLE_IDS_KEY] ?: emptySet()
            val idStr = mediaId.toString()
            if (current.contains(idStr)) {
                prefs[MANUAL_AVAILABLE_IDS_KEY] = current - idStr
            } else {
                prefs[MANUAL_AVAILABLE_IDS_KEY] = current + idStr
            }
        }
    }
}
