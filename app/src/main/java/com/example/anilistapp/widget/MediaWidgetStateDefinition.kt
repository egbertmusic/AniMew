package com.example.anilistapp.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object MediaWidgetStateDefinition : GlanceStateDefinition<MediaWidgetState> {

    private val Context.dataStore by preferencesDataStore(name = "media_widget_state")

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<MediaWidgetState> {
        val stateKey = stringPreferencesKey("widget_state_$fileKey")
        
        return object : DataStore<MediaWidgetState> {
            override val data: Flow<MediaWidgetState> = context.dataStore.data.map { prefs ->
                val json = prefs[stateKey]
                if (json != null) {
                    Json.decodeFromString<MediaWidgetState>(json)
                } else {
                    MediaWidgetState()
                }
            }

            override suspend fun updateData(transform: suspend (t: MediaWidgetState) -> MediaWidgetState): MediaWidgetState {
                val current = data.first()
                val new = transform(current)
                context.dataStore.edit { prefs ->
                    prefs[stateKey] = Json.encodeToString(new)
                }
                return new
            }
        }
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.applicationContext.filesDir, "datastore/$fileKey")
    }
}
