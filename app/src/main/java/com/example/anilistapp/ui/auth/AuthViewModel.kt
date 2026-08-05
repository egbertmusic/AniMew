package com.example.anilistapp.ui.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.Constants
import com.example.anilistapp.data.TokenManager
import com.example.anilistapp.widget.WidgetWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            tokenManager.tokenFlow.collect { token ->
                _isLoggedIn.value = token != null
            }
        }
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            tokenManager.saveToken(token)
        }
    }

    fun handleAuthRedirect(uri: Uri) {
        // Authorization Code flow returns code in query parameter: animew://auth?code=...
        val code = uri.getQueryParameter("code")
        if (code != null) {
            exchangeCodeForToken(code)
        }
    }

    private fun exchangeCodeForToken(code: String) {
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val requestBody = FormBody.Builder()
                        .add("grant_type", "authorization_code")
                        .add("client_id", Constants.ANILIST_CLIENT_ID)
                        .add("client_secret", Constants.ANILIST_CLIENT_SECRET)
                        .add("redirect_uri", Constants.REDIRECT_URI)
                        .add("code", code)
                        .build()

                    val request = Request.Builder()
                        .url(Constants.TOKEN_URL)
                        .post(requestBody)
                        .header("Accept", "application/json")
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            json.getString("access_token")
                        } else null
                    } else null
                }

                if (token != null) {
                    tokenManager.saveToken(token)
                    WidgetWorker.enqueueOneTime(context.applicationContext)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.deleteToken()
        }
    }
}
