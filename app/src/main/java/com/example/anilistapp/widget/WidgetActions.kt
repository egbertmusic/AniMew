package com.example.anilistapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.EntryPointAccessors

class RefreshCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetWorker.enqueueOneTime(context)
    }
}

class ToggleFilterCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, MediaWidgetStateDefinition, glanceId) { old ->
            val nextFilter = when (old.filter) {
                WidgetFilter.BOTH -> WidgetFilter.ANIME
                WidgetFilter.ANIME -> WidgetFilter.MANGA
                WidgetFilter.MANGA -> WidgetFilter.BOTH
            }
            old.copy(filter = nextFilter)
        }
        
        // Refresh the widget to show the new filter state
        MediaWidget().update(context, glanceId)
        WatchlistWidget().update(context, glanceId)
    }
}

class UpdateProgressCallback : ActionCallback {
    companion object {
        val MEDIA_ID = ActionParameters.Key<Int>("media_id")
        val NEW_PROGRESS = ActionParameters.Key<Int>("new_progress")
    }
    
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val mediaId = parameters[MEDIA_ID] ?: return
        val newProgress = parameters[NEW_PROGRESS] ?: return
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetWorker.WidgetWorkerEntryPoint::class.java
        )
        val repository = entryPoint.repository()
        
        try {
            repository.updateProgress(mediaId, newProgress)
            WidgetWorker.enqueueOneTime(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
