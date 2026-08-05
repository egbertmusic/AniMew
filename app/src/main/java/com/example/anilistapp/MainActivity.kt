package com.example.anilistapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState("DARK")
            val customThemeJson by settingsRepository.customThemeJson.collectAsState(null)
            val enableDiscoverFeed by settingsRepository.enableDiscoverFeed.collectAsState(true)
            val enableProfileTab by settingsRepository.enableProfileTab.collectAsState(true)
            
            val appTheme = try { AppTheme.valueOf(themeMode) } catch (e: Exception) { AppTheme.DARK }
            val customTheme = remember(customThemeJson) {
                customThemeJson?.let { 
                    try { Json.decodeFromString<CustomTheme>(it) } catch (e: Exception) { null }
                }
            }

            AnilistAppTheme(theme = appTheme, customTheme = customTheme) {
                    val navController = rememberNavController()
                    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

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
                                    
                                    // Only show bottom bar on top-level screens AND if enabled in settings
                                    val showBottomBar = (enableDiscoverFeed || enableProfileTab) && 
                                        currentDestination?.route in listOf("library", "discover", "profile")
                                    
                                    if (showBottomBar) {
                                        val glass = AniListTheme.glass
                                        
                                        Surface(
                                            modifier = Modifier
                                                .padding(horizontal = 24.dp, vertical = 20.dp)
                                                .fillMaxWidth()
                                                .then(
                                                    if (glass.useBlur) {
                                                        Modifier.border(0.5.dp, glass.borderColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                                    } else {
                                                        Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                                    }
                                                ),
                                            shape = RoundedCornerShape(24.dp),
                                            color = if (glass.useBlur) Color.White.copy(alpha = 0.03f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                            tonalElevation = if (glass.useBlur) 0.dp else 12.dp
                                        ) {
                                            NavigationBar(
                                                containerColor = Color.Transparent,
                                                tonalElevation = 0.dp,
                                                windowInsets = WindowInsets(0, 0, 0, 0),
                                                modifier = Modifier.height(64.dp)
                                            ) {
                                                NavigationBarItem(
                                                    icon = { Icon(Icons.Default.CollectionsBookmark, contentDescription = null) },
                                                    label = { Text("Library") },
                                                    selected = currentDestination?.route == "library",
                                                    onClick = {
                                                        navController.navigate("library") {
                                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    },
                                                    colors = NavigationBarItemDefaults.colors(
                                                        selectedIconColor = if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary,
                                                        selectedTextColor = if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary,
                                                        unselectedIconColor = if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        unselectedTextColor = if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        indicatorColor = if (glass.useBlur) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                                                    )
                                                )
                                                if (enableDiscoverFeed) {
                                                    NavigationBarItem(
                                                        icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                                                        label = { Text("Discover") },
                                                        selected = currentDestination?.route == "discover",
                                                        onClick = {
                                                            navController.navigate("discover") {
                                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                                launchSingleTop = true
                                                                restoreState = true
                                                            }
                                                        },
                                                        colors = NavigationBarItemDefaults.colors(
                                                            selectedIconColor = if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary,
                                                            selectedTextColor = if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary,
                                                            unselectedIconColor = if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            unselectedTextColor = if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            indicatorColor = if (glass.useBlur) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                    )
                                                }
                                                if (enableProfileTab) {
                                                    NavigationBarItem(
                                                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                                                        label = { Text("Profile") },
                                                        selected = currentDestination?.route == "profile",
                                                        onClick = {
                                                            navController.navigate("profile") {
                                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                                launchSingleTop = true
                                                                restoreState = true
                                                            }
                                                        },
                                                        colors = NavigationBarItemDefaults.colors(
                                                            selectedIconColor = if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary,
                                                            selectedTextColor = if (glass.useBlur) Color.White else MaterialTheme.colorScheme.primary,
                                                            unselectedIconColor = if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            unselectedTextColor = if (glass.useBlur) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            indicatorColor = if (glass.useBlur) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                    )
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
