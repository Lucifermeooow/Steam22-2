package com.ahmed.streamgit101.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import com.ahmed.streamgit101.R
import com.ahmed.streamgit101.ui.components.CameraPreviewView
import com.ahmed.streamgit101.ui.components.DestinationTile
import com.ahmed.streamgit101.ui.components.OAuthConfigurationDialog
import com.ahmed.streamgit101.ui.components.StreamStatsRow
import com.ahmed.streamgit101.ui.theme.DarkBackground
import com.ahmed.streamgit101.ui.theme.DarkSurface
import com.ahmed.streamgit101.ui.theme.DarkSurfaceBorder
import com.ahmed.streamgit101.ui.theme.StreamRed
import com.ahmed.streamgit101.ui.theme.StreamRedContainer
import com.ahmed.streamgit101.ui.theme.StreamRedLight
import com.ahmed.streamgit101.ui.theme.TextMuted
import com.ahmed.streamgit101.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScreen(
    viewModel: StreamViewModel,
    onRequestPermissions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val connectProvider: (String) -> Unit = { provider ->
        val backend = uiState.backendUrl.trim().removeSuffix("/")
        if (backend.isEmpty() || !backend.startsWith("https://")) {
            Toast.makeText(context, "استخدم رابط Backend يبدأ بـ HTTPS.", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$backend/auth/$provider/start"))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر فتح تسجيل الدخول: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_stream_22),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openOAuthConfig() },
                        modifier = Modifier.testTag("open_oauth_settings_appbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "إعدادات OAuth وحسابات البث",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.prepareCamera() },
                        enabled = !uiState.isBusy && !uiState.isLive,
                        modifier = Modifier.testTag("refresh_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة تجهيز الكاميرا",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Camera Preview View
            CameraPreviewView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .testTag("camera_preview_view"),
                isInitialized = uiState.isInitialized,
                isLive = uiState.isLive,
                cameraLens = uiState.currentLens,
                hasCameraPermission = uiState.hasCameraPermission,
                onRequestPermissions = onRequestPermissions
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status message badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (uiState.isLive) StreamRedContainer else DarkSurface)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isLive) StreamRedLight else Color.White
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Live telemetry stats
            AnimatedVisibility(
                visible = uiState.isLive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    StreamStatsRow(stats = uiState.stats)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RTMP URL Input Field
            OutlinedTextField(
                value = uiState.rtmpUrl,
                onValueChange = { viewModel.onRtmpUrlChanged(it) },
                enabled = !uiState.isLive,
                label = { Text(stringResource(R.string.server_rtmp_label)) },
                placeholder = { Text(stringResource(R.string.server_rtmp_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rtmp_url_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StreamRed,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.server_relay_info),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // OAuth Backend URL Input Field
            OutlinedTextField(
                value = uiState.backendUrl,
                onValueChange = { viewModel.onBackendUrlChanged(it) },
                enabled = !uiState.isLive,
                label = { Text(stringResource(R.string.oauth_backend_label)) },
                placeholder = { Text(stringResource(R.string.oauth_backend_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backend_url_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StreamRed,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dedicated OAuth Configuration Card Button
            Card(
                onClick = { viewModel.openOAuthConfig() },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_oauth_config_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = StreamRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.oauth_config_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "إدخال وحفظ بيانات الاعتماد (YouTube, Twitch, Facebook, TikTok)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.openOAuthConfig() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StreamRedLight)
                    ) {
                        Text("تهيئة")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // OAuth Provider Buttons (YouTube, Twitch, Facebook, TikTok)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.openOAuthConfig("youtube") },
                    enabled = !uiState.isLive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("oauth_youtube_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("YouTube", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.openOAuthConfig("twitch") },
                    enabled = !uiState.isLive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("oauth_twitch_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Twitch", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.openOAuthConfig("facebook") },
                    enabled = !uiState.isLive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("oauth_facebook_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Facebook", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.openOAuthConfig("tiktok") },
                    enabled = !uiState.isLive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("oauth_tiktok_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("TikTok", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Platforms Header
            Text(
                text = stringResource(R.string.broadcast_platforms),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            DestinationTile(
                name = "YouTube",
                icon = Icons.Default.PlayCircle,
                isEnabled = uiState.destinations["YouTube"] ?: false,
                isLive = uiState.isLive,
                onToggle = { viewModel.toggleDestination("YouTube", it) },
                modifier = Modifier.testTag("destination_youtube")
            )

            DestinationTile(
                name = "Twitch",
                icon = Icons.Default.Tv,
                isEnabled = uiState.destinations["Twitch"] ?: false,
                isLive = uiState.isLive,
                onToggle = { viewModel.toggleDestination("Twitch", it) },
                modifier = Modifier.testTag("destination_twitch")
            )

            DestinationTile(
                name = "Facebook",
                icon = Icons.Default.VideoLibrary,
                isEnabled = uiState.destinations["Facebook"] ?: false,
                isLive = uiState.isLive,
                onToggle = { viewModel.toggleDestination("Facebook", it) },
                modifier = Modifier.testTag("destination_facebook")
            )

            DestinationTile(
                name = "TikTok",
                icon = Icons.Default.Tv,
                isEnabled = uiState.destinations["TikTok"] ?: false,
                isLive = uiState.isLive,
                onToggle = { viewModel.toggleDestination("TikTok", it) },
                modifier = Modifier.testTag("destination_tiktok")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Camera Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.prepareCamera() },
                    enabled = !uiState.isBusy && !uiState.isLive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prepare_camera_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.reinit_camera))
                }

                OutlinedButton(
                    onClick = { viewModel.switchCamera() },
                    enabled = !uiState.isBusy && !uiState.isLive,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("switch_camera_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.FlipCameraAndroid, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.switch_camera))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mute / Unmute Button
            OutlinedButton(
                onClick = { viewModel.toggleMute() },
                enabled = uiState.isInitialized && !uiState.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_mute_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (uiState.isMuted) StreamRed else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isMuted) {
                        stringResource(R.string.unmute_mic)
                    } else {
                        stringResource(R.string.mute_mic)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live action button (GO LIVE / STOP LIVE)
            Button(
                onClick = {
                    if (uiState.isLive) {
                        viewModel.stopStreaming()
                    } else {
                        viewModel.startStreaming()
                    }
                },
                enabled = !uiState.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("go_live_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isLive) DarkSurfaceBorder else StreamRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isLive) Icons.Default.Stop else Icons.Default.LiveTv,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isLive) {
                        stringResource(R.string.stop_live)
                    } else {
                        stringResource(R.string.go_live)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Text(
                text = stringResource(R.string.stream_footer),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    OAuthConfigurationDialog(
        isOpen = uiState.isOAuthConfigOpen,
        selectedTab = uiState.selectedOAuthTab,
        credentials = uiState.oauthCredentials,
        backendUrl = uiState.backendUrl,
        onTabSelected = { viewModel.selectOAuthTab(it) },
        onUpdateField = { platformId, clientId, clientSecret, redirectUri, streamKey, rtmpIngestUrl ->
            viewModel.updateOAuthField(platformId, clientId, clientSecret, redirectUri, streamKey, rtmpIngestUrl)
        },
        onSave = { viewModel.saveOAuthCredentials(it) },
        onDisconnect = { viewModel.disconnectOAuth(it) },
        onDismiss = { viewModel.closeOAuthConfig() }
    )
}

