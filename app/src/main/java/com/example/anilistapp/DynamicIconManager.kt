package com.example.anilistapp

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class DynamicIconManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun setChadIconEnabled(enabled: Boolean) {
        val packageManager = context.packageManager
        
        val defaultComponent = ComponentName(context, "com.example.anilistapp.MainActivityDefault")
        val chadComponent = ComponentName(context, "com.example.anilistapp.MainActivityChad")

        if (enabled) {
            // Enable Chad, Disable Default
            packageManager.setComponentEnabledSetting(
                chadComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                defaultComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            // Enable Default, Disable Chad
            packageManager.setComponentEnabledSetting(
                defaultComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                chadComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
