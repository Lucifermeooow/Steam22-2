package com.ahmed.streamgit101.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmed.streamgit101.data.PlatformOAuthCredential
import com.ahmed.streamgit101.ui.theme.DarkSurface
import com.ahmed.streamgit101.ui.theme.DarkSurfaceBorder
import com.ahmed.streamgit101.ui.theme.StreamRed

@Composable
fun DestinationGridCard(
    name: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isLive: Boolean,
    credential: PlatformOAuthCredential? = null,
    onToggle: (Boolean) -> Unit,
    onConfigure: (() -> Unit)? = null,
    onOneClickLogin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val brandColor = when (name.lowercase()) {
        "youtube" -> Color(0xFFFF0000)
        "twitch" -> Color(0xFF9146FF)
        "facebook" -> Color(0xFF1877F2)
        "tiktok" -> Color(0xFF00F2FE)
        else -> StreamRed
    }

    val isConnected = credential?.isConnected == true
    val isConnecting = credential?.isConnecting == true

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isEnabled) brandColor.copy(alpha = 0.12f) else DarkSurface)
            .border(
                width = if (isEnabled) 1.5.dp else 1.dp,
                color = if (isEnabled) brandColor.copy(alpha = 0.55f) else DarkSurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isLive) {
                if (!isConnected && onConfigure != null) {
                    onConfigure()
                } else {
                    onToggle(!isEnabled)
                }
            }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(brandColor.copy(alpha = if (isEnabled) 0.95f else 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onConfigure != null) {
                        IconButton(
                            onClick = onConfigure,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "إعدادات $name",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = if (isLive) null else onToggle,
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = brandColor,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color(0xFF0F1318)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isConnected) Color(0xFF133824) else Color(0xFF231B1B))
                        .border(0.8.dp, if (isConnected) Color(0xFF00E676).copy(alpha = 0.6f) else Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isConnected) "مربوط ✓" else "غير متصل",
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) Color(0xFF00E676) else Color(0xFFFF8A80)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isConnecting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = brandColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "جاري تسجيل الدخول...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    )
                }
            } else if (isConnected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = credential?.accountName?.ifBlank { "جاهز للبث" } ?: "جاهز للبث",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(brandColor.copy(alpha = 0.2f))
                        .clickable {
                            if (onOneClickLogin != null) {
                                onOneClickLogin()
                            } else if (onConfigure != null) {
                                onConfigure()
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تسجيل دخول سريع",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DestinationTile(
    name: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isLive: Boolean,
    credential: PlatformOAuthCredential? = null,
    onToggle: (Boolean) -> Unit,
    onConfigure: (() -> Unit)? = null,
    onOneClickLogin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    DestinationGridCard(
        name = name,
        icon = icon,
        isEnabled = isEnabled,
        isLive = isLive,
        credential = credential,
        onToggle = onToggle,
        onConfigure = onConfigure,
        onOneClickLogin = onOneClickLogin,
        modifier = modifier
    )
}


