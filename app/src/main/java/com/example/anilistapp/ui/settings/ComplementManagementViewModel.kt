package com.example.anilistapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.data.Complement
import com.example.anilistapp.data.ComplementRepository
import com.example.anilistapp.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComplementInfo(
    val url: String,
    val complement: Complement
)

@HiltViewModel
class ComplementManagementViewModel @Inject constructor(
    private val complementRepository: ComplementRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val complements: StateFlow<List<ComplementInfo>> = combine(
        settingsRepository.installedComplementsUrls,
        complementRepository.installedComplements
    ) { urls, list ->
        // This mapping assumes order is preserved or hashes match
        list.mapNotNull { comp ->
            val url = urls.find { it.hashCode().toString() == comp.id.hashCode().toString() } // Simple heuristic
                ?: urls.firstOrNull { it.contains(comp.id) }
                ?: urls.toList().getOrNull(list.indexOf(comp)) ?: ""
            ComplementInfo(url, comp)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun uninstall(url: String) {
        viewModelScope.launch {
            complementRepository.uninstallComplement(url)
        }
    }
}
