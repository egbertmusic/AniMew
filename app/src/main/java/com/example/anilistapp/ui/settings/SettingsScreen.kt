package com.example.anilistapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anilistapp.ui.components.LocalizationManager
import com.example.anilistapp.ui.components.LocalizableText
import com.example.anilistapp.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onManageWidgetsClick: () -> Unit,
    onManageComplementsClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val themeImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.use { r -> r.readText() }
                viewModel.onImportTheme(json)
            } catch (e: Exception) {
                // Error handling in VM
            }
        }
    }

    val complementImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.use { r -> r.readText() }
                viewModel.onImportComplement(json)
            } catch (e: Exception) {
                // Error handling in VM
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    LocalizableText(
                        text = "Settings",
                        languages = state.appLanguages,
                        randomize = state.randomizeUiLanguage,
                        localizationManager = viewModel.localizationManager
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveSettings() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- APPEARANCE SECTION ---
            SettingsSection(title = "Appearance", localizationManager = viewModel.localizationManager, state = state) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("App Theme", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            if (theme != AppTheme.CUSTOM || state.customTheme != null) {
                                ThemePreviewItem(
                                    theme = theme,
                                    isSelected = state.themeMode == theme,
                                    onClick = { viewModel.onThemeChanged(theme) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Search Tags", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Display type, format, and genres in search results.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.showSearchTags,
                            onCheckedChange = { viewModel.onShowSearchTagsChanged(it) }
                        )
                    }
                }
            }

            // --- GENERAL SECTION ---
            SettingsSection(title = "General", localizationManager = viewModel.localizationManager, state = state) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Preferred Title Language", style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ROMAJI", "ENGLISH", "NATIVE").forEach { lang ->
                                FilterChip(
                                    selected = state.titleLanguage == lang,
                                    onClick = { viewModel.onTitleLanguageChanged(lang) },
                                    label = { Text(lang) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Multiple Titles", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Display all available languages at once.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.showMultipleTitles,
                            onCheckedChange = { viewModel.onShowMultipleTitlesChanged(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Discover Feed", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Show the Discover tab in the bottom navigation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.enableDiscoverFeed,
                            onCheckedChange = { viewModel.onEnableDiscoverFeedChanged(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Profile Tab", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Show the Profile tab in the bottom navigation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.enableProfileTab,
                            onCheckedChange = { viewModel.onEnableProfileTabChanged(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Group Anime Seasons", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Group separate AniList season entries in search results.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.groupSeasons,
                            onCheckedChange = { viewModel.onGroupSeasonsChanged(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show 'More Content'", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Show related anime, movies, and manga in the detail screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.showMoreContent,
                            onCheckedChange = { viewModel.onShowMoreContentChanged(it) }
                        )
                    }
                }
            }

            // --- LOCALIZATION SECTION ---
            SettingsSection(title = "Localization", localizationManager = viewModel.localizationManager, state = state) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Selected App Languages", style = MaterialTheme.typography.bodyLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ENGLISH", "SPANISH", "FRENCH", "JAPANESE").forEach { lang ->
                                val selected = state.appLanguages.contains(lang)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val newSet = state.appLanguages.toMutableSet()
                                        if (selected) {
                                            if (newSet.size > 1) newSet.remove(lang)
                                        } else {
                                            newSet.add(lang)
                                        }
                                        viewModel.onAppLanguagesChanged(newSet)
                                    },
                                    label = { Text(lang) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Randomize UI Language", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Each UI item will pick a random language from your selection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.randomizeUiLanguage,
                            onCheckedChange = { viewModel.onRandomizeUiChanged(it) }
                        )
                    }
                }
            }

            // --- HOME SCREEN WIDGETS SECTION ---
            SettingsSection(title = "Home Screen Widgets", localizationManager = viewModel.localizationManager, state = state) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Manage your home screen widgets and their individual themes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text("Global Widget Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            if (theme != AppTheme.CUSTOM || state.customTheme != null) {
                                ThemePreviewItem(
                                    theme = theme,
                                    isSelected = state.widgetThemeMode == theme,
                                    onClick = { viewModel.onWidgetThemeChanged(theme) }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onManageWidgetsClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Manage Active Widgets")
                    }
                }
            }

            // --- ANILIST SYNC SECTION ---
            SettingsSection(title = "AniList Sync", localizationManager = viewModel.localizationManager, state = state) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LocalizableText(
                                text = "Anime",
                                style = MaterialTheme.typography.bodyLarge,
                                languages = state.appLanguages,
                                randomize = state.randomizeUiLanguage,
                                localizationManager = viewModel.localizationManager
                            )
                            Text(
                                "Disable updates for Anime.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.disableAnimeUpdate,
                            onCheckedChange = { viewModel.onDisableAnimeUpdateChanged(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LocalizableText(
                                text = "Manga",
                                style = MaterialTheme.typography.bodyLarge,
                                languages = state.appLanguages,
                                randomize = state.randomizeUiLanguage,
                                localizationManager = viewModel.localizationManager
                            )
                            Text(
                                "Disable updates for Manga.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.disableMangaUpdate,
                            onCheckedChange = { viewModel.onDisableMangaUpdateChanged(it) }
                        )
                    }
                }
            }

            // --- SEERR SECTION ---
            SettingsSection(title = "Seerr (Beta)", localizationManager = viewModel.localizationManager, state = state) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Seerr Integration", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Connect to Overseerr/Jellyseerr.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.enableSeerr,
                            onCheckedChange = { viewModel.onEnableSeerrChanged(it) }
                        )
                    }

                    if (state.enableSeerr) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Show Cloud Icon in Library", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Display the Seerr request icon on anime cards.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.showSeerrCloudInLibrary,
                                onCheckedChange = { viewModel.onShowSeerrCloudChanged(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Sync Downloaded Content", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Automatically add downloaded anime to your watchlist.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.autoAddDownloadedToWatchlist,
                                onCheckedChange = { viewModel.onAutoAddDownloadedChanged(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Media Server Fallback", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Directly search Jellyfin/Plex if Seerr sync fails.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.enableMediaServerFallback,
                                onCheckedChange = { viewModel.onEnableMediaFallbackChanged(it) }
                            )
                        }

                        OutlinedTextField(
                            value = state.seerrUrl,
                            onValueChange = { viewModel.onUrlChanged(it) },
                            label = { Text("Seerr URL") },
                            placeholder = { Text("https://seerr.example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.seerrApiKey,
                            onValueChange = { viewModel.onApiKeyChanged(it) },
                            label = { Text("Seerr API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text("Jellyfin / Plex (Optional Fallback)", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = state.jellyfinUrl,
                            onValueChange = { viewModel.onJellyfinUrlChanged(it) },
                            label = { Text("Jellyfin URL") },
                            placeholder = { Text("http://192.168.1.x:8096") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.jellyfinApiKey,
                            onValueChange = { viewModel.onJellyfinApiKeyChanged(it) },
                            label = { Text("Jellyfin API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        if (state.jellyfinLibraries.isNotEmpty()) {
                            Text("Jellyfin Library to Sync", style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.jellyfinLibraries.forEach { (name, id) ->
                                    FilterChip(
                                        selected = state.jellyfinLibraryId == id,
                                        onClick = { viewModel.onJellyfinLibraryChanged(id) },
                                        label = { Text(name) }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.testJellyfinConnection() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Test Jellyfin Connection")
                        }
                        state.jellyfinTestResult?.let { result ->
                            Text(
                                text = result,
                                color = if (result.contains("Successful")) Color.Green else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        OutlinedTextField(
                            value = state.plexUrl,
                            onValueChange = { viewModel.onPlexUrlChanged(it) },
                            label = { Text("Plex URL") },
                            placeholder = { Text("http://192.168.1.x:32400") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.plexToken,
                            onValueChange = { viewModel.onPlexTokenChanged(it) },
                            label = { Text("Plex Token") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        if (state.plexLibraries.isNotEmpty()) {
                            Text("Plex Library to Sync", style = MaterialTheme.typography.labelSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.plexLibraries.forEach { (name, id) ->
                                    FilterChip(
                                        selected = state.plexLibraryId == id,
                                        onClick = { viewModel.onPlexLibraryChanged(id) },
                                        label = { Text(name) }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.testPlexConnection() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Test Plex Connection")
                        }
                        state.plexTestResult?.let { result ->
                            Text(
                                text = result,
                                color = if (result.contains("Successful")) Color.Green else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        Button(
                            onClick = { viewModel.testSeerrConnection() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Test Seerr Connection")
                        }

                        state.seerrTestResult?.let { result ->
                            Text(
                                text = result,
                                color = if (result.contains("Successful")) Color.Green else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        Text("Advanced Server Selection", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.seerrRadarrServerId?.toString() ?: "",
                                onValueChange = { viewModel.onSeerrAdvancedChanged(it.toIntOrNull(), state.seerrSonarrServerId, state.seerrRadarrRootFolder, state.seerrSonarrRootFolder) },
                                label = { Text("Radarr ID") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.seerrSonarrServerId?.toString() ?: "",
                                onValueChange = { viewModel.onSeerrAdvancedChanged(state.seerrRadarrServerId, it.toIntOrNull(), state.seerrRadarrRootFolder, state.seerrSonarrRootFolder) },
                                label = { Text("Sonarr ID") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = state.seerrRadarrRootFolder,
                            onValueChange = { viewModel.onSeerrAdvancedChanged(state.seerrRadarrServerId, state.seerrSonarrServerId, it, state.seerrSonarrRootFolder) },
                            label = { Text("Radarr Root Folder") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.seerrSonarrRootFolder,
                            onValueChange = { viewModel.onSeerrAdvancedChanged(state.seerrRadarrServerId, state.seerrSonarrServerId, state.seerrRadarrRootFolder, it) },
                            label = { Text("Sonarr Root Folder") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // --- ADVANCED SECTION ---
            SettingsSection(title = "Advanced & Complements", localizationManager = viewModel.localizationManager, state = state) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Import custom themes or enter a URL to install community addons.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    var addonUrl by remember { mutableStateOf("") }
                    
                    OutlinedTextField(
                        value = addonUrl,
                        onValueChange = { addonUrl = it },
                        label = { Text("Addon Manifest URL") },
                        placeholder = { Text("https://example.com/addon.json") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { 
                                if (addonUrl.isNotEmpty()) {
                                    viewModel.onInstallComplement(addonUrl)
                                    addonUrl = ""
                                }
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "Install")
                            }
                        }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { themeImportLauncher.launch("application/json") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Import Theme")
                        }
                        
                        OutlinedButton(
                            onClick = onManageComplementsClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Manage Addons")
                        }
                    }
                }
            }

            if (state.isSaved) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = "Settings saved successfully!",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                LocalizableText(
                    text = "Save", // Using a simpler key
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage,
                    localizationManager = viewModel.localizationManager
                )
            }
        }
    }
}

@Composable
fun ThemePreviewItem(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK -> Color(0xFF151F2E)
        AppTheme.AMOLED -> Color.Black
        AppTheme.SAKURA -> Color(0xFFFFB7C5)
        AppTheme.FOREST -> Color(0xFF2D5A27)
        AppTheme.DRACULA -> Color(0xFFBD93F9)
        AppTheme.LIQUID_GLASS -> Color.White.copy(alpha = 0.5f)
        AppTheme.CYBERPUNK -> CyberpunkPink
        AppTheme.GENSHIN -> GenshinGold
        AppTheme.CUSTOM -> Color.Gray
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(2.dp, borderColor, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    localizationManager: LocalizationManager,
    state: SettingsState,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LocalizableText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            languages = state.appLanguages,
            randomize = state.randomizeUiLanguage,
            localizationManager = localizationManager
        )
        content()
        Divider(modifier = Modifier.padding(top = 12.dp))
    }
}
