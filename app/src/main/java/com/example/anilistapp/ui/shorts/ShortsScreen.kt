package com.example.anilistapp.ui.shorts

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anilistapp.ui.components.LocalizableText
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.anilistapp.ui.shorts.components.ShortsYouTubePlayer
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import com.example.anilistapp.R

@Composable
fun ShortsScreen(
    onMediaClick: (String, Int, String) -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: ShortsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    var isControlsVisible by remember { mutableStateOf(true) }
    var synopsisExpanded by remember { mutableStateOf(false) }
    var showRequestDialog by remember { mutableStateOf(false) }
    var currentMediaForRequest by remember { mutableStateOf<com.example.anilistapp.GetShortsMediaQuery.Medium?>(null) }

    // Auto-hide controls for TOP and FLOATING style
    LaunchedEffect(isControlsVisible, state.shortsNavigationStyle) {
        if (isControlsVisible && state.shortsNavigationStyle == "FLOATING") {
            delay(5000)
            isControlsVisible = false
        }
    }
    
    if (state.isLoading && state.mediaList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.error != null && state.mediaList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { state.mediaList.size })
        val flingBehavior = androidx.compose.foundation.pager.PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = androidx.compose.foundation.pager.PagerSnapDistance.atMost(1)
        )
        
        // Pagination Trigger
        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage > 0) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.soundManager.playSwish()
            }
            if (pagerState.currentPage >= state.mediaList.size - 7) {
                viewModel.fetchShorts(isPagination = true)
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().clickable { 
                    isControlsVisible = !isControlsVisible 
                },
                beyondViewportPageCount = 2,
                flingBehavior = flingBehavior,
                pageSpacing = 8.dp
            ) { page ->
                val media = state.mediaList[page]
                val isCurrentPage = pagerState.currentPage == page
                var currentVideoTime by remember { mutableFloatStateOf(0f) }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // BLURRED BACKDROP
                    val localized = state.localizedDetails[media.id]
                    val posterModel = localized?.posterPath ?: media.coverImage?.extraLarge ?: media.coverImage?.large
                    
                    AsyncImage(
                        model = posterModel,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(40.dp)
                            .graphicsLayer { alpha = 0.5f },
                        contentScale = ContentScale.Crop
                    )
                    
                    // Video Player Layer
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val videoId = state.overriddenVideoIds[media.id] ?: media.trailer?.id
                        videoId?.let { id ->
                            ShortsYouTubePlayer(
                                videoId = id,
                                isPlaying = isCurrentPage,
                                onCurrentSecond = { time -> currentVideoTime = time },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(9f / 16f) 
                            )
                        }
                    }

                    // Unified Status Overlay (Loading/Switching/Analyzing)
                    if (isCurrentPage) {
                        val statusMessage = state.requestMessages[media.id]
                        if (statusMessage != null && (statusMessage.contains("...") || statusMessage.contains("!"))) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(top = 300.dp) // Positioned below the video center
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (statusMessage.contains("...")) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    Text(
                                        text = statusMessage,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    
                    // Overlay for info and buttons
                    AnimatedVisibility(
                        visible = isControlsVisible,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                        startY = 1200f
                                    )
                                )
                        ) {
                            // Synopsis Section
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                                    .padding(bottom = if (state.shortsNavigationStyle == "BOTTOM") 100.dp else 32.dp)
                                    .fillMaxWidth(if (synopsisExpanded) 0.85f else 0.7f)
                            ) {
                                Text(
                                    text = localized?.title ?: media.title?.userPreferred ?: "Unknown Title",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .animateContentSize()
                                        .clickable { synopsisExpanded = !synopsisExpanded }
                                        .background(
                                            if (synopsisExpanded) Color.Black.copy(alpha = 0.7f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(if (synopsisExpanded) 8.dp else 0.dp)
                                ) {
                                    val description = (localized?.overview?.takeIf { it.isNotEmpty() } ?: media.description ?: "").replace(Regex("<.*?>"), "")
                                    if (description.isNotEmpty()) {
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f),
                                            maxLines = if (synopsisExpanded) 12 else 3,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = if (synopsisExpanded) Modifier.verticalScroll(rememberScrollState()) else Modifier
                                        )
                                    } else {
                                        com.example.anilistapp.ui.components.LocalizableText(
                                            text = "No synopsis available.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.5f),
                                            languages = state.appLanguages,
                                            randomize = false,
                                            primaryLanguage = state.primaryAppLanguage,
                                            localizationManager = viewModel.localizationManager
                                        )
                                    }
                                }
                            }
                            
                            // Side Buttons
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .padding(bottom = if (state.shortsNavigationStyle == "BOTTOM") 100.dp else 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Request Button
                                if (state.isSeerrEnabled) {
                                    ShortsActionButton(
                                        icon = Icons.Default.CloudDownload,
                                        label = "Request",
                                        onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.soundManager.playClick()
                                            currentMediaForRequest = media
                                            viewModel.searchOnSeerr(media.title?.userPreferred ?: "")
                                            showRequestDialog = true
                                        },
                                        state = state,
                                        localizationManager = viewModel.localizationManager
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                // Watchlist Button
                                ShortsActionButton(
                                    icon = Icons.Default.PlaylistAdd,
                                    label = "Watchlist",
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.soundManager.playClick()
                                        viewModel.addToWatchlist(media.id) 
                                    },
                                    state = state,
                                    localizationManager = viewModel.localizationManager
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // Details Button
                                ShortsActionButton(
                                    icon = Icons.Default.Info,
                                    label = "Details",
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.soundManager.playClick()
                                        onMediaClick(media.title?.userPreferred ?: "", media.id, media.type?.toString() ?: "ANIME") 
                                    },
                                    state = state,
                                    localizationManager = viewModel.localizationManager
                                )
                            }
                        }
                    }
                }
            }

            // Top Navigation Bar
            if (state.shortsNavigationStyle in listOf("TOP", "FLOATING")) {
                AnimatedVisibility(
                    visible = isControlsVisible,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
            TopNavigationBar(
                onNavigate = { route ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.soundManager.playClick()
                    onNavigate(route)
                },
                enableMewingChad = state.enableMewingChad,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            )
                }
            }

            if (showRequestDialog && currentMediaForRequest != null) {
                SeerrRequestDialog(
                    title = currentMediaForRequest?.title?.userPreferred ?: "",
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { 
                    viewModel.soundManager.playClick()
                    showRequestDialog = false 
                },
                onRequest = { tmdbId, type, seasons, profileId, serverId, rootFolder ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.soundManager.playSuccess()
                    currentMediaForRequest?.let { media ->
                            viewModel.requestOnSeerr(media.id, tmdbId, type, seasons, profileId, serverId, rootFolder)
                        }
                        showRequestDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ShortsActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    state: ShortsState,
    localizationManager: com.example.anilistapp.ui.components.LocalizationManager
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White)
        }
        com.example.anilistapp.ui.components.LocalizableText(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = Color.White.copy(alpha = 0.7f),
            languages = state.appLanguages,
            randomize = false, 
            primaryLanguage = state.primaryAppLanguage,
            localizationManager = localizationManager
        )
    }
}

