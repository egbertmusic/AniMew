package com.example.anilistapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.anilistapp.ui.auth.AuthViewModel
import com.example.anilistapp.ui.detail.MediaDetailScreen
import com.example.anilistapp.ui.library.LibraryScreen
import com.example.anilistapp.ui.login.LoginScreen
import com.example.anilistapp.ui.search.SearchScreen
import com.example.anilistapp.ui.settings.SettingsScreen
import com.example.anilistapp.ui.settings.WidgetManagementScreen
import com.example.anilistapp.ui.settings.ComplementManagementScreen
import com.example.anilistapp.ui.theme.AnilistAppTheme
import com.example.anilistapp.ui.theme.AppTheme
import com.example.anilistapp.ui.theme.AniListTheme
import com.example.anilistapp.ui.components.LocalizationManager
import com.example.anilistapp.ui.components.SoundManager
import com.example.anilistapp.ui.theme.CustomTheme
import com.example.anilistapp.data.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var localizationManager: LocalizationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState("DARK")
            val enableBgm by settingsRepository.enableBgm.collectAsState(false)
            
            LaunchedEffect(enableBgm) {
                if (enableBgm) {
                    // soundManager.startBgm(R.raw.ambient_piano) // User needs to add this
                    soundManager.startBgm()
                } else {
                    soundManager.stopBgm()
                }
            }
            
            val customThemeJson by settingsRepository.customThemeJson.collectAsState(null)
            val enableDiscoverFeed by settingsRepository.enableDiscoverFeed.collectAsState(true)
            val enableShortsFeed by settingsRepository.enableShortsFeed.collectAsState(true)
            val enableProfileTab by settingsRepository.enableProfileTab.collectAsState(true)
            val enableMewingChad by settingsRepository.enableMewingChad.collectAsState(false)
            val primaryAppLanguage by settingsRepository.primaryAppLanguage.collectAsState("ENGLISH")
            val randomizeUiLanguage by settingsRepository.randomizeUiLanguage.collectAsState(false)
            val appLanguages by settingsRepository.appLanguages.collectAsState(setOf("ENGLISH"))
            val shortsNavigationStyle by settingsRepository.shortsNavigationStyle.collectAsState("BOTTOM")
            
            val appTheme = try { AppTheme.valueOf(themeMode) } catch (e: Exception) { AppTheme.DARK }
            val customTheme = remember(customThemeJson) {
                customThemeJson?.let { 
                    try { Json.decodeFromString<CustomTheme>(it) } catch (e: Exception) { null }
                }
            }

            AnilistAppTheme(theme = appTheme, customTheme = customTheme) {
                    val navController = rememberNavController()
                    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                    val haptic = LocalHapticFeedback.current

                    if (isLoggedIn == null) {
                        // Loading state
                    } else if (isLoggedIn == true) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Scaffold(
                                containerColor = Color.Transparent,
                                bottomBar = {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentDestination = navBackStackEntry?.destination
                                    val currentRoute = currentDestination?.route
                                    
                                    // Only show bottom bar on top-level screens AND if enabled in settings
                                    var showBottomBar = (enableDiscoverFeed || enableShortsFeed || enableProfileTab) && 
                                        currentRoute in listOf("library", "discover", "shorts", "profile")
                                    
                                    // Special logic for shorts: hide bottom bar if style is not BOTTOM
                                    if (currentRoute == "shorts" && shortsNavigationStyle != "BOTTOM") {
                                        showBottomBar = false
                                    }
                                    
                                    if (showBottomBar) {
                                        val glass = AniListTheme.glass
                                        
                                        Surface(
                                            modifier = Modifier
                                                .padding(horizontal = 24.dp)
                                                .navigationBarsPadding()
                                                .padding(bottom = 16.dp)
                                                .fillMaxWidth()
                                                .then(
                                                    if (glass.useBlur) {
                                                        Modifier.border(0.5.dp, glass.borderColor.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                                                    } else {
                                                        Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                                                    }
                                                ),
                                            shape = RoundedCornerShape(28.dp),
                                            color = if (glass.useBlur) Color(0xFF1A1C1E) else MaterialTheme.colorScheme.surface,
                                            tonalElevation = if (glass.useBlur) 0.dp else 12.dp
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(80.dp)
                                                    .padding(horizontal = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val items = mutableListOf<Triple<String, String, Any>>()
                                                items.add(Triple("Library", "library", Icons.Default.CollectionsBookmark))
                                                if (enableDiscoverFeed) items.add(Triple("Discover", "discover", Icons.Default.Explore))
                                                if (enableShortsFeed) items.add(Triple("Shorts", "shorts", Icons.Default.Movie))
                                                if (enableProfileTab) {
                                                    items.add(Triple("Profile", "profile", Icons.Default.AccountCircle))
                                                }
                                                
                                                items.forEach { (label, route, icon) ->
                                                    val selected = currentDestination?.route == route
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .clickable(
                                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                                indication = null,
                                                                onClick = {
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    navController.navigate(route) {
                                                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                                        launchSingleTop = true
                                                                        restoreState = true
                                                                    }
                                                                }
                                                            ),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(width = 64.dp, height = 32.dp)
                                                                .background(
                                                                    color = if (selected) {
                                                                        if (glass.useBlur) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                                                                    } else Color.Transparent,
                                                                    shape = CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
                                                                Icon(
                                                                    imageVector = icon,
                                                                    contentDescription = null,
                                                                    tint = if (selected) {
                                                                        if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary
                                                                    } else {
                                                                        if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                                    }
                                                                )
                                                            } else if (icon is Int) {
                                                                androidx.compose.foundation.Image(
                                                                    painter = androidx.compose.ui.res.painterResource(id = icon),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(24.dp),
                                                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                                                    colorFilter = if (selected) {
                                                                        null // Keep original colors for chad? Or tint? 
                                                                    } else {
                                                                        androidx.compose.ui.graphics.ColorFilter.tint(if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant)
                                                                    }
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        com.example.anilistapp.ui.components.LocalizableText(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (selected) {
                                                                if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary
                                                            } else {
                                                                if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                            languages = appLanguages,
                                                            randomize = randomizeUiLanguage,
                                                            primaryLanguage = primaryAppLanguage,
                                                            localizationManager = localizationManager
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                NavHost(
                                    navController = navController, 
                                    startDestination = "library"
                                ) {
                                    composable("library") {
                                        LibraryScreen(
                                            onSettingsClick = { navController.navigate("settings") },
                                            onSearchClick = { navController.navigate("search") },
                                            onMediaClick = { title, id, type -> 
                                                val route = if (id != -1) "detail/$title?id=$id&type=$type" else "detail/$title?type=$type"
                                                navController.navigate(route)
                                            },
                                            contentPadding = innerPadding
                                        )
                                    }
                                    composable("discover") {
                                        com.example.anilistapp.ui.discover.DiscoverScreen(
                                            onMediaClick = { title, id, type -> 
                                                val route = if (id != -1) "detail/$title?id=$id&type=$type" else "detail/$title?type=$type"
                                                navController.navigate(route)
                                            },
                                            contentPadding = innerPadding
                                        )
                                    }
                                    composable("shorts") {
                                        com.example.anilistapp.ui.shorts.ShortsScreen(
                                            onMediaClick = { title, id, type ->
                                                val route = if (id != -1) "detail/$title?id=$id&type=$type" else "detail/$title?type=$type"
                                                navController.navigate(route)
                                            },
                                            onNavigate = { route ->
                                                navController.navigate(route) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                    composable("profile") {
                                        com.example.anilistapp.ui.profile.ProfileScreen(
                                            contentPadding = innerPadding
                                        )
                                    }
                                    composable("settings") {
                                        SettingsScreen(
                                            onBackClick = { navController.popBackStack() },
                                            onManageWidgetsClick = { navController.navigate("widget_management") },
                                            onManageComplementsClick = { navController.navigate("complement_management") }
                                        )
                                    }
                                    composable("widget_management") {
                                        WidgetManagementScreen(
                                            onBackClick = { navController.popBackStack() }
                                        )
                                    }
                                    composable("complement_management") {
                                        ComplementManagementScreen(
                                            onBackClick = { navController.popBackStack() }
                                        )
                                    }
                                    composable("search") {
                                        SearchScreen(
                                            onBackClick = { navController.popBackStack() },
                                            onMediaClick = { title, id, type -> 
                                                val route = if (id != -1) "detail/$title?id=$id&type=$type" else "detail/$title?type=$type"
                                                navController.navigate(route)
                                            }
                                        )
                                    }
                                    composable(
                                        route = "detail/{title}?id={id}&type={type}",
                                        arguments = listOf(
                                            navArgument("title") { type = NavType.StringType },
                                            navArgument("id") { 
                                                type = NavType.IntType
                                                defaultValue = -1
                                            },
                                            navArgument("type") {
                                                type = NavType.StringType
                                                nullable = true
                                                defaultValue = null
                                            }
                                        )
                                    ) { backStackEntry ->
                                        val title = backStackEntry.arguments?.getString("title") ?: ""
                                        val id = backStackEntry.arguments?.getInt("id").takeIf { it != -1 }
                                        val type = backStackEntry.arguments?.getString("type")
                                        MediaDetailScreen(
                                            title = title,
                                            mediaId = id,
                                            mediaType = type,
                                            onBackClick = { navController.popBackStack() }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "animew" && uri.host == "auth") {
                authViewModel.handleAuthRedirect(uri)
            }
        }
    }
}
