package com.example.anilistapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anilistapp.ui.theme.CardDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onMediaClick: (String, Int, String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = { Text("Search Anime & Movies") },
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (state.aniListResults.isNotEmpty()) {
                    item {
                        Text(
                            "AniList Search",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(state.aniListResults, key = { it.id }) { result ->
                        SearchItem(
                            title = result.title?.userPreferred ?: "Unknown",
                            overview = result.description ?: "No description.",
                            posterUrl = result.coverImage?.extraLarge,
                            genres = result.genres?.filterNotNull() ?: emptyList(),
                            type = result.type?.name,
                            format = result.format?.name,
                            showTags = state.showSearchTags,
                            isInList = result.mediaListEntry != null,
                            onAddClick = { viewModel.addToWatchlist(result.title?.userPreferred ?: "") }
                        ) {
                            onMediaClick(result.title?.userPreferred ?: "", result.id, result.type?.name ?: "ANIME")
                        }
                    }
                }

                if (state.kitsuResults.isNotEmpty()) {
                    item {
                        Text(
                            "Anime (Kitsu)",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(state.kitsuResults, key = { "kitsu_${it.id}" }) { result ->
                        SearchItem(
                            title = result.title,
                            overview = result.synopsis,
                            posterUrl = result.posterUrl,
                            genres = emptyList(), // Kitsu search results don't have genres in this repo
                            type = if (result.isAnime) "ANIME" else "MANGA",
                            showTags = state.showSearchTags,
                            isInList = false, // Status unknown for Kitsu-only results
                            onAddClick = { viewModel.addToWatchlist(result.title) }
                        ) {
                            onMediaClick(result.title, -1, if (result.isAnime) "ANIME" else "MANGA")
                        }
                    }
                }

                if (state.seerrResults.isNotEmpty()) {
                    item {
                        Text(
                            "Seerr Discovery",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(16.dp),
                            color = com.example.anilistapp.ui.theme.SeerrPurple
                        )
                    }
                    items(state.seerrResults, key = { "seerr_${it.id}" }) { result ->
                        SearchItem(
                            title = result.title,
                            overview = result.overview,
                            posterUrl = null,
                            genres = emptyList(),
                            type = result.type.uppercase(),
                            showTags = state.showSearchTags,
                            isInList = false,
                            onAddClick = { viewModel.addToWatchlist(result.title) }
                        ) {
                            onMediaClick(result.title, -1, result.type.uppercase())
                        }
                    }
                }

                state.customResults.forEach { (sourceName, results) ->
                    item {
                        Text(
                            sourceName,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    items(results) { result ->
                        SearchItem(
                            title = result.title,
                            overview = result.synopsis,
                            posterUrl = result.posterUrl,
                            genres = emptyList(),
                            type = "CUSTOM",
                            showTags = state.showSearchTags,
                            isInList = false,
                            onAddClick = { viewModel.addToWatchlist(result.title) }
                        ) {
                            onMediaClick(result.title, -1, "ANIME")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItem(
    title: String,
    overview: String,
    posterUrl: String?,
    genres: List<String>,
    type: String?,
    format: String? = null,
    showTags: Boolean = true,
    isInList: Boolean,
    onAddClick: () -> Unit,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            if (posterUrl != null) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = title,
                    modifier = Modifier.width(80.dp).fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.width(80.dp).fillMaxHeight().background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                }
            }
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                
                if (showTags) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (type != null) {
                            Surface(
                                color = if (type == "MANGA") Color(0xFFE91E63) else Color(0xFF2196F3),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = type,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (format != null) {
                            Surface(
                                color = Color.DarkGray,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = format,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                        genres.take(2).forEach { genre ->
                            Surface(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = genre,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(overview.replace(Regex("<[^>]*>"), ""), fontSize = 12.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant, overflow = TextOverflow.Ellipsis)
            }
            
            if (!isInList) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Add to Watchlist", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "In List",
                    tint = Color.Green,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp).size(24.dp)
                )
            }
        }
    }
}
