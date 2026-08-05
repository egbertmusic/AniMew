package com.example.anilistapp.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import com.example.anilistapp.MainActivity
import com.example.anilistapp.R
import com.example.anilistapp.widget.WidgetTheme.appBackground

class QuickActionsWidget : GlanceAppWidget() {

    override val stateDefinition = MediaWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<MediaWidgetState>()
            val colors = WidgetTheme.getColors(state)
            val ctx = LocalContext.current
            WidgetContent(ctx, state, colors)
        }
    }

    @Composable
    private fun WidgetContent(context: Context, state: MediaWidgetState, colors: WidgetColors) {
        Row(
            modifier = GlanceModifier.appBackground(colors, state.themeMode),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionIcon(
                text = "Open App",
                iconRes = android.R.drawable.ic_menu_send,
                onClick = actionStartActivity(Intent(context, MainActivity::class.java)),
                state = state,
                colors = colors
            )
            Spacer(modifier = GlanceModifier.width(24.dp))
            ActionIcon(
                text = "Refresh",
                iconRes = android.R.drawable.stat_notify_sync,
                onClick = actionRunCallback<RefreshCallback>(),
                state = state,
                colors = colors
            )
        }
    }

    @Composable
    private fun ActionIcon(text: String, iconRes: Int, onClick: androidx.glance.action.Action, state: MediaWidgetState, colors: WidgetColors) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier
                .background(colors.surface)
                .cornerRadius(state.cornerRadius.dp)
                .padding(12.dp)
                .clickable(onClick)
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = text,
                modifier = GlanceModifier.size(32.dp),
                colorFilter = ColorFilter.tint(colors.primary)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = text,
                style = TextStyle(
                    color = colors.onSurface, 
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickActionsWidget()
}
