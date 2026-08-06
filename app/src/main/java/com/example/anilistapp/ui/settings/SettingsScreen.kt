package com.example.anilistapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anilistapp.ui.components.LocalizationManager
import com.example.anilistapp.ui.components.LocalizableText
import com.example.anilistapp.ui.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                        primaryLanguage = state.primaryAppLanguage,
                        localizationManager = viewModel.localizationManager
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = viewModel.localizationManager.translate("Back", state.primaryAppLanguage)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveSettings() }) {
                        Icon(
                            Icons.Default.Save, 
                            contentDescription = viewModel.localizationManager.translate("Save", state.primaryAppLanguage)
                        )
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
            SettingsCard {
                SettingsHeader(
                    title = "Appearance",
                    icon = Icons.Default.Palette,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )
                
                SettingsItem(
                    title = "App Theme",
                    icon = Icons.Default.Brush,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                ) {
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
                }

                SettingsSwitchItem(
                    title = "Show Search Tags",
                    subtitle = "Display type, format, and genres in search results.",
                    icon = Icons.Default.Label,
                    checked = state.showSearchTags,
                    onCheckedChange = { viewModel.onShowSearchTagsChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Show App Title",
                    subtitle = "Display the app branding in the top bar.",
                    icon = Icons.Default.Title,
                    checked = state.showAppTitle,
                    onCheckedChange = { viewModel.onShowAppTitleChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Enable Mewing Chad",
                    subtitle = "🤫🧏‍♂️ Bye bye...",
                    icon = Icons.Default.Face,
                    checked = state.enableMewingChad,
                    onCheckedChange = { viewModel.onEnableMewingChadChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingsHeader(
                    title = "Sounds & Music",
                    icon = Icons.Default.VolumeUp,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Enable SFX",
                    subtitle = "Play UI sounds for clicks and actions.",
                    icon = Icons.Default.Audiotrack,
                    checked = state.enableSfx,
                    onCheckedChange = { viewModel.onEnableSfxChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Enable Background Music",
                    subtitle = "Play ambient music while using the app.",
                    icon = Icons.Default.MusicNote,
                    checked = state.enableBgm,
                    onCheckedChange = { viewModel.onEnableBgmChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )
            }

            // --- GENERAL SECTION ---
            SettingsCard {
                SettingsHeader(
                    title = "General",
                    icon = Icons.Default.Settings,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsItem(
                    title = "Preferred Title Language",
                    icon = Icons.Default.Language,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ROMAJI", "ENGLISH", "NATIVE").forEach { lang ->
                            FilterChip(
                                selected = state.titleLanguage == lang,
                                onClick = { viewModel.onTitleLanguageChanged(lang) },
                                label = { Text(lang) }
                            )
                        }
                    }
                }

                SettingsItem(
                    title = "Preferred Trailer Language",
                    subtitle = "Used when searching for trailers on YouTube.",
                    icon = Icons.Default.OndemandVideo,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("JAPANESE", "ENGLISH", "SPANISH", "FRENCH").forEach { lang ->
                            FilterChip(
                                selected = state.preferredTrailerLanguage == lang,
                                onClick = { viewModel.onPreferredTrailerLanguageChanged(lang) },
                                label = { Text(lang) }
                            )
                        }
                    }
                }

                SettingsSwitchItem(
                    title = "Show Multiple Titles",
                    subtitle = "Display all available languages at once.",
                    icon = Icons.Default.Subtitles,
                    checked = state.showMultipleTitles,
                    onCheckedChange = { viewModel.onShowMultipleTitlesChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Enable Discover Feed",
                    subtitle = "Show the Discover tab in the bottom navigation.",
                    icon = Icons.Default.Explore,
                    checked = state.enableDiscoverFeed,
                    onCheckedChange = { viewModel.onEnableDiscoverFeedChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Enable Shorts Feed",
                    subtitle = "Show the full-screen trailer feed tab.",
                    icon = Icons.Default.Movie,
                    checked = state.enableShortsFeed,
                    onCheckedChange = { viewModel.onEnableShortsFeedChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                if (state.enableShortsFeed) {
                    SettingsItem(
                        title = "Shorts Navigation Style",
                        subtitle = "Choose where the menu bar appears in Shorts.",
                        icon = Icons.Default.Navigation,
                        localizationManager = viewModel.localizationManager,
                        languages = state.appLanguages,
                        primaryLanguage = state.primaryAppLanguage,
                        randomize = state.randomizeUiLanguage
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("BOTTOM", "TOP", "FLOATING").forEach { style ->
                                FilterChip(
                                    selected = state.shortsNavigationStyle == style,
                                    onClick = { viewModel.onShortsNavigationStyleChanged(style) },
                                    label = { Text(style) }
                                )
                            }
                        }
                    }

                    SettingsItem(
                        title = "Shorts Feed Source",
                        subtitle = "Select what content to show in the shorts feed.",
                        icon = Icons.Default.Source,
                        localizationManager = viewModel.localizationManager,
                        languages = state.appLanguages,
                        primaryLanguage = state.primaryAppLanguage,
                        randomize = state.randomizeUiLanguage
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("TRENDING", "FOR YOU", "RECOMMENDED", "GEMINI", "PLANNING", "RANDOM").forEach { source ->
                                FilterChip(
                                    selected = state.shortsFeedSource == source,
                                    onClick = { viewModel.onShortsFeedSourceChanged(source) },
                                    label = { Text(source) }
                                )
                            }
                        }
                    }

                    SettingsItem(
                        title = "Shorts Content Type",
                        subtitle = "Choose between Anime, Manga, or both.",
                        icon = Icons.Default.Category,
                        localizationManager = viewModel.localizationManager,
                        languages = state.appLanguages,
                        primaryLanguage = state.primaryAppLanguage,
                        randomize = state.randomizeUiLanguage
                    ) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ANIME", "MANGA", "BOTH").forEach { type ->
                                FilterChip(
                                    selected = state.shortsFeedType == type,
                                    onClick = { viewModel.onShortsFeedTypeChanged(type) },
                                    label = { Text(type) }
                                )
                            }
                        }
                    }
                }

                SettingsSwitchItem(
                    title = "Enable Profile Tab",
                    subtitle = "Show the Profile tab in the bottom navigation.",
                    icon = Icons.Default.AccountCircle,
                    checked = state.enableProfileTab,
                    onCheckedChange = { viewModel.onEnableProfileTabChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Group Anime Seasons",
                    subtitle = "Group separate AniList season entries in search results.",
                    icon = Icons.Default.FolderOpen,
                    checked = state.groupSeasons,
                    onCheckedChange = { viewModel.onGroupSeasonsChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Show 'More Content'",
                    subtitle = "Show related anime, movies, and manga in the detail screen.",
                    icon = Icons.Default.MoreHoriz,
                    checked = state.showMoreContent,
                    onCheckedChange = { viewModel.onShowMoreContentChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )
            }

            // --- LOCALIZATION SECTION ---
            SettingsCard {
                SettingsHeader(
                    title = "Localization",
                    icon = Icons.Default.Translate,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsItem(
                    title = "Primary App Language",
                    subtitle = "This will be the main language for the UI and synopsis.",
                    icon = Icons.Default.Translate,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.appLanguages.forEach { lang ->
                            FilterChip(
                                selected = state.primaryAppLanguage == lang,
                                onClick = { viewModel.onPrimaryAppLanguageChanged(lang) },
                                label = { Text(lang) }
                            )
                        }
                    }
                }

                SettingsItem(
                    title = "Selected App Languages",
                    icon = Icons.Default.ListAlt,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ENGLISH", "SPANISH", "FRENCH", "JAPANESE").forEach { lang ->
                            val selected = state.appLanguages.contains(lang)
                            Surface(
                                modifier = Modifier.clickable {
                                    val newSet = state.appLanguages.toMutableSet()
                                    if (selected) {
                                        if (newSet.size > 1) newSet.remove(lang)
                                    } else {
                                        newSet.add(lang)
                                    }
                                    viewModel.onAppLanguagesChanged(newSet)
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = lang,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                SettingsSwitchItem(
                    title = "Randomize UI Language",
                    subtitle = "Each UI item will pick a random language from your selection.",
                    icon = Icons.Default.Casino,
                    checked = state.randomizeUiLanguage,
                    onCheckedChange = { viewModel.onRandomizeUiChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Localized Posters & Synopsis",
                    subtitle = "Fetch localized title cards and descriptions from TMDB.",
                    icon = Icons.Default.Public,
                    checked = state.enableLocalizedContent,
                    onCheckedChange = { viewModel.onEnableLocalizedContentChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                )
            }

            // --- HOME SCREEN WIDGETS SECTION ---
            SettingsCard {
                SettingsHeader(
                    title = "Home Screen Widgets",
                    icon = Icons.Default.Widgets,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                )

                LocalizableText(
                    text = "Manage your home screen widgets and their individual themes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage,
                    primaryLanguage = state.primaryAppLanguage,
                    localizationManager = viewModel.localizationManager
                )
                
                SettingsItem(
                    title = "Global Widget Theme",
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                ) {
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
                }

                Button(
                    onClick = onManageWidgetsClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    LocalizableText(
                        text = "Manage Active Widgets",
                        languages = state.appLanguages,
                        randomize = state.randomizeUiLanguage,
                        primaryLanguage = state.primaryAppLanguage,
                        localizationManager = viewModel.localizationManager
                    )
                }
            }

            // --- ANILIST SYNC SECTION ---
            SettingsCard {
                SettingsHeader(
                    title = "AniList Sync",
                    icon = Icons.Default.Sync,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Anime",
                    subtitle = "Disable updates for Anime.",
                    icon = Icons.Default.Movie,
                    checked = state.disableAnimeUpdate,
                    onCheckedChange = { viewModel.onDisableAnimeUpdateChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Manga",
                    subtitle = "Disable updates for Manga.",
                    icon = Icons.Default.MenuBook,
                    checked = state.disableMangaUpdate,
                    onCheckedChange = { viewModel.onDisableMangaUpdateChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage
                )
            }

            // --- SEERR SECTION ---
            SettingsCard {
                SettingsHeader(
                    title = "Seerr (Beta)",
                    icon = Icons.Default.Cloud,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                SettingsSwitchItem(
                    title = "Enable Seerr Integration",
                    subtitle = "Connect to Overseerr/Jellyseerr.",
                    checked = state.enableSeerr,
                    onCheckedChange = { viewModel.onEnableSeerrChanged(it) },
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                if (state.enableSeerr) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SettingsSwitchItem(
                        title = "Show Cloud Icon in Library",
                        subtitle = "Display the Seerr request icon on anime cards.",
                        checked = state.showSeerrCloudInLibrary,
                        onCheckedChange = { viewModel.onShowSeerrCloudChanged(it) },
                        localizationManager = viewModel.localizationManager,
                        languages = state.appLanguages,
                        primaryLanguage = state.primaryAppLanguage,
                        randomize = state.randomizeUiLanguage
                    )

                    SettingsSwitchItem(
                        title = "Auto-Sync Downloaded Content",
                        subtitle = "Automatically add downloaded anime to your watchlist.",
                        checked = state.autoAddDownloadedToWatchlist,
                        onCheckedChange = { viewModel.onAutoAddDownloadedChanged(it) },
                        localizationManager = viewModel.localizationManager,
                        languages = state.appLanguages,
                        primaryLanguage = state.primaryAppLanguage,
                        randomize = state.randomizeUiLanguage
                    )

                    SettingsSwitchItem(
                        title = "Media Server Fallback",
                        subtitle = "Directly search Jellyfin/Plex if Seerr sync fails.",
                        checked = state.enableMediaServerFallback,
                        onCheckedChange = { viewModel.onEnableMediaFallbackChanged(it) },
                        localizationManager = viewModel.localizationManager,
                        languages = state.appLanguages,
                        primaryLanguage = state.primaryAppLanguage,
                        randomize = state.randomizeUiLanguage
                    )

                    OutlinedTextField(
                        value = state.seerrUrl,
                        onValueChange = { viewModel.onUrlChanged(it) },
                        label = { Text("Seerr URL") },
                        placeholder = { Text("https://seerr.example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = state.seerrApiKey,
                        onValueChange = { viewModel.onApiKeyChanged(it) },
                        label = { Text("Seerr API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    Button(
                        onClick = { viewModel.testSeerrConnection() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LocalizableText(
                            text = "Test Seerr Connection",
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            primaryLanguage = state.primaryAppLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                    }

                    state.seerrTestResult?.let { result ->
                        Text(
                            text = result,
                            color = if (result.contains("Successful")) Color.Green else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    // Media Server section within Seerr
                    LocalizableText(
                        text = "Media Servers", 
                        style = MaterialTheme.typography.titleSmall, 
                        modifier = Modifier.padding(top = 16.dp),
                        languages = state.appLanguages,
                        randomize = state.randomizeUiLanguage,
                        primaryLanguage = state.primaryAppLanguage,
                        localizationManager = viewModel.localizationManager
                    )
                    
                    OutlinedTextField(
                        value = state.jellyfinUrl,
                        onValueChange = { viewModel.onJellyfinUrlChanged(it) },
                        label = { Text("Jellyfin URL") },
                        placeholder = { Text("http://192.168.1.x:8096") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = state.jellyfinApiKey,
                        onValueChange = { viewModel.onJellyfinApiKeyChanged(it) },
                        label = { Text("Jellyfin API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    Button(
                        onClick = { viewModel.testJellyfinConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        LocalizableText(
                            text = "Test Jellyfin Connection",
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            primaryLanguage = state.primaryAppLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                    }
                    
                    OutlinedTextField(
                        value = state.plexUrl,
                        onValueChange = { viewModel.onPlexUrlChanged(it) },
                        label = { Text("Plex URL") },
                        placeholder = { Text("http://192.168.1.x:32400") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = state.plexToken,
                        onValueChange = { viewModel.onPlexTokenChanged(it) },
                        label = { Text("Plex Token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    Button(
                        onClick = { viewModel.testPlexConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        LocalizableText(
                            text = "Test Plex Connection",
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            primaryLanguage = state.primaryAppLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                    }
                }
            }

            // --- ADVANCED SECTION ---
            SettingsCard {
                SettingsHeader(
                    title = "Advanced & Complements",
                    icon = Icons.Default.Extension,
                    localizationManager = viewModel.localizationManager,
                    languages = state.appLanguages,
                    primaryLanguage = state.primaryAppLanguage,
                    randomize = state.randomizeUiLanguage
                )

                LocalizableText(
                    text = "Import custom themes or enter a URL to install community addons.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    languages = state.appLanguages,
                    randomize = state.randomizeUiLanguage,
                    primaryLanguage = state.primaryAppLanguage,
                    localizationManager = viewModel.localizationManager
                )
                
                var addonUrl by remember { mutableStateOf("") }
                
                OutlinedTextField(
                    value = addonUrl,
                    onValueChange = { addonUrl = it },
                    label = { Text("Addon Manifest URL") },
                    placeholder = { Text("https://example.com/addon.json") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        IconButton(onClick = { 
                            if (addonUrl.isNotEmpty()) {
                                viewModel.onInstallComplement(addonUrl)
                                addonUrl = ""
                            }
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Install")
                        }
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { themeImportLauncher.launch("application/json") },
                        modifier = Modifier.weight(1f)
                    ) {
                        LocalizableText(
                            text = "Import Theme",
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onManageComplementsClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        LocalizableText(
                            text = "Manage Addons",
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                    }
                }
            }

            if (state.isSaved) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        LocalizableText(
                            text = "Settings saved successfully!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                LocalizableText(
                    text = "Save",
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
fun SettingsHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    localizationManager: LocalizationManager,
    languages: Set<String>,
    primaryLanguage: String = "ENGLISH",
    randomize: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        LocalizableText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            languages = languages,
            randomize = randomize,
            primaryLanguage = primaryLanguage,
            localizationManager = localizationManager
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    localizationManager: LocalizationManager,
    languages: Set<String>,
    primaryLanguage: String = "ENGLISH",
    randomize: Boolean,
    content: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                LocalizableText(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    languages = languages,
                    randomize = randomize,
                    primaryLanguage = primaryLanguage,
                    localizationManager = localizationManager
                )
                if (subtitle != null) {
                    LocalizableText(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        languages = languages,
                        randomize = randomize,
                        primaryLanguage = primaryLanguage,
                        localizationManager = localizationManager
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    localizationManager: LocalizationManager,
    languages: Set<String>,
    primaryLanguage: String = "ENGLISH",
    randomize: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                LocalizableText(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    languages = languages,
                    randomize = randomize,
                    primaryLanguage = primaryLanguage,
                    localizationManager = localizationManager
                )
                if (subtitle != null) {
                    LocalizableText(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        languages = languages,
                        randomize = randomize,
                        primaryLanguage = primaryLanguage,
                        localizationManager = localizationManager
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.large
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}
