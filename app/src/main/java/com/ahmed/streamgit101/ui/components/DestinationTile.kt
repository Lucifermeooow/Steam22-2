package com.ahmed.streamgit101.ui.components

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
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmed.streamgit101.ui.theme.DarkSurface
import com.ahmed.streamgit101.ui.theme.DarkSurfaceBorder
import com.ahmed.streamgit101.ui.theme.StreamRed

@Composable
fun DestinationGridCard(
    name: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isLive: Boolean,
    onToggle: (Boolean) -> Unit,
    onConfigure: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val brandColor = when (name.lowercase()) {
        "youtube" -> Color(0xFFFF0000)
        "twitch" -> Color(0xFF9146FF)
        "facebook" -> Color(0xFF1877F2)
        "tiktok" -> Color(0xFF00F2FE)
        else -> StreamRed
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isEnabled) brandColor.copy(alpha = 0.12f) else DarkSurface)
            .border(
                width = if (isEnabled) 1.5.dp else 1.dp,
                color = if (isEnabled) brandColor.copy(alpha = 0.55f) else DarkSurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isLive) { onToggle(!isEnabled) }
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
                                tint = Color.White.copy(alpha = 0.6f),
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isEnabled) Color(0xFF133824) else Color(0xFF1E242C))
                        .border(0.8.dp, if (isEnabled) Color.Green.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isEnabled) "نشط" else "معطّل",
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) Color(0xFF00E676) else Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = if (isEnabled) (if (isLive) "🔴 بث نشط" else "جاهز للبث التلقائي") else "معطّل حالياً",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = if (isEnabled) (if (isLive) Color(0xFFFF5252) else Color.White.copy(alpha = 0.75f)) else Color.White.copy(alpha = 0.38f)
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DestinationTile(
    name: String,
    icon: ImageVector,
    isEnabled: Boolean,
    isLive: Boolean,
    onToggle: (Boolean) -> Unit,
    onConfigure: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    DestinationGridCard(
        name = name,
        icon = icon,
        isEnabled = isEnabled,
        isLive = isLive,
        onToggle = onToggle,
        onConfigure = onConfigure,
        modifier = modifier
    )
}


