package com.example.anilistapp

import com.example.anilistapp.BuildConfig

object Constants {
    // Uses the ID from build.gradle.kts / local.properties
    val ANILIST_CLIENT_ID: String = BuildConfig.ANILIST_CLIENT_ID
    val ANILIST_CLIENT_SECRET: String = BuildConfig.ANILIST_CLIENT_SECRET
    const val REDIRECT_URI = "animew://auth"
    
    // Switch to response_type=code as AniList has deprecated response_type=token for new apps
    val AUTH_URL = "https://anilist.co/api/v2/oauth/authorize?client_id=$ANILIST_CLIENT_ID&redirect_uri=$REDIRECT_URI&response_type=code"
    const val TOKEN_URL = "https://anilist.co/api/v2/oauth/token"
}
