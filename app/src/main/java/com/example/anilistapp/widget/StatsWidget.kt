package com.example.anilistapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.anilistapp.R
import com.example.anilistapp.widget.WidgetTheme.appBackground

class StatsWidget : GlanceAppWidget() {
    
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.viewerName ?: "Anime Stats",
                    style = TextStyle(
                        color = colors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Image(
                    provider = ImageProvider(android.R.drawable.stat_notify_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(20.dp)
                        .clickable(actionRunCallback<RefreshCallback>()),
                    colorFilter = ColorFilter.tint(colors.primary)
                )
            }
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            val stats = state.stats
            if (stats == null) {
                Text(text = "No stats available", style = TextStyle(color = colors.secondary))
            } else {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        StatItem("Watched", stats.count.toString(), colors, state, GlanceModifier.defaultWeight())
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        StatItem("Episodes", stats.episodesWatched.toString(), colors, state, GlanceModifier.defaultWeight())
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        val days = (stats.minutesWatched / 1440.0)
                        StatItem("Days", "%.1f".format(days), colors, state, GlanceModifier.defaultWeight())
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        StatItem("Mean Score", stats.meanScore.toString(), colors, state, GlanceModifier.defaultWeight())
                    }
                }
            }
        }
    }

    @Composable
    private fun StatItem(label: String, value: String, colors: WidgetColors, state: MediaWidgetState, modifier: GlanceModifier) {
        Column(
            modifier = modifier
                .background(colors.surface)
                .cornerRadius(state.cornerRadius.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = TextStyle(
                    color = colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = TextStyle(color = colors.secondary, fontSize = 10.sp)
            )
        }
    }
}

class StatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatsWidget()
}
