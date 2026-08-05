package com.example.anilistapp.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anilistapp.ui.components.LocalizableText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onMediaClick: (String, Int, String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    LocalizableText(
                        text = "Discover",
                        languages = state.appLanguages,
                        randomize = state.randomizeUiLanguage,
                        localizationManager = viewModel.localizationManager
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = contentPadding.calculateBottomPadding())
            ) {
                DiscoverSection("Trending Now", state.trending.map { 
                    DiscoverMedia(it.id, it.title?.userPreferred ?: "Unknown", it.coverImage?.large ?: "", "ANIME") 
                }, onMediaClick)
                
                DiscoverSection("Current Season", state.seasonal.map { 
                    DiscoverMedia(it.id, it.title?.userPreferred ?: "Unknown", it.coverImage?.large ?: "", "ANIME") 
                }, onMediaClick)

                DiscoverSection("Airing Today", state.airingToday.map { 
                    DiscoverMedia(it.media?.id ?: 0, it.media?.title?.userPreferred ?: "Unknown", it.media?.coverImage?.large ?: "", "ANIME") 
                }, onMediaClick)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DiscoverSection(
    title: String,
    items: List<DiscoverMedia>,
    onMediaClick: (String, Int, String) -> Unit
) {
    if (items.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                DiscoverItem(item, onMediaClick)
            }
        }
    }
}

@Composable
fun DiscoverItem(
    media: DiscoverMedia,
    onMediaClick: (String, Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onMediaClick(media.title, media.id, media.type) }
    ) {
        AsyncImage(
            model = media.coverUrl,
            contentDescription = media.title,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = media.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

data class DiscoverMedia(
    val id: Int,
    val title: String,
    val coverUrl: String,
    val type: String
)
