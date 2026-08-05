package com.example.anilistapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.anilistapp.ui.theme.*
import com.example.anilistapp.widget.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetManagementScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeWidgets by remember { mutableStateOf<List<WidgetInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadWidgets() {
        scope.launch {
            val manager = GlanceAppWidgetManager(context)
            val infoList = mutableListOf<WidgetInfo>()
            
            val widgets = listOf(
                MediaWidget() to "Media Widget",
                WatchlistWidget() to "Watchlist Widget",
                SeasonalWidget() to "Seasonal Widget",
                AiringWidget() to "Airing Widget",
                StatsWidget() to "Stats Widget",
                QuickActionsWidget() to "Quick Actions Widget"
            )

            widgets.forEach { (widget, name) ->
                manager.getGlanceIds(widget.javaClass).forEach { id ->
                    val state = getAppWidgetState(context, MediaWidgetStateDefinition, id)
                    infoList.add(WidgetInfo(name, widget.javaClass.simpleName, id, state.themeMode, widget))
                }
            }
            activeWidgets = infoList
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadWidgets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Widgets") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (activeWidgets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No active home screen widgets found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(activeWidgets) { info ->
                    WidgetInfoCard(
                        info = info,
                        onThemeChange = { newTheme ->
                            scope.launch {
                                updateAppWidgetState(context, MediaWidgetStateDefinition, info.glanceId) { old ->
                                    old.copy(themeMode = newTheme.name)
                                }
                                info.widget.update(context, info.glanceId)
                                loadWidgets()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetInfoCard(
    info: WidgetInfo,
    onThemeChange: (AppTheme) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(info.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "Active",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
            
            Text("Select Theme", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    if (theme != AppTheme.CUSTOM) {
                        val isSelected = info.currentTheme == theme.name
                        ThemePreviewItemSmall(
                            theme = theme,
                            isSelected = isSelected,
                            onClick = { onThemeChange(theme) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Live Preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                WidgetPreview(info.type, info.currentTheme)
            }
        }
    }
}

@Composable
fun WidgetPreview(type: String, themeName: String) {
    val widgetColors = when (themeName) {
        "LIGHT" -> Triple(Color.White, Color(0xFFF5F5F5), Color.Black)
        "AMOLED" -> Triple(Color.Black, Color(0xFF1C262F), Color.White)
        "SAKURA" -> Triple(Color(0xFFFFF0F3), Color.White, Color(0xFF5A3A41))
        "FOREST" -> Triple(Color(0xFFE8F5E9), Color.White, Color(0xFF2D5A27))
        "DRACULA" -> Triple(Color(0xFF282A36), Color(0xFF44475A), Color(0xFFF8F8F2))
        "LIQUID_GLASS" -> Triple(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.1f), Color.White)
        "CYBERPUNK" -> Triple(CyberpunkBlack, CyberpunkSurface, CyberpunkBlue)
        "GENSHIN" -> Triple(GenshinParchment, Color.White, GenshinDark)
        else -> Triple(DarkGrey, SurfaceDark, Color.White)
    }

    val (bg, surface, text) = widgetColors
    val isGenshin = themeName == "GENSHIN"
    val isCyberpunk = themeName == "CYBERPUNK"
    val accent = if (isCyberpunk) CyberpunkPink else if (isGenshin) GenshinGold else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (themeName == "LIQUID_GLASS") Color.DarkGray.copy(alpha = 0.5f) else bg)
            .then(
                if (themeName == "LIQUID_GLASS") Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                else if (isGenshin) Modifier.border(2.dp, GenshinGold, RoundedCornerShape(28.dp))
                else if (isCyberpunk) Modifier.border(1.dp, CyberpunkBlue.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                else Modifier
            )
            .padding(16.dp)
    ) {
        if (themeName == "LIQUID_GLASS") {
            Box(modifier = Modifier.fillMaxSize().blur(30.dp).background(Color.White.copy(alpha = 0.15f)))
        }
        
        if (isGenshin) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(GenshinGold.copy(alpha = 0.3f))
                    .align(Alignment.TopCenter)
            )
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(14.dp)
                            .clip(CircleShape)
                            .background(if (isCyberpunk) CyberpunkBlue else if (isGenshin) GenshinDark else accent)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(text.copy(alpha = 0.4f))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isCyberpunk) CyberpunkPink else accent.copy(alpha = 0.6f))
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            when (type) {
                "StatsWidget" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatPreviewItem(surface, text, isGenshin, Modifier.weight(1f))
                            StatPreviewItem(surface, text, isGenshin, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatPreviewItem(surface, text, isGenshin, Modifier.weight(1f))
                            StatPreviewItem(surface, text, isGenshin, Modifier.weight(1f))
                        }
                    }
                }
                "QuickActionsWidget" -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(2) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(surface)
                                    .then(if (isGenshin) Modifier.border(1.dp, GenshinGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)) else Modifier)
                                    .padding(12.dp)
                            ) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(accent.copy(alpha = 0.7f)))
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.width(30.dp).height(6.dp).clip(CircleShape).background(text.copy(alpha = 0.5f)))
                            }
                        }
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(surface)
                                    .then(if (isGenshin) Modifier.border(1.dp, GenshinGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)) else Modifier)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp, 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accent.copy(alpha = 0.3f))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Box(modifier = Modifier.size(130.dp, 10.dp).clip(CircleShape).background(text))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(modifier = Modifier.size(80.dp, 8.dp).clip(CircleShape).background(text.copy(alpha = 0.4f)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPreviewItem(surface: Color, text: Color, isGenshin: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(55.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(surface)
            .then(if (isGenshin) Modifier.border(1.dp, GenshinGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp)) else Modifier)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(32.dp, 14.dp).clip(CircleShape).background(text))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.size(44.dp, 6.dp).clip(CircleShape).background(text.copy(alpha = 0.4f)))
    }
}

@Composable
fun ThemePreviewItemSmall(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = when (theme) {
        AppTheme.LIGHT -> Color.White
        AppTheme.DARK -> Color(0xFF151F2E)
        AppTheme.AMOLED -> Color.Black
        AppTheme.SAKURA -> Color(0xFFFFB7C5)
        AppTheme.FOREST -> Color(0xFF2D5A27)
        AppTheme.DRACULA -> Color(0xFFBD93F9)
        AppTheme.LIQUID_GLASS -> Color.White.copy(alpha = 0.5f)
        AppTheme.CYBERPUNK -> CyberpunkPink
        AppTheme.GENSHIN -> GenshinGold
        AppTheme.CUSTOM -> Color.Gray
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(2.dp, borderColor, CircleShape)
        )
        Text(
            text = theme.name.lowercase().take(3),
            style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

data class WidgetInfo(
    val name: String,
    val type: String,
    val glanceId: GlanceId,
    val currentTheme: String,
    val widget: androidx.glance.appwidget.GlanceAppWidget
)
