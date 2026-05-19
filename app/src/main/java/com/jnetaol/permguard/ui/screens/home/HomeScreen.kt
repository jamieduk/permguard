package com.jnetaol.permguard.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.permguard.ui.components.*
import com.jnetaol.permguard.ui.screens.AppViewModel
import com.jnetaol.permguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToTimeline: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val appCount by viewModel.appCount.collectAsStateWithLifecycle()
    val logCount by viewModel.logCount.collectAsStateWithLifecycle()
    val highRiskProfiles by viewModel.highRiskProfiles.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val totalRisky by viewModel.totalRiskyPermissions.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    var showUpdateDialog by remember { mutableStateOf(false) }

    if (showUpdateDialog) {
        CheckUpdateDialog(onDismiss = { showUpdateDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = DarkPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "PermGuard",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = DarkPrimary
                            )
                            Text(
                                "Permission Monitor",
                                fontSize = 11.sp,
                                color = DarkOnSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanApps() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Scan",
                            tint = if (isScanning) DarkPrimary else DarkOnSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DarkOnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CrimsonBackground
                )
            )
        },
        containerColor = CrimsonBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats cards
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "Apps",
                        value = "$appCount",
                        icon = Icons.Default.Apps,
                        color = DarkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Logs",
                        value = "$logCount",
                        icon = Icons.Default.Timeline,
                        color = Color(0xFFFF9100),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Risky",
                        value = "$totalRisky",
                        icon = Icons.Default.Warning,
                        color = ScoreRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick actions
            item {
                SectionHeader(title = "Quick Actions")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlowButton(
                        text = "Timeline",
                        onClick = onNavigateToTimeline,
                        icon = Icons.Default.Timeline,
                        modifier = Modifier.weight(1f),
                        color = DarkPrimary
                    )
                    GlowButton(
                        text = "All Apps",
                        onClick = onNavigateToApps,
                        icon = Icons.Default.Apps,
                        modifier = Modifier.weight(1f),
                        color = DarkSecondary
                    )
                }
            }

            // Privacy alerts
            item {
                SectionHeader(
                    title = "Privacy Alerts",
                    action = {
                        if (highRiskProfiles.isNotEmpty()) {
                            TextButton(onClick = onNavigateToReport) {
                                Text(
                                    "Report",
                                    color = DarkPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                )

                if (highRiskProfiles.isEmpty()) {
                    EmptyState(
                        title = "All Clear",
                        message = "No high-risk apps detected. Your privacy looks good.",
                        icon = Icons.Default.CheckCircle
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        highRiskProfiles.take(3).forEach { profile ->
                            AppPermissionCard(
                                profile = profile,
                                onClick = { onAppClick(profile.packageName) }
                            )
                        }
                        if (highRiskProfiles.size > 3) {
                            TextButton(onClick = onNavigateToApps) {
                                Text(
                                    "View all ${highRiskProfiles.size} risky apps",
                                    color = ScoreRed,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Recent activity
            item {
                SectionHeader(
                    title = "Recent Activity",
                    action = {
                        TextButton(onClick = onNavigateToTimeline) {
                            Text("View All", color = DarkPrimary, fontSize = 13.sp)
                        }
                    }
                )

                if (recentLogs.isEmpty()) {
                    EmptyState(
                        title = "No Recent Activity",
                        message = "No permission access events in the last 24 hours",
                        icon = Icons.Default.Visibility
                    )
                } else {
                    PermissionTimeline(logs = recentLogs.take(5))
                }
            }

            // Report
            item {
                GlowButton(
                    text = "Generate Privacy Report",
                    onClick = onNavigateToReport,
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.fillMaxWidth(),
                    color = DarkTertiary
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CheckUpdateDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = DarkPrimary,
        textContentColor = DarkOnSurface,
        title = { Text("Check For Updates") },
        text = { Text("You are running the latest version of PermGuard (1.0.0).") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = DarkPrimary)
            }
        }
    )
}
