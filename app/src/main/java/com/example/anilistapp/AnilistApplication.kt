package com.example.anilistapp

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.example.anilistapp.widget.WidgetWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AnilistApplication : Application() {
    
    companion object {
        lateinit var instance: AnilistApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Fix for WebView crash on Android 9+ when multiple processes are used.
        // Even if not explicitly multi-process, some system components or 
        // WorkManager tasks might trigger this on certain devices.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }

        WidgetWorker.enqueue(this)
    }
}
