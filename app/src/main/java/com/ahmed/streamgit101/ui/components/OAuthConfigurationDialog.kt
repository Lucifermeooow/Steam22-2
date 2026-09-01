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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
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
    onOneClickLogin: (platformId: String) -> Unit,
    onUpdateField: (platformId: String, clientId: String?, clientSecret: String?, redirectUri: String?, streamKey: String?, rtmpIngestUrl: String?) -> Unit,
    onSave: (platformId: String) -> Unit,
    onDisconnect: (platformId: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val tabs = listOf(
        Triple("facebook", "Facebook", Icons.Default.VideoLibrary),
        Triple("youtube", "YouTube", Icons.Default.PlayCircle),
        Triple("twitch", "Twitch", Icons.Default.Tv),
        Triple("tiktok", "TikTok", Icons.Default.Tv)
    )

    val currentCred = credentials[selectedTab] ?: PlatformOAuthCredential(selectedTab, selectedTab)
    var showSecret by remember(selectedTab) { mutableStateOf(false) }
    var showStreamKey by remember(selectedTab) { mutableStateOf(false) }
    var isAdvancedExpanded by remember(selectedTab) { mutableStateOf(false) }

    val (brandColor, brandGradient, loginButtonText) = when (selectedTab.lowercase()) {
        "facebook" -> Triple(
            Color(0xFF1877F2),
            Brush.verticalGradient(listOf(Color(0xFF1877F2), Color(0xFF0D47A1))),
            "تسجيل الدخول بحساب Facebook"
        )
        "youtube" -> Triple(
            Color(0xFFFF0000),
            Brush.verticalGradient(listOf(Color(0xFFFF0000), Color(0xFFB71C1C))),
            "تسجيل الدخول بحساب Google / YouTube"
        )
        "twitch" -> Triple(
            Color(0xFF9146FF),
            Brush.verticalGradient(listOf(Color(0xFF9146FF), Color(0xFF4A148C))),
            "تسجيل الدخول بحساب Twitch"
        )
        "tiktok" -> Triple(
            Color(0xFF00F2FE),
            Brush.verticalGradient(listOf(Color(0xFF00C853), Color(0xFF004D40))),
            "تسجيل الدخول بحساب TikTok"
        )
        else -> Triple(
            StreamRed,
            Brush.verticalGradient(listOf(StreamRed, Color(0xFF880E4F))),
            "تسجيل الدخول وربط الحساب"
        )
    }

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
                            color = brandColor
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
                                        tint = if (selectedIndex == index) brandColor else TextSecondary
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
                                                .background(Color(0xFF00E676))
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scroll Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero 1-Click Login / Connected Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentCred.isConnected) Color(0xFF10281D) else DarkSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.2.dp,
                            color = if (currentCred.isConnected) Color(0xFF00E676).copy(alpha = 0.5f) else brandColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(if (currentCred.isConnected) Color(0xFF00E676).copy(alpha = 0.2f) else brandColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentCred.isConnected) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (currentCred.isConnected) Color(0xFF00E676) else brandColor,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentCred.displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (currentCred.isConnected) {
                                Text(
                                    text = "الحساب: ${currentCred.accountName.ifBlank { "حساب نشط" }}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (currentCred.accountHandle.isNotBlank()) {
                                    Text(
                                        text = currentCred.accountHandle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0A331E))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "✓ تم الربط وتوليد مفتاح البث تلقائياً",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF81C784),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    text = "غير متصل — اضغط على الزر أدناه لتسجيل الدخول السريع",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1-Click Login Primary Button
                            if (!currentCred.isConnected) {
                                Button(
                                    onClick = { onOneClickLogin(selectedTab) },
                                    enabled = !currentCred.isConnecting,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("one_click_login_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = brandColor,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (currentCred.isConnecting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("جاري المصادقة وربط الحساب...", fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = loginButtonText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

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
                                            Toast.makeText(context, "تعذر فتح المتصفح: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("official_browser_login_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("فتح صفحة تسجيل الدخول الرسمية بالمتصفح", fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onDisconnect(selectedTab) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("disconnect_account_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تسجيل الخروج وفك ربط الحساب", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Collapsible Advanced Settings (Optional for power users)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isAdvancedExpanded = !isAdvancedExpanded }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.advanced_manual_settings),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            )
                        }
                        Icon(
                            imageVector = if (isAdvancedExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = isAdvancedExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .padding(14.dp)
                        ) {
                            OutlinedTextField(
                                value = currentCred.clientId,
                                onValueChange = { onUpdateField(selectedTab, it, null, null, null, null) },
                                label = { Text(stringResource(R.string.client_id_label)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("oauth_client_id_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = brandColor,
                                    unfocusedBorderColor = DarkSurfaceBorder
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = currentCred.clientSecret,
                                onValueChange = { onUpdateField(selectedTab, null, it, null, null, null) },
                                label = { Text(stringResource(R.string.client_secret_label)) },
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
                                    focusedBorderColor = brandColor,
                                    unfocusedBorderColor = DarkSurfaceBorder
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = currentCred.streamKey,
                                onValueChange = { onUpdateField(selectedTab, null, null, null, it, null) },
                                label = { Text(stringResource(R.string.stream_key_label)) },
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
                                    focusedBorderColor = brandColor,
                                    unfocusedBorderColor = DarkSurfaceBorder
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onSave(selectedTab) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_manual_settings_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.save_credentials), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
