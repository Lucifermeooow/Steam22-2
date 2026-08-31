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

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
