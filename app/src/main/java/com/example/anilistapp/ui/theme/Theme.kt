package com.example.anilistapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppTheme {
    LIGHT, DARK, AMOLED, SAKURA, FOREST, DRACULA, LIQUID_GLASS, CYBERPUNK, GENSHIN, CUSTOM
}

@kotlinx.serialization.Serializable
data class CustomTheme(
    val name: String = "Custom",
    val primary: String = "#3DBBFF",
    val secondary: String = "#3577FF",
    val tertiary: String = "#C063D8",
    val background: String = "#0B1622",
    val surface: String = "#151F2E",
    val onPrimary: String = "#FFFFFF",
    val onBackground: String = "#FFFFFF",
    val isDark: Boolean = true
) {
    fun toColorScheme(): ColorScheme {
        return if (isDark) {
            darkColorScheme(
                primary = Color(android.graphics.Color.parseColor(primary)),
                secondary = Color(android.graphics.Color.parseColor(secondary)),
                tertiary = Color(android.graphics.Color.parseColor(tertiary)),
                background = Color(android.graphics.Color.parseColor(background)),
                surface = Color(android.graphics.Color.parseColor(surface)),
                onPrimary = Color(android.graphics.Color.parseColor(onPrimary)),
                onBackground = Color(android.graphics.Color.parseColor(onBackground))
            )
        } else {
            lightColorScheme(
                primary = Color(android.graphics.Color.parseColor(primary)),
                secondary = Color(android.graphics.Color.parseColor(secondary)),
                tertiary = Color(android.graphics.Color.parseColor(tertiary)),
                background = Color(android.graphics.Color.parseColor(background)),
                surface = Color(android.graphics.Color.parseColor(surface)),
                onPrimary = Color(android.graphics.Color.parseColor(onPrimary)),
                onBackground = Color(android.graphics.Color.parseColor(onBackground))
            )
        }
    }
}

data class GlassProperties(
    val useBlur: Boolean = false,
    val blurRadius: Dp = 0.dp,
    val containerColor: Color = Color.Transparent,
    val borderColor: Color = Color.Transparent
)

val LocalGlassProperties = staticCompositionLocalOf { GlassProperties() }

object AniListTheme {
    val glass: GlassProperties
        @Composable
        @ReadOnlyComposable
        get() = LocalGlassProperties.current
}

private val DarkColorScheme = darkColorScheme(
    primary = AniListBlue,
    secondary = AniListTeal,
    tertiary = AniListGreen,
    background = AmoledBlack,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = AniListBlue,
    secondary = AniListTeal,
    tertiary = AniListGreen,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val SakuraColorScheme = lightColorScheme(
    primary = SakuraDark,
    secondary = SakuraPink,
    tertiary = AniListTeal,
    background = SakuraSurface,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = SakuraDark,
    onTertiary = Color.White,
    onBackground = SakuraDark,
    onSurface = SakuraDark,
)

private val ForestColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = ForestLight,
    tertiary = AniListTeal,
    background = ForestSurface,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = ForestGreen,
    onTertiary = Color.White,
    onBackground = ForestGreen,
    onSurface = ForestGreen,
)

private val DraculaColorScheme = darkColorScheme(
    primary = DraculaPurple,
    secondary = DraculaPink,
    tertiary = DraculaGreen,
    background = DraculaBackground,
    surface = DraculaCurrentLine,
    onPrimary = DraculaBackground,
    onSecondary = DraculaBackground,
    onTertiary = DraculaBackground,
    onBackground = DraculaForeground,
    onSurface = DraculaForeground,
)

private val LiquidGlassColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = Color.White.copy(alpha = 0.8f),
    tertiary = AniListTeal,
    background = Color.Black,
    surface = Color.White.copy(alpha = 0.15f),
    surfaceVariant = Color.White.copy(alpha = 0.25f),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

private val StandardDarkColorScheme = darkColorScheme(
    primary = AniListBlue,
    secondary = AniListTeal,
    tertiary = AniListGreen,
    background = DarkGrey,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkBlue,
    secondary = CyberpunkPink,
    tertiary = CyberpunkYellow,
    background = CyberpunkBlack,
    surface = CyberpunkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = CyberpunkBlue,
    onSurface = CyberpunkBlue,
)

private val GenshinColorScheme = lightColorScheme(
    primary = GenshinDark,
    secondary = GenshinGold,
    tertiary = AniListBlue,
    background = GenshinParchment,
    surface = GenshinWhite,
    onPrimary = Color.White,
    onSecondary = GenshinDark,
    onBackground = GenshinDark,
    onSurface = GenshinDark,
)

@Composable
fun AnilistAppTheme(
    theme: AppTheme = AppTheme.DARK,
    customTheme: CustomTheme? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> StandardDarkColorScheme
        AppTheme.AMOLED -> DarkColorScheme
        AppTheme.SAKURA -> SakuraColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.DRACULA -> DraculaColorScheme
        AppTheme.LIQUID_GLASS -> LiquidGlassColorScheme
        AppTheme.CYBERPUNK -> CyberpunkColorScheme
        AppTheme.GENSHIN -> GenshinColorScheme
        AppTheme.CUSTOM -> customTheme?.toColorScheme() ?: StandardDarkColorScheme
    }

    val glassProperties = if (theme == AppTheme.LIQUID_GLASS) {
        GlassProperties(
            useBlur = true,
            blurRadius = 30.dp,
            containerColor = Color.White.copy(alpha = 0.12f),
            borderColor = Color.White.copy(alpha = 0.25f)
        )
    } else {
        GlassProperties()
    }

    CompositionLocalProvider(LocalGlassProperties provides glassProperties) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
