package com.example.anilistapp.ui.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.GetAiringScheduleQuery
import com.example.anilistapp.GetSeasonalMediaQuery
import com.example.anilistapp.GetTrendingMediaQuery
import com.example.anilistapp.data.MediaRepository
import com.example.anilistapp.data.SettingsRepository
import com.example.anilistapp.type.MediaSeason
import com.example.anilistapp.ui.components.LocalizationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DiscoverState(
    val isLoading: Boolean = false,
    val trending: List<GetTrendingMediaQuery.Medium> = emptyList(),
    val seasonal: List<GetSeasonalMediaQuery.Medium> = emptyList(),
    val airingToday: List<GetAiringScheduleQuery.AiringSchedule> = emptyList(),
    val error: String? = null,
    val appLanguages: Set<String> = setOf("ENGLISH"),
    val randomizeUiLanguage: Boolean = false
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    val localizationManager: LocalizationManager
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverState())
    val state: StateFlow<DiscoverState> = _state.asStateFlow()

    init {
        fetchDiscoverData()
    }

    private fun fetchDiscoverData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val appLangs = settingsRepository.appLanguages.first()
                val randomize = settingsRepository.randomizeUiLanguage.first()
                _state.update { it.copy(appLanguages = appLangs, randomizeUiLanguage = randomize) }

                // Trending
                val trendingResponse = repository.getTrendingMedia()
                val trendingList = trendingResponse.data?.Page?.media?.filterNotNull() ?: emptyList()

                // Seasonal
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val season = when (month) {
                    Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> MediaSeason.WINTER
                    Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> MediaSeason.SPRING
                    Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> MediaSeason.SUMMER
                    else -> MediaSeason.FALL
                }
                val seasonalResponse = repository.getSeasonalMedia(season, year)
                val seasonalList = seasonalResponse.data?.Page?.media?.filterNotNull() ?: emptyList()

                // Airing Today
                val now = System.currentTimeMillis() / 1000
                val endOfDay = now + 86400
                val airingResponse = repository.getAiringSchedule(now.toInt(), endOfDay.toInt())
                val airingList = airingResponse.data?.Page?.airingSchedules?.filterNotNull() ?: emptyList()

                _state.update { it.copy(
                    isLoading = false,
                    trending = trendingList,
                    seasonal = seasonalList,
                    airingToday = airingList
                ) }
            } catch (e: Exception) {
                Log.e("DiscoverVM", "Failed to fetch discover data", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun refresh() {
        fetchDiscoverData()
    }
}
