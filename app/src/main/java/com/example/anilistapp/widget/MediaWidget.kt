package com.example.anilistapp.widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.anilistapp.R
import com.example.anilistapp.ui.theme.AniListBlue
import com.example.anilistapp.widget.WidgetTheme.appBackground

class MediaWidget : GlanceAppWidget() {
    
    override val stateDefinition = MediaWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<MediaWidgetState>()
            val colors = WidgetTheme.getColors(state)
            WidgetContent(state, colors)
        }
    }

    @Composable
    private fun WidgetContent(state: MediaWidgetState, colors: WidgetColors) {
        val filteredList = when (state.filter) {
            WidgetFilter.ANIME -> state.watching.filter { it.type == "ANIME" }
            WidgetFilter.MANGA -> state.watching.filter { it.type == "MANGA" }
            WidgetFilter.BOTH -> state.watching
        }

        Column(
            modifier = GlanceModifier.appBackground(colors, state.themeMode)
        ) {
            WidgetHeader(state.viewerName, state.filter, colors)
            
            if (filteredList.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items to show",
                        style = TextStyle(color = colors.onBackground, fontSize = 12.sp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(filteredList) { item ->
                        MediaItemRow(item, state, colors)
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetHeader(userName: String?, filter: WidgetFilter, colors: WidgetColors) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Currently Watching",
                    style = TextStyle(
                        color = colors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (userName != null) {
                    Text(
                        text = userName.uppercase(),
                        style = TextStyle(
                            color = colors.secondary, 
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Filter Toggle
            Text(
                text = filter.name,
                style = TextStyle(color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(colors.surface)
                    .cornerRadius(4.dp)
                    .clickable(actionRunCallback<ToggleFilterCallback>())
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Refresh Button
            Image(
                provider = ImageProvider(android.R.drawable.stat_notify_sync),
                contentDescription = "Refresh",
                modifier = GlanceModifier
                    .size(24.dp)
                    .clickable(actionRunCallback<RefreshCallback>()),
                colorFilter = ColorFilter.tint(colors.primary)
            )
        }
    }

    @Composable
    private fun MediaItemRow(item: WidgetMediaItem, state: MediaWidgetState, colors: WidgetColors) {
        val accentColor = try {
            item.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF3DBBFF)
        } catch (e: Exception) {
            Color(0xFF3DBBFF)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .background(colors.surface)
                .cornerRadius(state.cornerRadius.dp)
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    if (item.localImageUri != null) {
                        val bitmap = BitmapFactory.decodeFile(item.localImageUri)
                        if (bitmap != null) {
                            Image(
                                provider = ImageProvider(bitmap),
                                contentDescription = item.title,
                                modifier = GlanceModifier
                                    .size(64.dp, 96.dp)
                                    .cornerRadius((state.cornerRadius / 2).dp)
                                    .background(colors.secondary),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            PlaceholderImage(accentColor, state.cornerRadius / 2)
                        }
                    } else {
                        PlaceholderImage(accentColor, state.cornerRadius / 2)
                    }
                }
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = item.title,
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type,
                            style = TextStyle(
                                color = colors.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = GlanceModifier
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .background(colors.surface)
                                .cornerRadius(4.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "${if (item.type == "ANIME") "Ep" else "Ch"} ${item.progress}${item.totalEpisodes?.let { "/$it" } ?: ""}",
                            style = TextStyle(color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    if (state.showProgress && item.totalEpisodes != null && item.totalEpisodes > 0) {
                        Spacer(modifier = GlanceModifier.height(10.dp))
                        val progress = item.progress.toFloat() / item.totalEpisodes.toFloat()
                        ProgressBar(progress, accentColor, colors.secondary)
                    }
                }
            }
        }
    }

    @Composable
    private fun ProgressBar(progress: Float, color: Color, backgroundColor: ColorProvider) {
        val coercedProgress = progress.coerceIn(0f, 1f)
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(4.dp)
                .background(backgroundColor)
                .cornerRadius(2.dp)
        ) {
            if (coercedProgress > 0f) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(end = (100 - (coercedProgress * 100)).toInt().dp) // This is a hack for Glance 1.0.0
                        .height(4.dp)
                        .background(ColorProvider(color))
                        .cornerRadius(2.dp)
                ) {}
            }
        }
    }

    @Composable
    private fun PlaceholderImage(color: Color, radius: Int) {
        Box(
            modifier = GlanceModifier
                .size(64.dp, 96.dp)
                .background(ColorProvider(color))
                .cornerRadius(radius.dp)
        ) {}
    }
}

class MediaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediaWidget()
}
