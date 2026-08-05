package com.example.anilistapp.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.*
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.anilistapp.data.MediaRepository
import com.example.anilistapp.data.SettingsRepository
import com.example.anilistapp.data.TokenManager
import com.example.anilistapp.type.MediaListStatus
import com.example.anilistapp.type.MediaSeason
import com.example.anilistapp.type.MediaType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class WidgetWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetWorkerEntryPoint {
        fun repository(): MediaRepository
        fun tokenManager(): TokenManager
        fun settingsRepository(): SettingsRepository
    }

    override suspend fun doWork(): ListenableWorker.Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WidgetWorkerEntryPoint::class.java
        )
        val repository = entryPoint.repository()
        val tokenManager = entryPoint.tokenManager()
        val settingsRepository = entryPoint.settingsRepository()

        val token = tokenManager.tokenFlow.first() ?: return ListenableWorker.Result.failure()
        val themeMode = settingsRepository.widgetThemeMode.first()

        return try {
            val viewerResponse = repository.getViewer()
            val viewerId = viewerResponse.data?.Viewer?.id ?: return ListenableWorker.Result.failure()
            val viewerName = viewerResponse.data?.Viewer?.name

            // Fetch Watching
            val animeWatching = fetchList(repository, viewerId, MediaType.ANIME, MediaListStatus.CURRENT)
            val mangaWatching = fetchList(repository, viewerId, MediaType.MANGA, MediaListStatus.CURRENT)
            val watching = (animeWatching + mangaWatching)
                .sortedByDescending { it.isNewRelease }
                .take(20)

            // Fetch Planning
            val animePlanning = fetchList(repository, viewerId, MediaType.ANIME, MediaListStatus.PLANNING)
            val mangaPlanning = fetchList(repository, viewerId, MediaType.MANGA, MediaListStatus.PLANNING)
            val planning = (animePlanning + mangaPlanning)
                .sortedBy { it.title }
                .take(20)

            // Fetch Seasonal
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val season = when (month) {
                in 2..4 -> MediaSeason.SPRING
                in 5..7 -> MediaSeason.SUMMER
                in 8..10 -> MediaSeason.FALL
                else -> MediaSeason.WINTER
            }

            val seasonalResponse = repository.getSeasonalMedia(season, year)
            val seasonalItems = mutableListOf<WidgetMediaItem>()
            seasonalResponse.data?.Page?.media?.forEach { media ->
                if (media != null) {
                    val localPath = downloadImage(media.coverImage?.extraLarge ?: media.coverImage?.large, "media_${media.id}")
                    seasonalItems.add(
                        WidgetMediaItem(
                            id = media.id,
                            title = media.title?.userPreferred ?: "Unknown",
                            progress = 0,
                            totalEpisodes = media.episodes,
                            imageUrl = media.coverImage?.large ?: "",
                            type = "ANIME",
                            color = media.coverImage?.color,
                            localImageUri = localPath
                        )
                    )
                }
            }
            val seasonal = seasonalItems.take(15)

            // Fetch Airing Schedule (Today)
            val startOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis / 1000
            val endOfDay = startOfDay + 86400
            val airingResponse = repository.getAiringSchedule(startOfDay.toInt(), endOfDay.toInt())
            val airingItems = mutableListOf<WidgetAiringItem>()
            airingResponse.data?.Page?.airingSchedules?.forEach { schedule ->
                val media = schedule?.media
                if (media != null) {
                    val localPath = downloadImage(media.coverImage?.extraLarge ?: media.coverImage?.large, "airing_${media.id}")
                    airingItems.add(
                        WidgetAiringItem(
                            id = media.id,
                            title = media.title?.userPreferred ?: "Unknown",
                            episode = schedule.episode,
                            airingAt = schedule.airingAt,
                            imageUrl = media.coverImage?.large ?: "",
                            localImageUri = localPath
                        )
                    )
                }
            }
            val airing = airingItems.take(15)

            // Fetch Stats
            val statsResponse = repository.getUserStats(viewerId)
            val animeStats = statsResponse.data?.User?.statistics?.anime
            val stats = animeStats?.let {
                WidgetUserStats(
                    count = it.count,
                    episodesWatched = it.episodesWatched,
                    minutesWatched = it.minutesWatched,
                    meanScore = it.meanScore
                )
            }

            val manager = GlanceAppWidgetManager(appContext)

            val newState = MediaWidgetState(
                watching = watching,
                planning = planning,
                seasonal = seasonal,
                airing = airing,
                stats = stats,
                viewerName = viewerName,
                themeMode = themeMode,
                lastUpdated = System.currentTimeMillis()
            )

            // Update all widgets manually to avoid reflection issues
            updateWidget(appContext, manager, MediaWidget(), newState)
            updateWidget(appContext, manager, WatchlistWidget(), newState)
            updateWidget(appContext, manager, SeasonalWidget(), newState)
            updateWidget(appContext, manager, AiringWidget(), newState)
            updateWidget(appContext, manager, StatsWidget(), newState)

            ListenableWorker.Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            ListenableWorker.Result.retry()
        }
    }

    private suspend fun updateWidget(
        context: Context,
        manager: GlanceAppWidgetManager,
        widget: GlanceAppWidget,
        newState: MediaWidgetState
    ) {
        manager.getGlanceIds(widget.javaClass).forEach { id ->
            updateAppWidgetState(context, MediaWidgetStateDefinition, id) { old ->
                newState.copy(
                    filter = old.filter,
                    themeMode = old.themeMode
                )
            }
            widget.update(context, id)
        }
    }

    private suspend fun fetchList(
        repository: MediaRepository,
        userId: Int,
        type: MediaType,
        status: MediaListStatus
    ): List<WidgetMediaItem> {
        return try {
            val response = repository.getUserList(userId, type, status)
            val items = mutableListOf<WidgetMediaItem>()
            response.data?.Page?.mediaList?.forEach { list ->
                val media = list?.media
                if (media != null) {
                    val localPath = downloadImage(media.coverImage?.extraLarge ?: media.coverImage?.large, "media_${media.id}")
                    
                    val isNew = if (type == MediaType.ANIME) {
                        val latestAired = media.nextAiringEpisode?.episode?.minus(1) ?: media.episodes ?: 0
                        (list.progress ?: 0) < latestAired
                    } else false

                    items.add(
                        WidgetMediaItem(
                            id = media.id,
                            title = media.title?.userPreferred ?: "Unknown",
                            progress = list.progress ?: 0,
                            totalEpisodes = if (type == MediaType.ANIME) media.episodes else media.chapters,
                            imageUrl = media.coverImage?.large ?: "",
                            type = type.name,
                            isNewRelease = isNew,
                            color = media.coverImage?.color,
                            localImageUri = localPath
                        )
                    )
                }
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun downloadImage(url: String?, fileName: String): String? {
        if (url == null) return null
        return try {
            val loader = ImageLoader(appContext)
            val request = ImageRequest.Builder(appContext)
                .data(url)
                .size(240, 360)
                .build()
            
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap
                val file = File(appContext.filesDir, "widget_images/$fileName.jpg")
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                file.absolutePath
            } else null
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<WidgetWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "widget_update_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun enqueueOneTime(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetWorker>()
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
