package com.jnetaol.permguard.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.permguard.data.model.AppProfile
import com.jnetaol.permguard.data.model.PermissionCategory
import com.jnetaol.permguard.data.model.PermissionLog
import com.jnetaol.permguard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    color: Color = DarkPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = DarkOnPrimary,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = DarkOnPrimary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .border(1.dp, DarkOutline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkPrimary
        )
        action?.invoke()
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Security
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = DarkPrimary.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = DarkOnSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatusBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun PrivacyScoreBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = scoreColor(score)
    val bgColor = scoreBackground(score)

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.dp, color, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                lineHeight = 18.sp
            )
            Text(
                text = "/100",
                fontSize = 8.sp,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PermissionTimeline(
    logs: List<PermissionLog>,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) {
        EmptyState(
            title = "No Permission Logs",
            message = "No permission access events recorded yet",
            icon = Icons.Default.Timeline
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(logs) { log ->
            TimelineItem(log)
        }
    }
}

@Composable
private fun TimelineItem(log: PermissionLog) {
    val category = PermissionCategory.classify(log.permission)
    val color = when (category) {
        PermissionCategory.MICROPHONE -> Color(0xFFFF1744)
        PermissionCategory.CAMERA -> Color(0xFFD500F9)
        PermissionCategory.LOCATION -> Color(0xFF00E5FF)
        PermissionCategory.CONTACTS -> Color(0xFFFF9100)
        PermissionCategory.STORAGE -> Color(0xFF76FF03)
        PermissionCategory.PHONE -> Color(0xFF448AFF)
        PermissionCategory.SENSORS -> Color(0xFF69F0AE)
        else -> DarkOutline
    }

    val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.accessType) {
                        "GRANTED" -> Icons.Default.CheckCircle
                        "ACCESSED" -> Icons.Default.Visibility
                        "REVOKED" -> Icons.Default.RemoveCircle
                        "DENIED" -> Icons.Default.Block
                        else -> Icons.Default.HelpOutline
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = log.permission.substringAfterLast("."),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkOutline
                )
            }

            StatusBadge(
                label = log.accessType,
                color = when (log.accessType) {
                    "GRANTED" -> ScoreGreen
                    "ACCESSED" -> color
                    "REVOKED" -> ScoreYellow
                    "DENIED" -> ScoreRed
                    else -> DarkOutline
                }
            )
        }
    }
}

@Composable
fun AppPermissionCard(
    profile: AppProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showScore: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = DarkPrimary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = profile.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        label = "${profile.riskyPermissionCount} risky",
                        color = ScoreRed
                    )
                    if (profile.isSystemApp) {
                        Spacer(Modifier.width(6.dp))
                        StatusBadge(label = "System", color = DarkOutline)
                    }
                }
            }

            if (showScore) {
                PrivacyScoreBadge(score = profile.privacyScore)
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Shield,
    color: Color = DarkPrimary,
    subtitle: String = ""
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = color
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkOutline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PermissionFilterBar(
    selected: PermissionCategory?,
    onCategorySelected: (PermissionCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        null to "All",
        PermissionCategory.MICROPHONE to "Mic",
        PermissionCategory.CAMERA to "Camera",
        PermissionCategory.LOCATION to "Location",
        PermissionCategory.CONTACTS to "Contacts",
        PermissionCategory.STORAGE to "Storage",
        PermissionCategory.PHONE to "Phone",
        PermissionCategory.SENSORS to "Sensors"
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (cat, label) ->
                    val isSelected = selected == cat
                    val chipColor = if (isSelected) {
                        when (cat) {
                            PermissionCategory.MICROPHONE -> Color(0xFFFF1744)
                            PermissionCategory.CAMERA -> Color(0xFFD500F9)
                            PermissionCategory.LOCATION -> Color(0xFF00E5FF)
                            PermissionCategory.CONTACTS -> Color(0xFFFF9100)
                            PermissionCategory.STORAGE -> Color(0xFF76FF03)
                            PermissionCategory.PHONE -> Color(0xFF448AFF)
                            PermissionCategory.SENSORS -> Color(0xFF69F0AE)
                            else -> DarkPrimary
                        }
                    } else DarkOutline

                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(cat) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.3f),
                            selectedLabelColor = chipColor
                        )
                    )
                }
            }
        }
    }
}