@Composable
fun TopNavigationBar(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit,
    enableMewingChad: Boolean = false
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (enableMewingChad) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.icon2),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clickable { onNavigate("profile") },
                    contentScale = ContentScale.Fit
                )
            }
            NavIcon(Icons.Default.CollectionsBookmark, "Library") { onNavigate("library") }
            NavIcon(Icons.Default.Explore, "Discover") { onNavigate("discover") }
            NavIcon(Icons.Default.AccountCircle, "Profile") { onNavigate("profile") }
        }
    }
}

@Composable
fun NavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = label, tint = Color.White)
    }
}

@Composable
fun SeerrRequestDialog(
    title: String,
    state: ShortsState,
    viewModel: ShortsViewModel,
    onDismiss: () -> Unit,
    onRequest: (Int, String, List<Int>, Int, Int?, String?) -> Unit
) {
    var selectedResult by remember { mutableStateOf<com.example.anilistapp.data.SeerrSearchResult?>(null) }
    var selectedProfile by remember { mutableStateOf<Int?>(null) }
    var selectedServer by remember { mutableStateOf<Int?>(null) }
    var selectedSeasons by remember { mutableStateOf<List<Int>>(emptyList()) }

    LaunchedEffect(selectedResult) {
        selectedResult?.let {
            viewModel.loadSeerrOptions(it.type)
            if (it.type == "tv") {
                viewModel.loadSeerrDetails(it.id, it.type)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            LocalizableText(
                text = "Request $title",
                languages = state.appLanguages,
                randomize = false,
                primaryLanguage = state.primaryAppLanguage,
                localizationManager = viewModel.localizationManager
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (state.isSearchingSeerr) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (selectedResult == null) {
                    LocalizableText(
                        text = "Select matching item:",
                        languages = state.appLanguages,
                        randomize = false,
                        primaryLanguage = state.primaryAppLanguage,
                        localizationManager = viewModel.localizationManager
                    )
                    state.seerrSearchResults.forEach { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedResult = result }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = false, onClick = { selectedResult = result })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${result.title} (${result.type})")
                        }
                    }
                } else {
                    Text("Item: ${selectedResult?.title}")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Quality Profile:")
                    state.seerrProfiles.forEach { profile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProfile = profile.id }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedProfile == profile.id, onClick = { selectedProfile = profile.id })
                            Text(profile.name)
                        }
                    }

                    if (selectedResult?.type == "tv" && state.seerrDetails != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Seasons:")
                        state.seerrDetails.first.forEach { season ->
                            val isAvailable = state.seerrDetails.second.contains(season.seasonNumber)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(!isAvailable) {
                                        selectedSeasons = if (selectedSeasons.contains(season.seasonNumber)) {
                                            selectedSeasons - season.seasonNumber
                                        } else {
                                            selectedSeasons + season.seasonNumber
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isAvailable || selectedSeasons.contains(season.seasonNumber),
                                    onCheckedChange = null,
                                    enabled = !isAvailable
                                )
                                Text("Season ${season.seasonNumber} ${if (isAvailable) "(Available)" else ""}")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedResult?.let { result ->
                        onRequest(
                            result.id,
                            result.type,
                            selectedSeasons,
                            selectedProfile ?: 1,
                            selectedServer,
                            null
                        )
                    }
                },
                enabled = selectedResult != null && (selectedResult?.type != "tv" || selectedSeasons.isNotEmpty())
            ) {
                Text("Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
