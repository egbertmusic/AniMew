package com.example.anilistapp.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.GetViewerQuery
import com.example.anilistapp.GetUserStatsQuery
import com.example.anilistapp.data.MediaRepository
import com.example.anilistapp.data.SettingsRepository
import com.example.anilistapp.ui.components.LocalizationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val viewer: GetViewerQuery.Viewer? = null,
    val animeStats: GetUserStatsQuery.Anime? = null,
    val mangaStats: GetUserStatsQuery.Manga? = null,
    val error: String? = null,
    val appLanguages: Set<String> = setOf("ENGLISH"),
    val randomizeUiLanguage: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    val localizationManager: LocalizationManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        fetchProfile()
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val appLangs = settingsRepository.appLanguages.first()
                val randomize = settingsRepository.randomizeUiLanguage.first()
                _state.update { it.copy(appLanguages = appLangs, randomizeUiLanguage = randomize) }

                val viewerResponse = repository.getViewer()
                val viewer = viewerResponse.data?.Viewer
                
                if (viewer != null) {
                    val statsResponse = repository.getUserStats(viewer.id)
                    val anime = statsResponse.data?.User?.statistics?.anime
                    val manga = statsResponse.data?.User?.statistics?.manga
                    
                    _state.update { it.copy(
                        isLoading = false,
                        viewer = viewer,
                        animeStats = anime,
                        mangaStats = manga
                    ) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to fetch viewer") }
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Failed to fetch profile", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun refresh() {
        fetchProfile()
    }
}
