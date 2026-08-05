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

class SeasonalWidget : GlanceAppWidget() {
    
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
        Column(
            modifier = GlanceModifier.appBackground(colors, state.themeMode)
        ) {
            WidgetHeader(colors)
            
            if (state.seasonal.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No seasonal data",
                        style = TextStyle(color = colors.onBackground, fontSize = 12.sp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(state.seasonal) { item ->
                        SeasonalItemRow(item, state, colors)
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetHeader(colors: WidgetColors) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trending This Season",
                style = TextStyle(
                    color = colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )

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
    private fun SeasonalItemRow(item: WidgetMediaItem, state: MediaWidgetState, colors: WidgetColors) {
        val accentColor = try {
            item.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF3DBBFF)
        } catch (e: Exception) {
            Color(0xFF3DBBFF)
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(colors.surface)
                .cornerRadius(state.cornerRadius.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.localImageUri != null) {
                val bitmap = BitmapFactory.decodeFile(item.localImageUri)
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = item.title,
                        modifier = GlanceModifier
                            .size(50.dp, 75.dp)
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
                if (item.totalEpisodes != null) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "${item.totalEpisodes} Episodes",
                        style = TextStyle(color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }

    @Composable
    private fun PlaceholderBox(color: Color, radius: Int) {
        Box(
            modifier = GlanceModifier
                .size(50.dp, 75.dp)
                .background(ColorProvider(color))
                .cornerRadius(radius.dp)
        ) {}
    }
}

class SeasonalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SeasonalWidget()
}
