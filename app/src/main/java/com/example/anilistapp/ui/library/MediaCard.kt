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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.anilistapp.GetUserListQuery
import com.example.anilistapp.ui.theme.AniListTheme
import com.example.anilistapp.ui.components.LocalizableText
import com.example.anilistapp.data.LocalizedMediaDetails
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
    appLanguages: Set<String> = setOf("ENGLISH"),
    primaryLanguage: String = "ENGLISH",
    randomizeUiLanguage: Boolean = false,
    localizationManager: com.example.anilistapp.ui.components.LocalizationManager? = null,
    localizedDetails: LocalizedMediaDetails? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val media = entry.media ?: return
    val seerrStatusOverride = seerrStatus ?: if (com.example.anilistapp.AnilistApplication.instance.applicationContext.let { false }) 6 else null 
    // Wait, I can't check manualAvailableIds easily here without passing it.
    val totalEpisodes = media.episodes ?: media.chapters ?: 0
    val progress = entry.progress ?: 0
    val nextEpisode = media.nextAiringEpisode?.episode
    val epsBehind = if (nextEpisode != null) nextEpisode - 1 - progress else 0
    val glass = AniListTheme.glass
    val type = media.type?.name ?: "ANIME"

    val title = localizedDetails?.title?.takeIf { it.isNotBlank() } ?: if (primaryLanguage != "ENGLISH") {
        media.title?.userPreferred
    } else {
        when (titleLanguage) {
            "ENGLISH" -> media.title?.english ?: media.title?.userPreferred
            "NATIVE" -> media.title?.native ?: media.title?.userPreferred
            else -> media.title?.romaji ?: media.title?.userPreferred
        }
    } ?: "Unknown"

    val posterModel = localizedDetails?.posterPath ?: media.coverImage?.extraLarge ?: media.coverImage?.large

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(320.dp)
            .then(
                if (glass.useBlur) {
                    Modifier.border(0.5.dp, glass.borderColor, RoundedCornerShape(28.dp))
                } else Modifier
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
                        .combinedClickable(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMediaClick(title, media.id, type) 
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClick()
                            }
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterModel)
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
                    // Make the title area also clickable
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onMediaClick(title, media.id, type) 
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick()
                                }
                            )
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (localizationManager != null) {
                            val localizedStatus = if (totalEpisodes > 0) {
                                val episodesText = localizationManager.translate("Episodes", primaryLanguage)
                                "$totalEpisodes $episodesText"
                            } else {
                                localizationManager.translate("Ongoing", primaryLanguage)
                            }
                            
                            Text(
                                text = localizedStatus,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = if (totalEpisodes > 0) "$totalEpisodes Episodes" else "Ongoing",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$progress / ${if (totalEpisodes > 0) totalEpisodes else "?"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (totalEpisodes > 0) {
                                    Text(
                                        text = "${(progress * 100 / totalEpisodes)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = if (totalEpisodes > 0) progress.toFloat() / totalEpisodes.toFloat() else 0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))

                        if (!isDisableUpdateOn) {
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                    .padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { 
                                        if (progress > 0) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDecrease(media.id, progress - 1) 
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { 
                                        if (totalEpisodes == 0 || progress < totalEpisodes) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onIncrease(media.id, progress + 1) 
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
