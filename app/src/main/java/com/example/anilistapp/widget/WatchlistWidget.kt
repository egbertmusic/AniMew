package com.example.anilistapp.widget

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
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
import com.example.anilistapp.widget.WidgetTheme.appBackground

class WatchlistWidget : GlanceAppWidget() {
    
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
            WidgetFilter.ANIME -> state.planning.filter { it.type == "ANIME" }
            WidgetFilter.MANGA -> state.planning.filter { it.type == "MANGA" }
            WidgetFilter.BOTH -> state.planning
        }

        Column(
            modifier = GlanceModifier.appBackground(colors, state.themeMode)
        ) {
            WidgetHeader(state.filter, colors)
            
            if (filteredList.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your watchlist is empty",
                        style = TextStyle(color = colors.onBackground, fontSize = 12.sp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(filteredList) { item ->
                        WatchlistItemRow(item, state, colors)
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetHeader(filter: WidgetFilter, colors: WidgetColors) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Watchlist",
                style = TextStyle(
                    color = colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )

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
    private fun WatchlistItemRow(item: WidgetMediaItem, state: MediaWidgetState, colors: WidgetColors) {
        val accentColor = try {
            item.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF4CAF50)
        } catch (e: Exception) {
            Color(0xFF4CAF50)
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .background(colors.surface)
                .cornerRadius(state.cornerRadius.dp)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.localImageUri != null) {
                val bitmap = BitmapFactory.decodeFile(item.localImageUri)
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = item.title,
                        modifier = GlanceModifier
                            .size(56.dp, 84.dp)
                            .cornerRadius((state.cornerRadius / 2).dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    PlaceholderBox(accentColor, state.cornerRadius / 2)
                }
            } else {
                PlaceholderBox(accentColor, state.cornerRadius / 2)
            }
            
            Spacer(modifier = GlanceModifier.width(12.dp))
            
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.title,
                    style = TextStyle(color = colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold),
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
                    if (item.totalEpisodes != null) {
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "${item.totalEpisodes} ${if (item.type == "ANIME") "Eps" else "Chs"}",
                            style = TextStyle(color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PlaceholderBox(color: Color, radius: Int) {
        Box(
            modifier = GlanceModifier
                .size(56.dp, 84.dp)
                .background(ColorProvider(color))
                .cornerRadius(radius.dp)
        ) {}
    }
}

class WatchlistWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WatchlistWidget()
}
