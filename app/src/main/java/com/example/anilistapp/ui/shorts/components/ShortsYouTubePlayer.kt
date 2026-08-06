package com.example.anilistapp.ui.shorts.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback

@Composable
fun ShortsYouTubePlayer(
    videoId: String,
    isPlaying: Boolean,
    subtitleLanguage: String? = null,
    onCurrentSecond: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    key(videoId, subtitleLanguage) {
        var activePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
        
        val youtubePlayerView = remember {
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)
                
                val options = IFramePlayerOptions.Builder()
                    .controls(0)
                    .rel(0)
                    .autoplay(1)
                    .ccLoadPolicy(1)
                    .apply {
                        subtitleLanguage?.let { langPref(it) }
                    }
                    .origin("https://www.youtube-nocookie.com")
                    .build()

                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        activePlayer = youTubePlayer
                        youTubePlayer.loadVideo(videoId, 0f)
                        if (!isPlaying) {
                            youTubePlayer.pause()
                        }
                    }

                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        onCurrentSecond(second)
                    }
                }, options)
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                youtubePlayerView.release()
            }
        }

        LaunchedEffect(isPlaying, activePlayer) {
            if (isPlaying) {
                activePlayer?.play()
            } else {
                activePlayer?.pause()
            }
        }

        AndroidView(
            factory = { youtubePlayerView },
            modifier = modifier
        )
    }
}
