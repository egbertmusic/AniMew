package com.example.anilistapp.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilistapp.GetUserListQuery
import com.example.anilistapp.ui.theme.AniListTheme
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MediaCard(
    entry: GetUserListQuery.MediaList,
    isDisableUpdateOn: Boolean,
    seerrStatus: Int? = null,
    titleLanguage: String = "ROMAJI",
    showMultipleTitles: Boolean = false,
    onIncrease: (Int, Int) -> Unit,
    onDecrease: (Int, Int) -> Unit,
    onMediaClick: (String, Int, String) -> Unit,
    onLongClick: () -> Unit = {},
    isManualAvailable: Boolean = false,
    modifier: Modifier = Modifier
) {
    val media = entry.media ?: return
    val seerrStatusOverride = seerrStatus ?: if (com.example.anilistapp.AnilistApplication.instance.applicationContext.let { false }) 6 else null 
    // Wait, I can't check manualAvailableIds easily here without passing it.
    val totalEpisodes = media.episodes ?: media.chapters ?: 0
    val progress = entry.progress ?: 0
    val nextEpisode = media.nextAiringEpisode?.episode
    val epsBehind = if (nextEpisode != null) nextEpisode - 1 - progress else 0
    val glass = AniListTheme.glass
    val type = media.type?.name ?: "ANIME"

    val title = when (titleLanguage) {
        "ENGLISH" -> media.title?.english ?: media.title?.userPreferred
        "NATIVE" -> media.title?.native ?: media.title?.userPreferred
        else -> media.title?.romaji ?: media.title?.userPreferred
    } ?: "Unknown"

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(320.dp)
            .then(
                if (glass.useBlur) {
                    Modifier.border(0.5.dp, glass.borderColor, RoundedCornerShape(28.dp))
                } else Modifier
            )
            .combinedClickable(
                onClick = { onMediaClick(title, media.id, type) },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (glass.useBlur) Color.White.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(media.coverImage?.extraLarge)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    /* // Glassy Play Icon simulation
                    if (glass.useBlur) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add, // Placeholder for play
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = Color.White
                            )
                        }
                    } */

                    // Seerr Badge
                    if ((seerrStatus != null && seerrStatus >= 2) || isManualAvailable) {
                        Surface(
                            modifier = Modifier
                                .padding(20.dp)
                                .align(Alignment.TopEnd),
                            color = if (seerrStatus != null && seerrStatus >= 5 || isManualAvailable) Color(0xFF4CAF50).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = when {
                                    isManualAvailable -> Icons.Default.CheckCircle
                                    seerrStatus == 5 -> Icons.Default.CloudDone
                                    seerrStatus == 6 -> Icons.AutoMirrored.Filled.Dvr
                                    else -> Icons.Default.CloudDownload
                                },
                                contentDescription = null,
                                modifier = Modifier.padding(6.dp).size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = if (totalEpisodes > 0) "$totalEpisodes Episodes" else "Ongoing",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$progress / ${if (totalEpisodes > 0) totalEpisodes else "?"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (!isDisableUpdateOn) {
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), CircleShape)
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (progress > 0) onDecrease(media.id, progress - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurface)
                                }
                                VerticalDivider(modifier = Modifier.height(16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                IconButton(
                                    onClick = { if (totalEpisodes == 0 || progress < totalEpisodes) onIncrease(media.id, progress + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
