package com.example.anilistapp.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anilistapp.data.SeerrSeasonInfo
import com.example.anilistapp.ui.components.LocalizableText
import com.example.anilistapp.ui.theme.CardDark
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
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

                    if (state.relatedMedia.any { it.relationType in listOf("PREQUEL", "SEQUEL", "PARENT", "SIDE_STORY") }) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Series Timeline",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.relatedMedia.filter { it.relationType in listOf("PREQUEL", "SEQUEL", "PARENT", "SIDE_STORY") || it.id == state.mediaId }
                                .distinctBy { it.id }
                                .forEach { related ->
                                    val isCurrent = related.id == state.mediaId
                                    Box(
                                        modifier = Modifier
                                            .width(70.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .border(
                                                width = if (isCurrent) 2.dp else 1.dp,
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { if (!isCurrent) viewModel.switchSeason(related.id) }
                                    ) {
                                        Column {
                                            Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                                                AsyncImage(
                                                    model = related.coverImage,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                if (isCurrent) {
                                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = related.relationType.replace("_", " "),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 6.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                color = if(isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                        }
                    }

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
                        text = state.synopsis.takeIf { it.isNotBlank() }?.replace(Regex("<[^>]*>"), "") ?: "No synopsis available.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (state.showMoreContentSection && state.relatedMedia.any { it.id != state.mediaId }) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            "More of this series",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.relatedMedia.filter { it.id != state.mediaId }.forEach { related ->
                                ElevatedCard(
                                    modifier = Modifier.width(140.dp).clickable { viewModel.switchSeason(related.id) },
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column {
                                        Box {
                                            AsyncImage(
                                                model = related.coverImage,
                                                contentDescription = related.title,
                                                modifier = Modifier.height(200.dp).fillMaxWidth(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Surface(
                                                modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                                                color = when(related.relationType) {
                                                    "PREQUEL" -> Color(0xFF4CAF50)
                                                    "SEQUEL" -> Color(0xFF2196F3)
                                                    "SIDE_STORY" -> Color(0xFFFF9800)
                                                    else -> Color.Black.copy(alpha = 0.6f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    related.relationType.replace("_", " "),
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                related.title,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                minLines = 2
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Surface(
                                                    color = if (related.type == "MANGA") Color(0xFFE91E63) else Color(0xFF2196F3),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(related.type, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp), color = Color.White)
                                                }
                                                related.format?.let {
                                                    Surface(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(4.dp)) {
                                                        Text(it, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp), color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

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

                        val rawVideoId = state.youtubeVideoId
                        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                        val cleanId = remember(rawVideoId) {
                            when {
                                rawVideoId == null -> null
                                rawVideoId.contains("v=") -> rawVideoId.substringAfter("v=").substringBefore("&")
                                rawVideoId.contains("youtu.be/") -> rawVideoId.substringAfter("youtu.be/").substringBefore("?")
                                rawVideoId.contains("shorts/") -> rawVideoId.substringAfter("shorts/").substringBefore("?")
                                else -> rawVideoId
                            }
                        }

                        var embedFailed by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f) // Ensures standard video proportions
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                        ) {
                            if (cleanId != null) {
                                if (embedFailed) {
                                    // CLEAN IFRAME WEBVIEW FALLBACK (No top/bottom YouTube web bars)
                                    AndroidView(
                                        factory = { ctx ->
                                            android.webkit.WebView(ctx).apply {
                                                layoutParams = android.view.ViewGroup.LayoutParams(
                                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                                settings.javaScriptEnabled = true
                                                settings.domStorageEnabled = true
                                                settings.mediaPlaybackRequiresUserGesture = false
                                                webChromeClient = android.webkit.WebChromeClient()
                                                webViewClient = android.webkit.WebViewClient()

                                                val embedHtml = """
                                                    <!DOCTYPE html>
                                                    <html>
                                                    <head>
                                                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                                        <style>
                                                            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: black; overflow: hidden; }
                                                            iframe { width: 100%; height: 100%; border: none; }
                                                        </style>
                                                    </head>
                                                    <body>
                                                        <iframe src="https://www.youtube.com/embed/$cleanId?autoplay=0&controls=1&modestbranding=1&rel=0&playsinline=1" 
                                                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                                                allowfullscreen>
                                                        </iframe>
                                                    </body>
                                                    </html>
                                                """.trimIndent()

                                                loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Native YouTubePlayerView
                                    AndroidView(
                                        factory = { ctx ->
                                            YouTubePlayerView(ctx).apply {
                                                enableAutomaticInitialization = false
                                                lifecycleOwner.lifecycle.addObserver(this)

                                                val options = IFramePlayerOptions.Builder()
                                                    .controls(1)
                                                    .rel(0)
                                                    .origin("https://www.youtube-nocookie.com")
                                                    .build()

                                                initialize(object : AbstractYouTubePlayerListener() {
                                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                                        youTubePlayer.cueVideo(cleanId, 0f)
                                                    }

                                                    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                                        if (error == PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER || 
                                                            error.name.contains("UNKNOWN") || 
                                                            error.name.contains("HTML5")) {
                                                            embedFailed = true
                                                        } else {
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$cleanId"))
                                                            context.startActivity(intent)
                                                        }
                                                    }
                                                }, options)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$cleanId"))
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
                                val isTv = state.seerrMatch?.type == "tv"
                                val allSeasonsAvailable = isTv && state.seerrSeasons.isNotEmpty() && 
                                                          state.seerrSeasons.all { state.availableSeasons.contains(it.seasonNumber) }
                                
                                val isFullyAvailable = (seerrStatus == 5 || seerrStatus == 6) && (!isTv || allSeasonsAvailable)
                                val isProcessing = seerrStatus == 2 || seerrStatus == 3
                                val isPartial = seerrStatus == 4 || (isTv && !allSeasonsAvailable && state.availableSeasons.isNotEmpty())

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
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Select Seasons",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.LightGray
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    TextButton(
                                                        onClick = { viewModel.selectAllSeasons() },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Select All", fontSize = 10.sp)
                                                    }
                                                    TextButton(
                                                        onClick = { viewModel.deselectAllSeasons() },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Deselect All", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                            val displaySeasons = if (state.seerrSeasons.isEmpty()) listOf(SeerrSeasonInfo(1, null)) else state.seerrSeasons
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                displaySeasons.forEach { seasonInfo ->
                                                    val seasonNum = seasonInfo.seasonNumber
                                                    val isSelected = state.selectedSeasons.contains(seasonNum)
                                                    val isAvailable = state.availableSeasons.contains(seasonNum)
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .width(100.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(if (isSelected) com.example.anilistapp.ui.theme.SeerrPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                            .border(
                                                                width = if (isSelected) 2.dp else 1.dp,
                                                                color = if (isSelected) com.example.anilistapp.ui.theme.SeerrPurple else (if(isAvailable) Color.Green.copy(alpha=0.5f) else Color.Gray.copy(alpha=0.3f)),
                                                                shape = RoundedCornerShape(12.dp)
                                                            )
                                                            .alpha(if (isAvailable) 0.6f else 1.0f)
                                                            .combinedClickable(
                                                                onClick = { if (!isAvailable) viewModel.onSeasonToggle(seasonNum) },
                                                                onLongClick = { 
                                                                    viewModel.loadDetails("${state.title} Season $seasonNum")
                                                                }
                                                            )
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                                                                AsyncImage(
                                                                    model = if (seasonInfo.posterPath != null) "https://image.tmdb.org/t/p/w300${seasonInfo.posterPath}" else state.posterUrl,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                                if (isAvailable) {
                                                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                                                        Surface(color = Color.Green, shape = CircleShape, modifier = Modifier.size(24.dp)) {
                                                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                                                                        }
                                                                    }
                                                                } else if (isSelected) {
                                                                    Box(modifier = Modifier.fillMaxSize().background(com.example.anilistapp.ui.theme.SeerrPurple.copy(alpha = 0.2f)))
                                                                }
                                                            }
                                                            Text(
                                                                "Season $seasonNum",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                modifier = Modifier.padding(vertical = 4.dp),
                                                                color = if(isAvailable) Color.Green else (if(isSelected) com.example.anilistapp.ui.theme.SeerrPurple else Color.White)
                                                            )
                                                        }
                                                    }
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


