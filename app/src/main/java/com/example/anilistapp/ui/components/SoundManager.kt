package com.example.anilistapp.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import android.util.Log
import com.example.anilistapp.data.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Map to store loaded sound IDs
    private val soundMap = mutableMapOf<Int, Int>()
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize ToneGenerator", e)
        }
        // Load default system sounds as placeholders for SFX
        // These can be replaced by user's piano sounds in res/raw
        loadDefaultSfx()
    }

    private fun loadDefaultSfx() {
        // Mappings for standard UI sounds
    }

    fun playClick() {
        playSfx(AudioManager.FX_KEY_CLICK)
    }

    fun playSwish() {
        // Use a navigation sound for swish
        playSfx(AudioManager.FX_FOCUS_NAVIGATION_DOWN)
    }

    fun playSuccess() {
        // Unfortunately standard playSoundEffect doesn't have a "success" sound
        // We'll use CLICK for now, but user can load a custom one in res/raw/success.mp3
        playSfx(AudioManager.FX_KEY_CLICK)
    }

    fun playError() {
        // Fallback for older APIs
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            playSfx(AudioManager.FX_BACK)
        } else {
            playSfx(AudioManager.FX_KEY_CLICK)
        }
    }

    fun playSfx(effectId: Int = AudioManager.FX_KEY_CLICK) {
        scope.launch {
            if (settingsRepository.enableSfx.first()) {
                // If we have a custom sound for this effect, play it from SoundPool
                val customSoundId = soundMap[effectId]
                if (customSoundId != null) {
                    soundPool.play(customSoundId, 1f, 1f, 1, 0, 1f)
                } else {
                    // Fallback to a synthetic tone if system sound is silent
                    try {
                        val generator = toneGenerator
                        if (generator != null) {
                            generator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                        } else {
                            audioManager.playSoundEffect(effectId)
                        }
                    } catch (e: Exception) {
                        audioManager.playSoundEffect(effectId)
                    }
                }
            }
        }
    }

    /**
     * Call this to load a custom piano sound for a specific action.
     * Example: loadCustomSfx(R.raw.piano_click, AudioManager.FX_KEY_CLICK)
     */
    fun loadCustomSfx(resourceId: Int, mappingId: Int) {
        val id = soundPool.load(context, resourceId, 1)
        soundMap[mappingId] = id
    }

    fun startBgm(resourceId: Int? = null) {
        scope.launch {
            if (settingsRepository.enableBgm.first()) {
                if (mediaPlayer == null && resourceId != null) {
                    try {
                        mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                            isLooping = true
                            start()
                        }
                    } catch (e: Exception) {
                        Log.e("SoundManager", "Failed to start BGM", e)
                    }
                } else {
                    mediaPlayer?.start()
                }
            }
        }
    }

    fun stopBgm() {
        mediaPlayer?.pause()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        toneGenerator?.release()
        toneGenerator = null
        soundPool.release()
    }
}
