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
        "Facebook" to true,
        "TikTok" to true
    ),
    val hasCameraPermission: Boolean = false,
    val hasAudioPermission: Boolean = false,
    val snackbarMessage: String? = null
)
