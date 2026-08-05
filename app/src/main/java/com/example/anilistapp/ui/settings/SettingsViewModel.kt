package com.example.anilistapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.data.SeerrRepository
import com.example.anilistapp.data.SettingsRepository
import com.example.anilistapp.ui.components.LocalizationManager
import com.example.anilistapp.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val seerrUrl: String = "",
    val seerrApiKey: String = "",
    val seerrRadarrServerId: Int? = null,
    val seerrSonarrServerId: Int? = null,
    val seerrRadarrRootFolder: String = "",
    val seerrSonarrRootFolder: String = "",
    val disableAnimeUpdate: Boolean = false,
    val disableMangaUpdate: Boolean = false,
    val jellyfinUrl: String = "",
    val jellyfinApiKey: String = "",
    val plexUrl: String = "",
    val plexToken: String = "",
    val titleLanguage: String = "ROMAJI",
    val showMultipleTitles: Boolean = false,
    val appLanguages: Set<String> = setOf("ENGLISH"),
    val randomizeUiLanguage: Boolean = false,
    val enableSeerr: Boolean = false,
    val showSeerrCloudInLibrary: Boolean = true,
    val autoAddDownloadedToWatchlist: Boolean = false,
    val enableMediaServerFallback: Boolean = true,
    val showSearchTags: Boolean = true,
    val themeMode: AppTheme = AppTheme.DARK,
    val widgetThemeMode: AppTheme = AppTheme.DARK,
    val enableDiscoverFeed: Boolean = true,
    val enableProfileTab: Boolean = true,
    val groupSeasons: Boolean = true,
    val showMoreContent: Boolean = true,
    val jellyfinLibraryId: String = "",
    val plexLibraryId: String = "",
    val jellyfinLibraries: List<Pair<String, String>> = emptyList(),
    val plexLibraries: List<Pair<String, String>> = emptyList(),
    val customTheme: com.example.anilistapp.ui.theme.CustomTheme? = null,
    val customSources: List<com.example.anilistapp.data.CustomSource> = emptyList(),
    val seerrTestResult: String? = null,
    val jellyfinTestResult: String? = null,
    val plexTestResult: String? = null,
    val availableSonarrServers: List<com.example.anilistapp.data.SeerrServer> = emptyList(),
    val availableRadarrServers: List<com.example.anilistapp.data.SeerrServer> = emptyList(),
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val seerrRepository: SeerrRepository,
    private val jellyfinRepository: com.example.anilistapp.data.JellyfinRepository,
    private val plexRepository: com.example.anilistapp.data.PlexRepository,
    private val complementRepository: com.example.anilistapp.data.ComplementRepository,
    val localizationManager: LocalizationManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val url = repository.seerrUrl.first()
            val apiKey = repository.seerrApiKey.first()
            val disableAnime = repository.disableAnimeUpdate.first()
            val disableManga = repository.disableMangaUpdate.first()
            val language = repository.titleLanguage.first()
            val multipleTitles = repository.showMultipleTitles.first()
            val radarrId = repository.seerrRadarrServerId.first()
            val sonarrId = repository.seerrSonarrServerId.first()
            val radarrRoot = repository.seerrRadarrRootFolder.first()
            val sonarrRoot = repository.seerrSonarrRootFolder.first()
            val appLangs = repository.appLanguages.first()
            val randomize = repository.randomizeUiLanguage.first()
            val seerrEnabled = repository.enableSeerr.first()
            val mediaFallback = repository.enableMediaServerFallback.first()
            val jellyfinUrl = repository.jellyfinUrl.first()
            val jellyfinApiKey = repository.jellyfinApiKey.first()
            val plexUrl = repository.plexUrl.first()
            val plexToken = repository.plexToken.first()
            val showCloud = repository.showSeerrCloudInLibrary.first()
            val autoAdd = repository.autoAddDownloadedToWatchlist.first()
            val tags = repository.showSearchTags.first()
            val theme = repository.themeMode.first()
            val customThemeJson = repository.customThemeJson.first()
            val widgetTheme = repository.widgetThemeMode.first()
            val jLib = repository.jellyfinLibraryId.first()
            val pLib = repository.plexLibraryId.first()
            val discoverFeed = repository.enableDiscoverFeed.first()
            val profileTab = repository.enableProfileTab.first()
            val group = repository.groupSeasons.first()
            val moreContent = repository.showMoreContent.first()

            val customTheme = customThemeJson?.let {
                try { kotlinx.serialization.json.Json.decodeFromString<com.example.anilistapp.ui.theme.CustomTheme>(it) } catch (e: Exception) { null }
            }
            
            _state.value = _state.value.copy(
                seerrUrl = url,
                seerrApiKey = apiKey,
                seerrRadarrServerId = radarrId,
                seerrSonarrServerId = sonarrId,
                seerrRadarrRootFolder = radarrRoot,
                seerrSonarrRootFolder = sonarrRoot,
                disableAnimeUpdate = disableAnime,
                disableMangaUpdate = disableManga,
                titleLanguage = language,
                showMultipleTitles = multipleTitles,
                appLanguages = appLangs,
                randomizeUiLanguage = randomize,
                enableSeerr = seerrEnabled,
                enableMediaServerFallback = mediaFallback,
                jellyfinUrl = jellyfinUrl,
                jellyfinApiKey = jellyfinApiKey,
                plexUrl = plexUrl,
                plexToken = plexToken,
                jellyfinLibraryId = jLib,
                plexLibraryId = pLib,
                showSeerrCloudInLibrary = showCloud,
                autoAddDownloadedToWatchlist = autoAdd,
                showSearchTags = tags,
                themeMode = AppTheme.valueOf(theme),
                customTheme = customTheme,
                widgetThemeMode = AppTheme.valueOf(widgetTheme),
                enableDiscoverFeed = discoverFeed,
                enableProfileTab = profileTab,
                groupSeasons = group,
                showMoreContent = moreContent
            )
            
            if (jellyfinUrl.isNotEmpty() && jellyfinApiKey.isNotEmpty()) {
                fetchJellyfinLibraries()
            }
            if (plexUrl.isNotEmpty() && plexToken.isNotEmpty()) {
                fetchPlexLibraries()
            }

            // Watch for complements
            viewModelScope.launch {
                complementRepository.installedComplements.collect { list ->
                    _state.value = _state.value.copy(customSources = list.flatMap { it.searchProviders })
                }
            }
        }
    }

    fun onUrlChanged(url: String) {
        _state.value = _state.value.copy(seerrUrl = url, isSaved = false)
    }

    fun onApiKeyChanged(apiKey: String) {
        _state.value = _state.value.copy(seerrApiKey = apiKey, isSaved = false)
    }

    fun onDisableAnimeUpdateChanged(disable: Boolean) {
        _state.value = _state.value.copy(disableAnimeUpdate = disable, isSaved = false)
    }

    fun onDisableMangaUpdateChanged(disable: Boolean) {
        _state.value = _state.value.copy(disableMangaUpdate = disable, isSaved = false)
    }

    fun onJellyfinUrlChanged(url: String) {
        _state.value = _state.value.copy(jellyfinUrl = url, isSaved = false)
    }

    fun onJellyfinApiKeyChanged(apiKey: String) {
        _state.value = _state.value.copy(jellyfinApiKey = apiKey, isSaved = false)
    }

    fun onPlexUrlChanged(url: String) {
        _state.value = _state.value.copy(plexUrl = url, isSaved = false)
    }

    fun onPlexTokenChanged(token: String) {
        _state.value = _state.value.copy(plexToken = token, isSaved = false)
    }

    fun onJellyfinLibraryChanged(id: String) {
        _state.value = _state.value.copy(jellyfinLibraryId = id, isSaved = false)
    }

    fun onPlexLibraryChanged(id: String) {
        _state.value = _state.value.copy(plexLibraryId = id, isSaved = false)
    }

    fun fetchJellyfinLibraries() {
        viewModelScope.launch {
            val libs = jellyfinRepository.getLibraries()
            _state.value = _state.value.copy(jellyfinLibraries = libs)
        }
    }

    fun fetchPlexLibraries() {
        viewModelScope.launch {
            val libs = plexRepository.getLibraries()
            _state.value = _state.value.copy(plexLibraries = libs)
        }
    }

    fun onTitleLanguageChanged(language: String) {
        _state.value = _state.value.copy(titleLanguage = language, isSaved = false)
    }

    fun onShowMultipleTitlesChanged(show: Boolean) {
        _state.value = _state.value.copy(showMultipleTitles = show, isSaved = false)
    }

    fun onSeerrAdvancedChanged(radarrId: Int?, sonarrId: Int?, radarrRoot: String, sonarrRoot: String) {
        _state.value = _state.value.copy(
            seerrRadarrServerId = radarrId,
            seerrSonarrServerId = sonarrId,
            seerrRadarrRootFolder = radarrRoot,
            seerrSonarrRootFolder = sonarrRoot,
            isSaved = false
        )
    }

    fun onAppLanguagesChanged(languages: Set<String>) {
        _state.value = _state.value.copy(appLanguages = languages, isSaved = false)
    }

    fun onRandomizeUiChanged(randomize: Boolean) {
        _state.value = _state.value.copy(randomizeUiLanguage = randomize, isSaved = false)
    }

    fun onEnableSeerrChanged(enable: Boolean) {
        _state.value = _state.value.copy(enableSeerr = enable, isSaved = false)
    }

    fun onShowSeerrCloudChanged(show: Boolean) {
        _state.value = _state.value.copy(showSeerrCloudInLibrary = show, isSaved = false)
    }

    fun onAutoAddDownloadedChanged(autoAdd: Boolean) {
        _state.value = _state.value.copy(autoAddDownloadedToWatchlist = autoAdd, isSaved = false)
    }

    fun onEnableMediaFallbackChanged(enable: Boolean) {
        _state.value = _state.value.copy(enableMediaServerFallback = enable, isSaved = false)
    }

    fun onShowSearchTagsChanged(show: Boolean) {
        _state.value = _state.value.copy(showSearchTags = show, isSaved = false)
    }

    fun onEnableDiscoverFeedChanged(enable: Boolean) {
        _state.value = _state.value.copy(enableDiscoverFeed = enable, isSaved = false)
    }

    fun onEnableProfileTabChanged(enable: Boolean) {
        _state.value = _state.value.copy(enableProfileTab = enable, isSaved = false)
    }

    fun onGroupSeasonsChanged(group: Boolean) {
        _state.value = _state.value.copy(groupSeasons = group, isSaved = false)
    }

    fun onShowMoreContentChanged(show: Boolean) {
        _state.value = _state.value.copy(showMoreContent = show, isSaved = false)
    }

    fun onThemeChanged(theme: AppTheme) {
        _state.value = _state.value.copy(themeMode = theme, isSaved = false)
    }

    fun onImportTheme(json: String) {
        try {
            val theme = kotlinx.serialization.json.Json.decodeFromString<com.example.anilistapp.ui.theme.CustomTheme>(json)
            _state.value = _state.value.copy(
                customTheme = theme,
                themeMode = AppTheme.CUSTOM,
                isSaved = false,
                seerrTestResult = "Theme '${theme.name}' Imported!"
            )
            viewModelScope.launch {
                repository.setCustomThemeJson(json)
                repository.setThemeMode(AppTheme.CUSTOM.name)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(seerrTestResult = "Invalid Theme File")
        }
    }

    fun onInstallComplement(url: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(seerrTestResult = "Installing Complement...")
            val success = complementRepository.installComplement(url)
            _state.value = _state.value.copy(
                seerrTestResult = if (success) "Complement Installed!" else "Installation Failed"
            )
        }
    }

    fun onImportComplement(json: String) {
        try {
            val complement = kotlinx.serialization.json.Json.decodeFromString<com.example.anilistapp.data.Complement>(json)
            // For local imports, we don't have a URL to track updates, so we use a local scheme
            val localId = "local://${complement.id}"
            _state.value = _state.value.copy(seerrTestResult = "Importing local complement...")
            viewModelScope.launch {
                // Manually save to cache and repository
                val fileName = localId.hashCode().toString() + ".json"
                val file = java.io.File(com.example.anilistapp.AnilistApplication.instance.cacheDir, "complements/$fileName")
                file.parentFile?.mkdirs()
                file.writeText(json)
                repository.installComplement(localId)
                _state.value = _state.value.copy(seerrTestResult = "Complement '${complement.name}' Imported!")
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(seerrTestResult = "Invalid Complement File")
        }
    }

    fun onWidgetThemeChanged(theme: AppTheme) {
        _state.value = _state.value.copy(widgetThemeMode = theme, isSaved = false)
    }

    fun testSeerrConnection() {
        viewModelScope.launch {
            _state.value = _state.value.copy(seerrTestResult = "Testing...")
            repository.saveSeerrSettings(_state.value.seerrUrl, _state.value.seerrApiKey)
            val success = seerrRepository.testConnection()
            if (success) {
                val sonarr = seerrRepository.getSonarrSettings()
                val radarr = seerrRepository.getRadarrSettings()
                _state.value = _state.value.copy(
                    seerrTestResult = "Connection Successful!",
                    availableSonarrServers = sonarr,
                    availableRadarrServers = radarr
                )
            } else {
                _state.value = _state.value.copy(
                    seerrTestResult = "Connection Failed."
                )
            }
        }
    }

    fun testJellyfinConnection() {
        viewModelScope.launch {
            _state.value = _state.value.copy(jellyfinTestResult = "Testing...")
            repository.saveJellyfinSettings(_state.value.jellyfinUrl, _state.value.jellyfinApiKey)
            val success = jellyfinRepository.testConnection()
            if (success) {
                fetchJellyfinLibraries()
                _state.value = _state.value.copy(jellyfinTestResult = "Connection Successful! Libraries loaded.")
            } else {
                _state.value = _state.value.copy(jellyfinTestResult = "Connection Failed.")
            }
        }
    }

    fun testPlexConnection() {
        viewModelScope.launch {
            _state.value = _state.value.copy(plexTestResult = "Testing...")
            repository.savePlexSettings(_state.value.plexUrl, _state.value.plexToken)
            val success = plexRepository.testConnection()
            if (success) {
                fetchPlexLibraries()
                _state.value = _state.value.copy(plexTestResult = "Connection Successful! Libraries loaded.")
            } else {
                _state.value = _state.value.copy(plexTestResult = "Connection Failed.")
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            repository.saveSeerrSettings(_state.value.seerrUrl, _state.value.seerrApiKey)
            repository.setSeerrAdvancedSettings(
                _state.value.seerrRadarrServerId,
                _state.value.seerrSonarrServerId,
                _state.value.seerrRadarrRootFolder,
                _state.value.seerrSonarrRootFolder
            )
            repository.setDisableAnimeUpdate(_state.value.disableAnimeUpdate)
            repository.setDisableMangaUpdate(_state.value.disableMangaUpdate)
            repository.saveJellyfinSettings(_state.value.jellyfinUrl, _state.value.jellyfinApiKey)
            repository.savePlexSettings(_state.value.plexUrl, _state.value.plexToken)
            repository.setJellyfinLibraryId(_state.value.jellyfinLibraryId)
            repository.setPlexLibraryId(_state.value.plexLibraryId)
            repository.setTitleLanguage(_state.value.titleLanguage)
            repository.setShowMultipleTitles(_state.value.showMultipleTitles)
            repository.setAppLanguages(_state.value.appLanguages)
            repository.setRandomizeUiLanguage(_state.value.randomizeUiLanguage)
            repository.setEnableSeerr(_state.value.enableSeerr)
            repository.setShowSeerrCloudInLibrary(_state.value.showSeerrCloudInLibrary)
            repository.setAutoAddDownloadedToWatchlist(_state.value.autoAddDownloadedToWatchlist)
            repository.setEnableMediaServerFallback(_state.value.enableMediaServerFallback)
            repository.setShowSearchTags(_state.value.showSearchTags)
            repository.setThemeMode(_state.value.themeMode.name)
            repository.setWidgetThemeMode(_state.value.widgetThemeMode.name)
            repository.setEnableDiscoverFeed(_state.value.enableDiscoverFeed)
            repository.setEnableProfileTab(_state.value.enableProfileTab)
            repository.setGroupSeasons(_state.value.groupSeasons)
            repository.setShowMoreContent(_state.value.showMoreContent)
            
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
