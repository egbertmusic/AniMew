package com.example.anilistapp.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.anilistapp.ui.theme.AnilistAppTheme
import kotlinx.coroutines.launch

class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            AnilistAppTheme {
                ConfigurationScreen(appWidgetId) {
                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    setResult(RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(appWidgetId: Int, onFinish: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var themeMode by remember { mutableStateOf("DARK") }
    var opacity by remember { mutableFloatStateOf(1.0f) }
    var cornerRadius by remember { mutableIntStateOf(16) }
    var showProgress by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(appWidgetId) {
        // Here we could load existing state if we wanted to pre-fill
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Configuration") },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            saveAndFinish(context, appWidgetId, themeMode, opacity, cornerRadius, showProgress, onFinish)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Done")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
                val themes = listOf("DARK", "LIGHT", "AMOLED", "SAKURA", "FOREST", "DRACULA", "LIQUID_GLASS")
                themes.forEach { theme ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        RadioButton(selected = themeMode == theme, onClick = { themeMode = theme })
                        Text(theme, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Opacity: ${(opacity * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
                Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.1f..1.0f)

                Spacer(Modifier.height(24.dp))
                Text("Corner Radius: ${cornerRadius}dp", style = MaterialTheme.typography.titleMedium)
                Slider(value = cornerRadius.toFloat(), onValueChange = { cornerRadius = it.toInt() }, valueRange = 0f..40f)

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showProgress, onCheckedChange = { showProgress = it })
                    Text("Show Progress Bar", modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        scope.launch {
                            saveAndFinish(context, appWidgetId, themeMode, opacity, cornerRadius, showProgress, onFinish)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Configuration")
                }
            }
        }
    }
}

private suspend fun saveAndFinish(
    context: Context,
    appWidgetId: Int,
    themeMode: String,
    opacity: Float,
    cornerRadius: Int,
    showProgress: Boolean,
    onFinish: () -> Unit
) {
    val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
    updateAppWidgetState(context, MediaWidgetStateDefinition, glanceId) { old ->
        old.copy(
            themeMode = themeMode,
            opacity = opacity,
            cornerRadius = cornerRadius,
            showProgress = showProgress
        )
    }
    
    // Update all widgets to be safe (or we could detect which one it is)
    MediaWidget().update(context, glanceId)
    WatchlistWidget().update(context, glanceId)
    AiringWidget().update(context, glanceId)
    SeasonalWidget().update(context, glanceId)
    StatsWidget().update(context, glanceId)
    QuickActionsWidget().update(context, glanceId)
    
    onFinish()
}
