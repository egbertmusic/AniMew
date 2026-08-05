package com.example.anilistapp.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.network.okHttpClient
import com.example.anilistapp.data.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            // Extremely safe token retrieval
            val token = runBlocking { 
                try {
                    withTimeoutOrNull(3000L) {
                        tokenManager.tokenFlow.firstOrNull() 
                    }
                } catch (e: Exception) { null }
            }
            val request = chain.request().newBuilder()
            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
            }
            request.addHeader("Accept", "application/json")
            
            // Log for debugging (remove in prod)
            // Log.d("NetworkModule", "Request: ${chain.request().url}")
            
            try {
                chain.proceed(request.build())
            } catch (e: Exception) {
                // If it fails, retry once for transient network errors
                chain.proceed(request.build())
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
        return ApolloClient.Builder()
            .serverUrl("https://graphql.anilist.co")
            .okHttpClient(okHttpClient)
            .normalizedCache(cacheFactory)
            .build()
    }
}
