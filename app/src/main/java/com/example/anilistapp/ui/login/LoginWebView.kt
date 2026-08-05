package com.example.anilistapp.ui.login

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.anilistapp.Constants

@Composable
fun LoginWebView(
    onTokenReceived: (String) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true // Enable DOM storage for modern web apps
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        if (url.startsWith("animew://auth")) {
                            val fragment = request.url.fragment
                            if (fragment != null && fragment.contains("access_token=")) {
                                val token = fragment.split("&")
                                    .find { it.startsWith("access_token=") }
                                    ?.substringAfter("access_token=")
                                
                                if (token != null) {
                                    onTokenReceived(token)
                                    return true
                                }
                            }
                        }
                        return false
                    }
                }
                loadUrl(Constants.AUTH_URL)
            }
        }
    )
}
