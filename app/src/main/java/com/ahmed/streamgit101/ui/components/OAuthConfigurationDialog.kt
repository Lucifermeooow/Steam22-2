package com.ahmed.streamgit101.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ahmed.streamgit101.R
import com.ahmed.streamgit101.data.PlatformOAuthCredential
import com.ahmed.streamgit101.ui.theme.DarkBackground
import com.ahmed.streamgit101.ui.theme.DarkSurface
import com.ahmed.streamgit101.ui.theme.DarkSurfaceBorder
import com.ahmed.streamgit101.ui.theme.StreamRed
import com.ahmed.streamgit101.ui.theme.StreamRedContainer
import com.ahmed.streamgit101.ui.theme.StreamRedLight
import com.ahmed.streamgit101.ui.theme.TextMuted
import com.ahmed.streamgit101.ui.theme.TextSecondary

@Composable
fun OAuthConfigurationDialog(
    isOpen: Boolean,
    selectedTab: String,
    credentials: Map<String, PlatformOAuthCredential>,
    backendUrl: String,
    onTabSelected: (String) -> Unit,
    onUpdateField: (platformId: String, clientId: String?, clientSecret: String?, redirectUri: String?, streamKey: String?, rtmpIngestUrl: String?) -> Unit,
    onSave: (platformId: String) -> Unit,
    onDisconnect: (platformId: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val tabs = listOf(
        Triple("youtube", "YouTube", Icons.Default.PlayCircle),
        Triple("twitch", "Twitch", Icons.Default.Tv),
        Triple("facebook", "Facebook", Icons.Default.VideoLibrary),
        Triple("tiktok", "TikTok", Icons.Default.Tv)
    )

    val currentCred = credentials[selectedTab] ?: PlatformOAuthCredential(selectedTab, selectedTab)
    var showSecret by remember(selectedTab) { mutableStateOf(false) }
    var showStreamKey by remember(selectedTab) { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.oauth_config_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = stringResource(R.string.oauth_config_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_oauth_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Platform Navigation Tabs
                val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
                TabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = DarkSurface,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            color = StreamRed
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("oauth_platform_tabs")
                ) {
                    tabs.forEachIndexed { index, (id, label, icon) ->
                        val isConfigured = credentials[id]?.isConnected == true
                        Tab(
                            selected = selectedIndex == index,
                            onClick = { onTabSelected(id) },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedIndex == index) StreamRedLight else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    if (isConfigured) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4CAF50))
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Form Scroll Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Status Badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentCred.isConnected) Color(0xFF1B382B) else DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentCred.isConnected) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (currentCred.isConnected) Color(0xFF4CAF50) else StreamRedLight,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentCred.isConnected) stringResource(R.string.status_connected) else stringResource(R.string.status_not_configured),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "المنصة: ${currentCred.displayName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Client ID Field
                    OutlinedTextField(
                        value = currentCred.clientId,
                        onValueChange = { onUpdateField(selectedTab, it, null, null, null, null) },
                        label = { Text(stringResource(R.string.client_id_label)) },
                        placeholder = { Text("أدخل Client ID الخاص بـ ${currentCred.displayName}") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = TextSecondary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("oauth_client_id_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StreamRed,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Client Secret Field
                    OutlinedTextField(
                        value = currentCred.clientSecret,
                        onValueChange = { onUpdateField(selectedTab, null, it, null, null, null) },
                        label = { Text(stringResource(R.string.client_secret_label)) },
                        placeholder = { Text("أدخل Client Secret") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showSecret = !showSecret }) {
                                Icon(
                                    imageVector = if (showSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("oauth_client_secret_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StreamRed,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Redirect URI
                    OutlinedTextField(
                        value = currentCred.redirectUri,
                        onValueChange = { onUpdateField(selectedTab, null, null, it, null, null) },
                        label = { Text(stringResource(R.string.redirect_uri_label)) },
                        placeholder = { Text("https://your-server.example.com/auth/callback") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = TextSecondary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("oauth_redirect_uri_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StreamRed,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stream Key Field
                    OutlinedTextField(
                        value = currentCred.streamKey,
                        onValueChange = { onUpdateField(selectedTab, null, null, null, it, null) },
                        label = { Text(stringResource(R.string.stream_key_label)) },
                        placeholder = { Text("live_xxx... أو Stream Key الخاص بك") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showStreamKey = !showStreamKey }) {
                                Icon(
                                    imageVector = if (showStreamKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showStreamKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("oauth_stream_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StreamRed,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // RTMP Ingest Server URL
                    OutlinedTextField(
                        value = currentCred.rtmpIngestUrl,
                        onValueChange = { onUpdateField(selectedTab, null, null, null, null, it) },
                        label = { Text(stringResource(R.string.ingest_url_label)) },
                        placeholder = { Text("rtmp://...") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = TextSecondary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("oauth_ingest_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StreamRed,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scopes info
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "OAuth Scopes المطلوبة:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            )
                            Text(
                                text = currentCred.scopes,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Action Buttons at bottom
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onSave(selectedTab) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_oauth_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StreamRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.save_credentials),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val backend = backendUrl.trim().removeSuffix("/")
                                val oauthUrl = if (currentCred.clientId.isNotBlank() && currentCred.authEndpoint.isNotBlank()) {
                                    "${currentCred.authEndpoint}?client_id=${Uri.encode(currentCred.clientId)}&redirect_uri=${Uri.encode(currentCred.redirectUri)}&response_type=code&scope=${Uri.encode(currentCred.scopes)}"
                                } else {
                                    "$backend/auth/$selectedTab/start"
                                }
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح متصفح المصادقة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("connect_oauth_browser_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ربط عبر المتصفح", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { onDisconnect(selectedTab) },
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("disconnect_oauth_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مسح", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
