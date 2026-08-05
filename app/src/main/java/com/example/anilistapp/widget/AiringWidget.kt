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
import java.text.SimpleDateFormat
import java.util.*

class AiringWidget : GlanceAppWidget() {
    
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
            
            if (state.airing.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No episodes airing today",
                        style = TextStyle(color = colors.onBackground, fontSize = 12.sp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(state.airing) { item ->
                        AiringItemRow(item, state, colors)
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
                text = "Airing Today",
                style = TextStyle(
                    color = colors.accent,
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
                colorFilter = ColorFilter.tint(colors.accent)
            )
        }
    }

    @Composable
    private fun AiringItemRow(item: WidgetAiringItem, state: MediaWidgetState, colors: WidgetColors) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val airTime = timeFormat.format(Date(item.airingAt.toLong() * 1000))

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
                        modifier = GlanceModifier.size(40.dp).cornerRadius(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.title,
                    style = TextStyle(color = colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "Episode ${item.episode}",
                    style = TextStyle(color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                )
            }
            
            Box(
                modifier = GlanceModifier
                    .background(colors.surface)
                    .cornerRadius(8.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = airTime,
                    style = TextStyle(
                        color = colors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class AiringWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AiringWidget()
}
