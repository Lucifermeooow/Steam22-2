package com.ahmed.streamgit101.data

data class StreamStatistics(
    val bitrate: String = "--",
    val fps: String = "--",
    val rtt: String = "--",
    val durationSeconds: Long = 0L
)

enum class CameraLens {
    BACK,
    FRONT
}

data class PlatformOAuthCredential(
    val platformId: String,
    val displayName: String,
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "https://your-server.example.com/auth/callback",
    val streamKey: String = "",
    val rtmpIngestUrl: String = "",
    val isConnected: Boolean = false,
    val scopes: String = "",
    val authEndpoint: String = ""
)

data class StreamUiState(
    val rtmpUrl: String = "rtmp://your-server/live/stream-key",
    val backendUrl: String = "https://your-server.example.com",
    val isInitialized: Boolean = false,
    val isLive: Boolean = false,
    val isBusy: Boolean = false,
    val isMuted: Boolean = false,
    val currentLens: CameraLens = CameraLens.BACK,
    val statusMessage: String = "جاهز للبث",
    val stats: StreamStatistics = StreamStatistics(),
    val destinations: Map<String, Boolean> = mapOf(
        "YouTube" to true,
        "Twitch" to true,
        "Facebook" to true,
        "TikTok" to true
    ),
    val oauthCredentials: Map<String, PlatformOAuthCredential> = mapOf(
        "youtube" to PlatformOAuthCredential(
            platformId = "youtube",
            displayName = "YouTube Live",
            rtmpIngestUrl = "rtmp://a.rtmp.youtube.com/live2",
            scopes = "https://www.googleapis.com/auth/youtube.force-ssl",
            authEndpoint = "https://accounts.google.com/o/oauth2/v2/auth"
        ),
        "twitch" to PlatformOAuthCredential(
            platformId = "twitch",
            displayName = "Twitch",
            rtmpIngestUrl = "rtmp://live.twitch.tv/app/",
            scopes = "channel:manage:broadcast user:read:email",
            authEndpoint = "https://id.twitch.tv/oauth2/authorize"
        ),
        "facebook" to PlatformOAuthCredential(
            platformId = "facebook",
            displayName = "Facebook Live",
            rtmpIngestUrl = "rtmps://live-api-s.facebook.com:443/rtmp/",
            scopes = "publish_video,pages_show_list",
            authEndpoint = "https://www.facebook.com/v19.0/dialog/oauth"
        ),
        "tiktok" to PlatformOAuthCredential(
            platformId = "tiktok",
            displayName = "TikTok Live",
            rtmpIngestUrl = "rtmp://live.tiktok.com/live/",
            scopes = "video.upload,user.info.basic",
            authEndpoint = "https://www.tiktok.com/v2/auth/authorize/"
        )
    ),
    val isOAuthConfigOpen: Boolean = false,
    val selectedOAuthTab: String = "youtube",
    val hasCameraPermission: Boolean = false,
    val hasAudioPermission: Boolean = false,
    val snackbarMessage: String? = null
)

