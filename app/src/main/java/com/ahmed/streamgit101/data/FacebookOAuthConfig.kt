package com.ahmed.streamgit101.data

import android.net.Uri

object FacebookOAuthConfig {
    const val APP_ID = "1041366175430588"
    const val REDIRECT_URI = "https://www.facebook.com/connect/login_success.html"
    const val SCOPES = "email,public_profile,user_videos"
    const val AUTH_URL_BASE = "https://www.facebook.com/v19.0/dialog/oauth"
    const val GRAPH_API_BASE = "https://graph.facebook.com/v19.0"

    fun buildLoginUrl(): String {
        return Uri.parse(AUTH_URL_BASE).buildUpon()
            .appendQueryParameter("client_id", APP_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("scope", SCOPES)
            .build()
            .toString()
    }
}
