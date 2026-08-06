package com.example.anilistapp.data

import com.apollographql.apollo.ApolloClient
import com.example.anilistapp.*
import com.example.anilistapp.type.MediaListStatus
import com.example.anilistapp.type.MediaSeason
import com.example.anilistapp.type.MediaType
import com.example.anilistapp.type.MediaSort
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun getViewer() = apolloClient.query(GetViewerQuery()).execute()

    suspend fun getUserList(userId: Int, type: MediaType, status: MediaListStatus?, forceRefresh: Boolean = false) =
        apolloClient.query(
            GetUserListQuery(
                userId = Optional.present(userId),
                type = Optional.present(type),
                status = Optional.presentIfNotNull(status)
            )
        ).fetchPolicy(if (forceRefresh) FetchPolicy.NetworkOnly else FetchPolicy.CacheFirst)
        .execute()

    suspend fun getSeasonalMedia(season: MediaSeason, year: Int) =
        apolloClient.query(
            GetSeasonalMediaQuery(
                season = Optional.present(season),
                seasonYear = Optional.present(year)
            )
        ).execute()

    suspend fun getAiringSchedule(start: Int, end: Int) =
        apolloClient.query(
            GetAiringScheduleQuery(
                start = Optional.present(start),
                end = Optional.present(end)
            )
        ).execute()

    suspend fun getTrendingMedia(page: Int = 1, perPage: Int = 10) =
        apolloClient.query(
            GetTrendingMediaQuery(
                page = Optional.present(page),
                perPage = Optional.present(perPage)
            )
        ).execute()

    suspend fun getUserStats(userId: Int) =
        apolloClient.query(GetUserStatsQuery(id = Optional.present(userId))).execute()

    suspend fun updateProgress(mediaId: Int, progress: Int) =
        apolloClient.mutation(
            UpdateMediaProgressMutation(
                mediaId = Optional.present(mediaId),
                progress = Optional.present(progress)
            )
        ).execute()

    suspend fun saveMediaListEntry(mediaId: Int, status: MediaListStatus) =
        apolloClient.mutation(
            SaveMediaListEntryMutation(
                mediaId = Optional.present(mediaId),
                status = Optional.present(status)
            )
        ).execute()

    suspend fun deleteMediaListEntry(mediaId: Int) =
        apolloClient.mutation(
            DeleteMediaListEntryMutation(
                id = Optional.present(mediaId)
            )
        ).execute()

    suspend fun searchAniList(title: String, type: MediaType? = null) =
        apolloClient.query(
            SearchAniListQuery(
                search = Optional.present(title),
                type = Optional.presentIfNotNull(type)
            )
        ).fetchPolicy(FetchPolicy.CacheFirst)
        .execute()

    suspend fun getMediaDetails(id: Int) =
        apolloClient.query(GetMediaDetailsQuery(id = Optional.present(id)))
            .fetchPolicy(FetchPolicy.CacheFirst)
            .execute()

    suspend fun getShortsMedia(
        page: Int = 1,
        perPage: Int = 20,
        type: MediaType? = MediaType.ANIME,
        sort: List<MediaSort>? = listOf(MediaSort.TRENDING_DESC),
        genres: List<String>? = null
    ) =
        apolloClient.query(
            GetShortsMediaQuery(
                page = Optional.present(page),
                perPage = Optional.present(perPage),
                type = Optional.presentIfNotNull(type),
                sort = Optional.presentIfNotNull(sort),
                genres = Optional.presentIfNotNull(genres)
            )
        ).execute()
}
