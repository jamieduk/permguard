package com.jnetaol.permguard.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.permguard.data.model.PermissionCategory
import com.jnetaol.permguard.ui.components.*
import com.jnetaol.permguard.ui.screens.AppViewModel
import com.jnetaol.permguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    viewModel: AppViewModel,
    packageName: String,
    onBack: () -> Unit
) {
    val profile by viewModel.selectedApp.collectAsStateWithLifecycle()
    val logs by viewModel.selectedAppLogs.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()

    LaunchedEffect(packageName) {
        val found = allProfiles.find { it.packageName == packageName }
        viewModel.selectApp(found)
    }

    val currentProfile = profile ?: return

    val categorizedPermissions = remember(currentProfile) {
        val result = mutableMapOf<PermissionCategory, List<String>>()
        for (perm in currentProfile.declaredPermissions) {
            val cat = PermissionCategory.classify(perm)
            result[cat] = (result[cat] ?: emptyList()) + perm
        }
        result.filter { it.value.isNotEmpty() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentProfile.appName,
                        fontWeight = FontWeight.Bold,
                        color = DarkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = DarkOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CrimsonBackground)
            )
        },
        containerColor = CrimsonBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with score
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PrivacyScoreBadge(
                            score = currentProfile.privacyScore,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Privacy Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkOnSurface
                        )

                        val scoreLabel = when {
                            currentProfile.privacyScore >= 75 -> "Good"
                            currentProfile.privacyScore >= 50 -> "Fair"
                            currentProfile.privacyScore >= 25 -> "Poor"
                            else -> "Critical"
                        }
                        Text(
                            text = scoreLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scoreColor(currentProfile.privacyScore),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = currentProfile.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkOutline,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            if (currentProfile.isSystemApp) {
                                StatusBadge(label = "System App", color = DarkOutline)
                            }
                            StatusBadge(
                                label = "${currentProfile.riskyPermissionCount} risky perms",
                                color = ScoreRed
                            )
                            StatusBadge(
                                label = "${currentProfile.totalPermissionCount} total perms",
                                color = DarkPrimary
                            )
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "Events",
                        value = "${logs.size}",
                        icon = Icons.Default.Timeline,
                        color = DarkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Risky",
                        value = "${currentProfile.riskyPermissionCount}",
                        icon = Icons.Default.Warning,
                        color = ScoreRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Total",
                        value = "${currentProfile.totalPermissionCount}",
                        icon = Icons.Default.Security,
                        color = DarkSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Declared permissions
            item {
                SectionHeader(title = "Declared Permissions")
            }

            if (categorizedPermissions.isEmpty()) {
                item {
                    Text(
                        "No permissions declared",
                        color = DarkOutline,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(categorizedPermissions.entries.toList()) { (category, permissions) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val catColor = when (category) {
                                PermissionCategory.MICROPHONE -> Color(0xFFFF1744)
                                PermissionCategory.CAMERA -> Color(0xFFD500F9)
                                PermissionCategory.LOCATION -> Color(0xFF00E5FF)
                                PermissionCategory.CONTACTS -> Color(0xFFFF9100)
                                PermissionCategory.STORAGE -> Color(0xFF76FF03)
                                PermissionCategory.PHONE -> Color(0xFF448AFF)
                                PermissionCategory.SENSORS -> Color(0xFF69F0AE)
                                else -> DarkOutline
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (category) {
                                        PermissionCategory.MICROPHONE -> Icons.Default.Mic
                                        PermissionCategory.CAMERA -> Icons.Default.CameraAlt
                                        PermissionCategory.LOCATION -> Icons.Default.LocationOn
                                        PermissionCategory.CONTACTS -> Icons.Default.Contacts
                                        PermissionCategory.STORAGE -> Icons.Default.Storage
                                        PermissionCategory.PHONE -> Icons.Default.Phone
                                        PermissionCategory.SENSORS -> Icons.Default.Sensors
                                        else -> Icons.Default.Security
                                    },
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = catColor
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "${permissions.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkOutline
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            permissions.forEach { perm ->
                                Text(
                                    text = "  \u2022 ${perm.substringAfterLast(".")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkOnSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Access history
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(title = "Access History")
            }

            if (logs.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Access History",
                        message = "No permission access events recorded for this app",
                        icon = Icons.Default.HistoryToggleOff
                    )
                }
            } else {
                items(logs) { log ->
                    PermissionTimelineItem(log)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PermissionTimelineItem(log: com.jnetaol.permguard.data.model.PermissionLog) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.permission.substringAfterLast("."),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnSurface
                )
                Text(
                    text = java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.US)
                        .format(java.util.Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkOutline
                )
            }

            StatusBadge(label = log.accessType, color = color)
        }
    }
}
