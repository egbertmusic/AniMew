package com.example.anilistapp.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anilistapp.ui.components.LocalizableText
import com.example.anilistapp.ui.theme.CardDark
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    title: String,
    mediaId: Int? = null,
    mediaType: String? = null,
    onBackClick: () -> Unit,
    viewModel: MediaDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(title, mediaId, mediaType) {
        viewModel.loadDetails(title, mediaId, mediaType)
    }

    LaunchedEffect(state.requestMessage) {
        state.requestMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                // Poster and Basic Info
                Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = state.kitsuDetails?.posterUrl,
                        contentDescription = state.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (state.mediaId != null && !state.isInWatchlist) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.addToWatchlist() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            LocalizableText(
                                text = "Add to Watchlist",
                                languages = state.appLanguages,
                                randomize = state.randomizeUiLanguage,
                                localizationManager = viewModel.localizationManager
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LocalizableText(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        languages = state.appLanguages,
                        randomize = state.randomizeUiLanguage,
                        localizationManager = viewModel.localizationManager
                    )
                    Text(
                        text = state.kitsuDetails?.synopsis ?: "No synopsis available.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (state.streamLinks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Community Watch Links",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.streamLinks.forEach { (name, url) ->
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(name)
                                }
                            }
                        }
                    }

                    if (state.extraMetadata.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "External Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        state.extraMetadata.forEach { (name, value) ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(value, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    if (!state.youtubeVideoId.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        LocalizableText(
                            text = "Trailer",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            languages = state.appLanguages,
                            randomize = state.randomizeUiLanguage,
                            localizationManager = viewModel.localizationManager
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val videoId = state.youtubeVideoId
                        
                        // High-compatibility Stealth player to avoid the 152 error flash
                        // and ensure the consent wall is automatically bypassed.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                        ) {
                            // High-precision stealth player
                            TrailerWebViewFallback(videoId ?: "")
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow, 
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Watch on YouTube", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (state.isSeerrEnabled) {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = com.example.anilistapp.ui.theme.SeerrPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = com.example.anilistapp.ui.theme.SeerrPurple,
                                            modifier = Modifier.padding(8.dp).size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    LocalizableText(
                                        text = "Request on Seerr",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        languages = state.appLanguages,
                                        randomize = state.randomizeUiLanguage,
                                        localizationManager = viewModel.localizationManager
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val seerrStatus = state.seerrMatch?.status
                                val isFullyAvailable = seerrStatus == 5 || seerrStatus == 6
                                val isProcessing = seerrStatus == 2 || seerrStatus == 3
                                val isPartial = seerrStatus == 4

                                if (isFullyAvailable) {
                                    Surface(
                                        color = Color.Green.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (seerrStatus == 6) Icons.AutoMirrored.Filled.Dvr else Icons.Default.Check, 
                                                contentDescription = null, 
                                                tint = Color.Green
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    if (seerrStatus == 6) "Available on your Media Server!" else "Already available on your server!", 
                                                    color = MaterialTheme.colorScheme.onSurface, 
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text("Sync may be experimental for some items", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                } else if (isProcessing) {
                                    Surface(
                                        color = com.example.anilistapp.ui.theme.SeerrOrange.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = com.example.anilistapp.ui.theme.SeerrOrange)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                if (seerrStatus == 2) "Request is Pending..." else "Content is Downloading...",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                } else {
                                    if (isPartial) {
                                        Surface(
                                            color = Color.Yellow.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Yellow)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("Partially available. Request missing episodes below.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }

                                    if (state.seerrProfiles.isEmpty()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp,
                                                color = com.example.anilistapp.ui.theme.SeerrPurple
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Connecting to Seerr...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    } else {
                                        Text(
                                            "Select Quality Profile",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.LightGray
                                        )
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            state.seerrProfiles.forEach { profile ->
                                                val isSelected = state.selectedProfileId == profile.id
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { viewModel.onProfileSelected(profile.id) },
                                                    label = { Text(profile.name) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = com.example.anilistapp.ui.theme.SeerrPurple,
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        // Season Selector (Always show if TV)
                                        if (state.seerrMatch?.type != "movie") {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                "Select Seasons",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.LightGray
                                            )
                                            val displaySeasons = if (state.seerrSeasons.isEmpty()) listOf(1) else state.seerrSeasons
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                displaySeasons.forEach { seasonNum ->
                                                    val isSelected = state.selectedSeasons.contains(seasonNum)
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = { viewModel.onSeasonToggle(seasonNum) },
                                                        label = { Text("Season $seasonNum") },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = com.example.anilistapp.ui.theme.SeerrPurple,
                                                            selectedLabelColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Button(
                                            enabled = state.selectedProfileId != null && (state.seerrMatch?.type == "movie" || state.selectedSeasons.isNotEmpty()),
                                            onClick = { state.selectedProfileId?.let { viewModel.requestOnSeerr(it) } },
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = com.example.anilistapp.ui.theme.SeerrPurple
                                            )
                                        ) {
                                            Text("Request Content")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrailerWebViewFallback(videoId: String) {
    if (videoId.isEmpty()) return
    
    val mobileUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"

    AndroidView(
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                setBackgroundColor(android.graphics.Color.BLACK)
                
                webChromeClient = android.webkit.WebChromeClient()
                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                        val script = """
                            (function() {
                                function hideElements() {
                                    var selectors = [
                                        'header', 'footer', '.ytm-app-header-renderer', '.ytm-mobile-topbar-renderer',
                                        '.ytm-watch-metadata-renderer', '.ytm-item-section-renderer', 
                                        '.ytm-pivot-bar-renderer', '.ytm-open-app-button',
                                        '.ytp-chrome-top', '.ytp-show-cards-title', '.ytp-gradient-top',
                                        '.ytm-smart-app-banner', '#header', '#masthead-container',
                                        '.ytm-video-overlay-renderer', '.ytm-interstitial-renderer'
                                    ];
                                    selectors.forEach(function(s) {
                                        document.querySelectorAll(s).forEach(function(el) {
                                            el.style.display = 'none';
                                            el.style.visibility = 'hidden';
                                            el.style.height = '0';
                                        });
                                    });
                                    
                                    // Force video to fill
                                    var video = document.querySelector('video');
                                    if (video) {
                                        video.style.position = 'fixed';
                                        video.style.top = '0';
                                        video.style.left = '0';
                                        video.style.width = '100vw';
                                        video.style.height = '100vh';
                                        video.style.zIndex = '999999';
                                        video.style.objectFit = 'cover';
                                        if (video.paused) video.play();
                                    }
                                }

                                hideElements();
                                
                                // Aggressive observer to catch dynamic content
                                var observer = new MutationObserver(hideElements);
                                observer.observe(document.body, { childList: true, subtree: true });
                                
                                // Fallback interval
                                setInterval(hideElements, 1000);
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(script, null)
                    }
                }
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    userAgentString = mobileUA
                }
                
                loadUrl("https://m.youtube.com/watch?v=$videoId")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
