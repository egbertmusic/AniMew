package com.example.anilistapp.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.example.anilistapp.R
import com.example.anilistapp.ui.theme.*

data class WidgetColors(
    val background: ColorProvider,
    val surface: ColorProvider,
    val primary: ColorProvider,
    val secondary: ColorProvider,
    val onSurface: ColorProvider,
    val onBackground: ColorProvider,
    val accent: ColorProvider,
    val isGlass: Boolean = false,
    val opacity: Float = 1.0f,
    val cornerRadius: Int = 16
)

object WidgetTheme {
    
    fun getColors(state: MediaWidgetState): WidgetColors {
        val themeMode = state.themeMode
        val baseColors = when (themeMode) {
            "LIGHT" -> WidgetColors(
                background = ColorProvider(Color.White),
                surface = ColorProvider(Color(0xFFF5F5F5)),
                primary = ColorProvider(AniListBlue),
                secondary = ColorProvider(Color.LightGray),
                onSurface = ColorProvider(Color.Black),
                onBackground = ColorProvider(Color.Black),
                accent = ColorProvider(AniListBlue)
            )
            "AMOLED" -> WidgetColors(
                background = ColorProvider(AmoledBlack),
                surface = ColorProvider(CardDark),
                primary = ColorProvider(AniListBlue),
                secondary = ColorProvider(Color.DarkGray),
                onSurface = ColorProvider(Color.White),
                onBackground = ColorProvider(Color.White),
                accent = ColorProvider(AniListBlue)
            )
            "SAKURA" -> WidgetColors(
                background = ColorProvider(SakuraSurface),
                surface = ColorProvider(Color.White),
                primary = ColorProvider(SakuraDark),
                secondary = ColorProvider(SakuraPink),
                onSurface = ColorProvider(SakuraDark),
                onBackground = ColorProvider(SakuraDark),
                accent = ColorProvider(SakuraDark)
            )
            "FOREST" -> WidgetColors(
                background = ColorProvider(ForestSurface),
                surface = ColorProvider(Color.White),
                primary = ColorProvider(ForestGreen),
                secondary = ColorProvider(ForestLight),
                onSurface = ColorProvider(ForestGreen),
                onBackground = ColorProvider(ForestGreen),
                accent = ColorProvider(ForestGreen)
            )
            "DRACULA" -> WidgetColors(
                background = ColorProvider(DraculaBackground),
                surface = ColorProvider(DraculaCurrentLine),
                primary = ColorProvider(DraculaPurple),
                secondary = ColorProvider(DraculaComment),
                onSurface = ColorProvider(DraculaForeground),
                onBackground = ColorProvider(DraculaForeground),
                accent = ColorProvider(DraculaPink)
            )
            "LIQUID_GLASS" -> WidgetColors(
                background = ColorProvider(Color(0x33FFFFFF)),
                surface = ColorProvider(Color(0x1AFFFFFF)),
                primary = ColorProvider(Color.White),
                secondary = ColorProvider(Color(0xB3FFFFFF)),
                onSurface = ColorProvider(Color.White),
                onBackground = ColorProvider(Color.White),
                accent = ColorProvider(Color.White),
                isGlass = true
            )
            "CYBERPUNK" -> WidgetColors(
                background = ColorProvider(CyberpunkBlack),
                surface = ColorProvider(CyberpunkSurface),
                primary = ColorProvider(CyberpunkBlue),
                secondary = ColorProvider(CyberpunkPink),
                onSurface = ColorProvider(CyberpunkBlue),
                onBackground = ColorProvider(CyberpunkPink),
                accent = ColorProvider(CyberpunkNeonYellow)
            )
            "GENSHIN" -> WidgetColors(
                background = ColorProvider(GenshinParchment),
                surface = ColorProvider(Color.White),
                primary = ColorProvider(GenshinDark),
                secondary = ColorProvider(GenshinGold),
                onSurface = ColorProvider(GenshinDark),
                onBackground = ColorProvider(GenshinDark),
                accent = ColorProvider(GenshinGold)
            )
            else -> WidgetColors( // DARK
                background = ColorProvider(DarkGrey),
                surface = ColorProvider(SurfaceDark),
                primary = ColorProvider(AniListBlue),
                secondary = ColorProvider(Color.Gray),
                onSurface = ColorProvider(Color.White),
                onBackground = ColorProvider(Color.White),
                accent = ColorProvider(AniListBlue)
            )
        }
        return baseColors.copy(
            opacity = state.opacity,
            cornerRadius = state.cornerRadius
        )
    }

    fun GlanceModifier.appBackground(colors: WidgetColors, themeMode: String = ""): GlanceModifier {
        val base = this.fillMaxSize()
            .padding(8.dp)
            .appWidgetBackground()
            
        return when {
            colors.isGlass -> base.background(ImageProvider(R.drawable.widget_glass_bg))
                .cornerRadius(colors.cornerRadius.dp)
            themeMode == "GENSHIN" -> base.background(ImageProvider(R.drawable.widget_genshin_bg))
                .cornerRadius(colors.cornerRadius.dp)
                .padding(top = 10.dp) // Offset for the decorative line
            themeMode == "CYBERPUNK" -> base.background(colors.background)
                .cornerRadius(colors.cornerRadius.dp)
            else -> base.background(colors.background)
                .cornerRadius(colors.cornerRadius.dp)
        }
    }
}
