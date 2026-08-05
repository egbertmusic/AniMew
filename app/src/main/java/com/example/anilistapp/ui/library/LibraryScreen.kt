package com.example.anilistapp.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anilistapp.ui.components.LocalizableText
import com.example.anilistapp.type.MediaListStatus
import com.example.anilistapp.type.MediaType
import com.example.anilistapp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMediaClick: (String, Int, String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    
    var selectedMediaForAction by remember { mutableStateOf<com.example.anilistapp.GetUserListQuery.MediaList?>(null) }
    var showActionMenu by remember { mutableStateOf(false) }
    var showRepositionMenu by remember { mutableStateOf(false) }

    val statuses = remember { MediaListStatus.entries }
    val pagerState = rememberPagerState(
        initialPage = statuses.indexOf(state.selectedStatus).coerceAtLeast(0),
        pageCount = { statuses.size }
    )

    // Sync pager with status state
    LaunchedEffect(state.selectedStatus) {
        val targetPage = statuses.indexOf(state.selectedStatus).coerceAtLeast(0)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync status state with pager swipe
    LaunchedEffect(pagerState.currentPage) {
        val targetStatus = statuses[pagerState.currentPage]
        if (state.selectedStatus != targetStatus) {
            viewModel.onStatusSelected(targetStatus)
        }
    }

    LaunchedEffect(state.requestMessage) {
        state.requestMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearRequestMessage()
        }
    }

    val backgroundBrush = remember(state.themeMode) {
        if (state.themeMode == AppTheme.LIQUID_GLASS) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A1A1A),
                    Color(0xFF2C3E50),
                    Color(0xFF000000)
                )
            )
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush ?: Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)))
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        LocalizableText(
                            text = "AniMew",
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                    },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Anime / Manga Tabs
                val selectedType = state.selectedType
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { viewModel.onTypeSelected(MediaType.ANIME) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == MediaType.ANIME) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selectedType == MediaType.ANIME) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Anime")
                        }
                        Button(
                            onClick = { viewModel.onTypeSelected(MediaType.MANGA) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == MediaType.MANGA) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selectedType == MediaType.MANGA) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Manga")
                        }
                    }
                }

                // Status Filter
                ScrollableTabRow(
                    selectedTabIndex = state.selectedStatus.ordinal,
                    containerColor = Color.Transparent,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = {}
                ) {
                    val currentStatus = state.selectedStatus
                    MediaListStatus.entries.forEach { status ->
                        FilterChip(
                            selected = currentStatus == status,
                            onClick = { viewModel.onStatusSelected(status) },
                            label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = null
                        )
                    }
                }

                if (state.error != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = state.error ?: "Unknown Error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    val mediaList = state.mediaList
                    val isLoading = state.isLoading
                    val seerrMediaStatus = state.seerrMediaStatus
                    val showSeerrCloud = state.showSeerrCloudInLibrary
                    val disableAnimeUpdate = state.disableAnimeUpdate
                    val disableMangaUpdate = state.disableMangaUpdate
                    val titleLanguage = state.titleLanguage
                    val showMultipleTitles = state.showMultipleTitles

                    PullToRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = { viewModel.refresh() },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = true,
                            beyondViewportPageCount = 1
                        ) { page ->
                            // Optimization: Only show list if it matches the current page's status
                            // to avoid flickering while swiping
                            if (statuses[page] == state.selectedStatus) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(
                                        start = 8.dp,
                                        top = 8.dp,
                                        end = 8.dp,
                                        bottom = 160.dp + contentPadding.calculateBottomPadding() // Increased padding further for floating nav bar
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(mediaList, key = { it.id }) { entry ->
                                        MediaCard(
                                            entry = entry,
                                            seerrStatus = if (showSeerrCloud) seerrMediaStatus[entry.media?.id] else null,
                                            isDisableUpdateOn = if (selectedType == MediaType.ANIME) disableAnimeUpdate else disableMangaUpdate,
                                            titleLanguage = titleLanguage,
                                            showMultipleTitles = showMultipleTitles,
                                            isManualAvailable = state.manualAvailableIds.contains(entry.media?.id),
                                            onIncrease = { id, prog -> viewModel.updateProgress(id, prog) },
                                            onDecrease = { id, prog -> viewModel.updateProgress(id, prog) },
                                            onMediaClick = onMediaClick,
                                            onLongClick = {
                                                selectedMediaForAction = entry
                                                showActionMenu = true
                                            }
                                        )
                                    }
                                }
                            } else {
                                // Show a loading or empty state for other pages to keep swipe smooth
                                Box(Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }

        // --- Action Menu (Stylized) ---
        if (showActionMenu && selectedMediaForAction != null) {
            val entry = selectedMediaForAction!!
            val media = entry.media!!
            
            ModalBottomSheet(
                onDismissRequest = { showActionMenu = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = if (state.themeMode == AppTheme.LIQUID_GLASS) Color(0xFF1E1E1E).copy(alpha = 0.95f) else MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = media.title?.userPreferred ?: "Actions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val isManualAvailable = state.manualAvailableIds.contains(media.id)
                    
                    Surface(
                        onClick = { 
                            viewModel.toggleManualAvailable(media.id)
                            showActionMenu = false 
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isManualAvailable) Color.Green.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    if (isManualAvailable) "Remove Manual Availability" else "Mark as Available",
                                    fontWeight = FontWeight.SemiBold
                                ) 
                            },
                            leadingContent = { 
                                Icon(
                                    imageVector = if (isManualAvailable) Icons.Default.CheckCircle else Icons.Default.CheckCircle, 
                                    contentDescription = null, 
                                    tint = if (isManualAvailable) Color.Green else Color.Gray
                                ) 
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    Surface(
                        onClick = { 
                            showActionMenu = false
                            showRepositionMenu = true 
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        ListItem(
                            headlineContent = { Text("Reposition (Move)", fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    Surface(
                        onClick = { 
                            viewModel.removeFromLibrary(media.id)
                            showActionMenu = false 
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ) {
                        ListItem(
                            headlineContent = { Text("Remove from Library", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        // --- Reposition Menu ---
        if (showRepositionMenu && selectedMediaForAction != null) {
            val media = selectedMediaForAction!!.media!!
            AlertDialog(
                onDismissRequest = { showRepositionMenu = false },
                title = { Text("Move ${media.title?.userPreferred}") },
                text = {
                    Column {
                        MediaListStatus.entries.forEach { status ->
                            ListItem(
                                headlineContent = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.clickable {
                                    viewModel.updateMediaStatus(media.id, status)
                                    showRepositionMenu = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRepositionMenu = false }) { Text("Cancel") }
                }
            )
        }
    }
}
