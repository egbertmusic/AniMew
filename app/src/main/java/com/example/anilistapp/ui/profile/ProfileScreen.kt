package com.example.anilistapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anilistapp.ui.components.LocalizableText
import com.example.anilistapp.type.MediaListStatus
import com.example.anilistapp.GetUserStatsQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    LocalizableText(
                        text = "Profile",
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = state.viewer?.avatar?.large,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.viewer?.name ?: "User",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Summary Comparison
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SummaryItem("Anime", "${state.animeStats?.count ?: 0}", MaterialTheme.colorScheme.primary)
                            SummaryItem("Manga", "${state.mangaStats?.count ?: 0}", MaterialTheme.colorScheme.secondary)
                            val totalEntries = (state.animeStats?.count ?: 0) + (state.mangaStats?.count ?: 0)
                            SummaryItem("Total", "$totalEntries", MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Modern Tab Row
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Anime", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Manga", modifier = Modifier.padding(vertical = 12.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedTab == 0) {
                    state.animeStats?.let { stats ->
                        AnimeStatsSection(stats)
                    }
                } else {
                    state.mangaStats?.let { stats ->
                        MangaStatsSection(stats)
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AnimeStatsSection(stats: GetUserStatsQuery.Anime) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val days = stats.minutesWatched / 1440.0
            val displayTime = if (days >= 1.0) "%.1f Days".format(days) else "${stats.minutesWatched} Mins"
            StatItem("Time Watched", displayTime)
            StatItem("Episodes", stats.episodesWatched.toString())
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Avg Score", "%.1f".format(stats.meanScore))
            StatItem("Count", stats.count.toString())
        }

        ChartSectionTitle("Anime Status Distribution")
        StatusPieChart(stats.statuses?.filterNotNull()?.map { StatusData(it.status, it.count) } ?: emptyList())
        
        ChartSectionTitle("Top Anime Genres")
        GenreBarChart(stats.genres?.filterNotNull()?.map { GenreData(it.genre, it.count) } ?: emptyList())
    }
}

@Composable
fun MangaStatsSection(stats: GetUserStatsQuery.Manga) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Chapters", stats.chaptersRead.toString())
            StatItem("Volumes", stats.volumesRead.toString())
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Avg Score", "%.1f".format(stats.meanScore))
            StatItem("Count", stats.count.toString())
        }

        ChartSectionTitle("Manga Status Distribution")
        StatusPieChart(stats.statuses?.filterNotNull()?.map { StatusData(it.status, it.count) } ?: emptyList())
        
        ChartSectionTitle("Top Manga Genres")
        GenreBarChart(stats.genres?.filterNotNull()?.map { GenreData(it.genre, it.count) } ?: emptyList())
    }
}

@Composable
fun ChartSectionTitle(title: String) {
    Spacer(modifier = Modifier.height(32.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
}

data class StatusData(val status: MediaListStatus?, val count: Int)
data class GenreData(val genre: String?, val count: Int)

@Composable
fun StatusPieChart(data: List<StatusData>) {
    val total = data.sumOf { it.count }
    if (total == 0) return

    val colors = mapOf(
        MediaListStatus.COMPLETED to Color(0xFF4CAF50),
        MediaListStatus.CURRENT to Color(0xFF2196F3),
        MediaListStatus.PLANNING to Color(0xFF9E9E9E),
        MediaListStatus.DROPPED to Color(0xFFF44336),
        MediaListStatus.PAUSED to Color(0xFFFF9800),
        MediaListStatus.REPEATING to Color(0xFFE91E63)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val density = LocalDensity.current
            val strokeWidth = with(density) { 20.dp.toPx() }
            
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = -90f
                data.forEach { item ->
                    val sweepAngle = (item.count.toFloat() / total) * 360f
                    drawArc(
                        color = colors[item.status] ?: Color.LightGray,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                data.forEach { item ->
                    val percentage = (item.count.toFloat() / total * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors[item.status] ?: Color.LightGray))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}: $percentage%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreBarChart(data: List<GenreData>) {
    if (data.isEmpty()) return
    val maxCount = data.maxOf { it.count }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            data.take(6).forEach { genre ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(genre.genre ?: "Unknown", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${genre.count}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (maxCount > 0) genre.count.toFloat() / maxCount else 0f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
