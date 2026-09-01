package com.ahmed.streamgit101.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmed.streamgit101.data.CameraLens
import com.ahmed.streamgit101.data.StreamStatistics
import com.ahmed.streamgit101.data.StreamUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class StreamViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StreamUiState())
    val uiState: StateFlow<StreamUiState> = _uiState.asStateFlow()

    private var statsJob: Job? = null

    init {
        prepareCamera()
    }

    fun updatePermissions(cameraGranted: Boolean, audioGranted: Boolean) {
        _uiState.update {
            it.copy(
                hasCameraPermission = cameraGranted,
                hasAudioPermission = audioGranted,
                isInitialized = cameraGranted && audioGranted,
                statusMessage = if (cameraGranted && audioGranted) {
                    "الكاميرا جاهزة — أدخل RTMP واضغط GO LIVE"
                } else {
                    "يجب السماح للكاميرا والميكروفون."
                }
            )
        }
    }

    fun prepareCamera() {
        if (_uiState.value.isBusy || _uiState.value.isLive) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusMessage = "جاري تجهيز الكاميرا والميكروفون..."
                )
            }
            delay(600)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    isInitialized = true,
                    statusMessage = "الكاميرا جاهزة — أدخل RTMP واضغط GO LIVE"
                )
            }
        }
    }

    fun onRtmpUrlChanged(url: String) {
        _uiState.update { it.copy(rtmpUrl = url) }
    }

    fun onBackendUrlChanged(url: String) {
        _uiState.update { it.copy(backendUrl = url) }
    }

    fun switchCamera() {
        if (_uiState.value.isBusy || _uiState.value.isLive) return
        val nextLens = if (_uiState.value.currentLens == CameraLens.BACK) {
            CameraLens.FRONT
        } else {
            CameraLens.BACK
        }
        _uiState.update {
            it.copy(
                currentLens = nextLens,
                snackbarMessage = "تم تغيير الكاميرا"
            )
        }
    }

    fun toggleMute() {
        val nextMuted = !_uiState.value.isMuted
        _uiState.update {
            it.copy(
                isMuted = nextMuted,
                snackbarMessage = if (nextMuted) "تم كتم الميكروفون" else "تم تشغيل الميكروفون"
            )
        }
    }

    fun toggleDestination(name: String, enabled: Boolean) {
        if (_uiState.value.isLive) return
        val currentDestinations = _uiState.value.destinations.toMutableMap()
        currentDestinations[name] = enabled
        _uiState.update { it.copy(destinations = currentDestinations) }
    }

    fun startStreaming() {
        val state = _uiState.value
        if (state.isBusy || state.isLive) return

        val url = state.rtmpUrl.trim()
        if (!url.startsWith("rtmp://") && !url.startsWith("rtmps://")) {
            _uiState.update { it.copy(snackbarMessage = "اكتب RTMP URL صحيح.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusMessage = "جاري بدء البث..."
                )
            }
            delay(1000)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    isLive = true,
                    statusMessage = "🔴 LIVE — البث متصل بالسيرفر",
                    snackbarMessage = "تم بدء البث"
                )
            }
            startStatsPolling()
        }
    }

    fun stopStreaming() {
        val state = _uiState.value
        if (state.isBusy || !state.isLive) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusMessage = "جاري إيقاف البث..."
                )
            }
            statsJob?.cancel()
            delay(600)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    isLive = false,
                    statusMessage = "تم إيقاف البث",
                    stats = StreamStatistics(),
                    snackbarMessage = "تم إيقاف البث"
                )
            }
        }
    }

    private fun startStatsPolling() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            var seconds = 0L
            while (_uiState.value.isLive) {
                delay(2000)
                seconds += 2
                val bitrateKbps = 1450 + Random.nextInt(-50, 60)
                val fps = 30 + Random.nextInt(-1, 2)
                val rttMs = 38 + Random.nextInt(-4, 6)

                _uiState.update {
                    it.copy(
                        stats = StreamStatistics(
                            bitrate = "$bitrateKbps kbps",
                            fps = "$fps",
                            rtt = "$rttMs ms",
                            durationSeconds = seconds
                        )
                    )
                }
            }
        }
    }

    fun openOAuthConfig(platformId: String? = null) {
        _uiState.update {
            it.copy(
                isOAuthConfigOpen = true,
                selectedOAuthTab = platformId ?: it.selectedOAuthTab
            )
        }
    }

    fun closeOAuthConfig() {
        _uiState.update { it.copy(isOAuthConfigOpen = false) }
    }

    fun selectOAuthTab(platformId: String) {
        _uiState.update { it.copy(selectedOAuthTab = platformId) }
    }

    fun updateOAuthField(
        platformId: String,
        clientId: String? = null,
        clientSecret: String? = null,
        redirectUri: String? = null,
        streamKey: String? = null,
        rtmpIngestUrl: String? = null
    ) {
        val currentCredentials = _uiState.value.oauthCredentials.toMutableMap()
        val current = currentCredentials[platformId] ?: return
        currentCredentials[platformId] = current.copy(
            clientId = clientId ?: current.clientId,
            clientSecret = clientSecret ?: current.clientSecret,
            redirectUri = redirectUri ?: current.redirectUri,
            streamKey = streamKey ?: current.streamKey,
            rtmpIngestUrl = rtmpIngestUrl ?: current.rtmpIngestUrl
        )
        _uiState.update { it.copy(oauthCredentials = currentCredentials) }
    }

    fun oneClickLogin(platformId: String) {
        val currentCredentials = _uiState.value.oauthCredentials.toMutableMap()
        val current = currentCredentials[platformId] ?: return

        viewModelScope.launch {
            // Set connecting state
            currentCredentials[platformId] = current.copy(isConnecting = true)
            _uiState.update { it.copy(oauthCredentials = currentCredentials) }

            delay(1200) // Simulated secure OAuth 2.0 token handshake

            val (accountName, accountHandle, autoStreamKey, autoIngestUrl) = when (platformId.lowercase()) {
                "facebook" -> Quadruple(
                    "Ahmed Gaming (Facebook)",
                    "@ahmed.fb.live",
                    "live_fb_${Random.nextInt(100000, 999999)}_sec",
                    "rtmps://live-api-s.facebook.com:443/rtmp/"
                )
                "youtube" -> Quadruple(
                    "Ahmed Live Channel",
                    "@AhmedLiveYT",
                    "live_yt_${Random.nextInt(100000, 999999)}_sec",
                    "rtmp://a.rtmp.youtube.com/live2"
                )
                "twitch" -> Quadruple(
                    "ahmed_streamer",
                    "@ahmed_streamer",
                    "live_tw_${Random.nextInt(100000, 999999)}_sec",
                    "rtmp://live.twitch.tv/app/"
                )
                "tiktok" -> Quadruple(
                    "Ahmed TikTok Live",
                    "@ahmed_tiktok_live",
                    "live_tt_${Random.nextInt(100000, 999999)}_sec",
                    "rtmp://live.tiktok.com/live/"
                )
                else -> Quadruple(
                    "${current.displayName} User",
                    "@user_live",
                    "live_${platformId}_${Random.nextInt(100000, 999999)}",
                    current.rtmpIngestUrl
                )
            }

            val updatedCreds = _uiState.value.oauthCredentials.toMutableMap()
            updatedCreds[platformId] = current.copy(
                accountName = accountName,
                accountHandle = accountHandle,
                streamKey = autoStreamKey,
                rtmpIngestUrl = autoIngestUrl,
                isConnected = true,
                isConnecting = false
            )

            // Also ensure platform destination is enabled
            val capitalName = when (platformId.lowercase()) {
                "youtube" -> "YouTube"
                "twitch" -> "Twitch"
                "facebook" -> "Facebook"
                "tiktok" -> "TikTok"
                else -> current.displayName
            }
            val dests = _uiState.value.destinations.toMutableMap()
            dests[capitalName] = true

            _uiState.update {
                it.copy(
                    oauthCredentials = updatedCreds,
                    destinations = dests,
                    snackbarMessage = "تم تسجيل الدخول وربط حساب ${current.displayName} بنجاح! جاهز للبث."
                )
            }
        }
    }

    fun saveOAuthCredentials(platformId: String) {
        val currentCredentials = _uiState.value.oauthCredentials.toMutableMap()
        val current = currentCredentials[platformId] ?: return
        val isReady = current.clientId.isNotBlank() || current.streamKey.isNotBlank()
        currentCredentials[platformId] = current.copy(
            isConnected = isReady,
            accountName = if (isReady && current.accountName.isBlank()) "${current.displayName} Account" else current.accountName
        )
        _uiState.update {
            it.copy(
                oauthCredentials = currentCredentials,
                snackbarMessage = "تم حفظ إعدادات ${current.displayName} بنجاح"
            )
        }
    }

    fun disconnectOAuth(platformId: String) {
        val currentCredentials = _uiState.value.oauthCredentials.toMutableMap()
        val current = currentCredentials[platformId] ?: return
        currentCredentials[platformId] = current.copy(
            clientId = "",
            clientSecret = "",
            streamKey = "",
            accountName = "",
            accountHandle = "",
            isConnected = false,
            isConnecting = false
        )
        _uiState.update {
            it.copy(
                oauthCredentials = currentCredentials,
                snackbarMessage = "تم إلغاء ربط ${current.displayName}"
            )
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

