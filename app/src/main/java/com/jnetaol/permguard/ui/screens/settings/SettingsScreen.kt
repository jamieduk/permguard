package com.jnetaol.permguard.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.permguard.logger.DebugLogger
import com.jnetaol.permguard.ui.components.*
import com.jnetaol.permguard.ui.screens.AppViewModel
import com.jnetaol.permguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Logs", color = DarkPrimary) },
            text = { Text("Are you sure you want to clear all permission logs? This action cannot be undone.") },
            containerColor = DarkSurface,
            titleContentColor = DarkPrimary,
            textContentColor = DarkOnSurface,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLogs()
                    Toast.makeText(context, "Logs cleared", Toast.LENGTH_SHORT).show()
                    showClearDialog = false
                }) {
                    Text("Clear", color = ScoreRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = DarkOutline)
                }
            }
        )
    }

    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("Debug Log", color = DarkPrimary) },
            text = {
                Text(
                    DebugLogger.getLogContent().takeLast(2000),
                    color = DarkOnSurface,
                    fontSize = 11.sp
                )
            },
            containerColor = DarkSurface,
            titleContentColor = DarkPrimary,
            textContentColor = DarkOnSurface,
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = {
                        DebugLogger.clear()
                        Toast.makeText(context, "Log cleared", Toast.LENGTH_SHORT).show()
                        showLogDialog = false
                    }) {
                        Text("Clear", color = ScoreRed)
                    }
                    TextButton(onClick = { showLogDialog = false }) {
                        Text("Close", color = DarkPrimary)
                    }
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Data", color = DarkPrimary) },
            text = { Text("This will delete all profiles and logs. Run a new scan to rebuild. Continue?") },
            containerColor = DarkSurface,
            titleContentColor = DarkPrimary,
            textContentColor = DarkOnSurface,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLogs()
                    viewModel.scanApps()
                    Toast.makeText(context, "Data reset, rescanning...", Toast.LENGTH_SHORT).show()
                    showResetDialog = false
                }) {
                    Text("Reset", color = ScoreRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = DarkOutline)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About PermGuard", color = DarkPrimary) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Shield,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = DarkPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "PermGuard v1.0.0",
                        fontWeight = FontWeight.Bold,
                        color = DarkPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Permission Activity Monitor",
                        color = DarkOnSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Monitors which apps access sensitive permissions like microphone, camera, and location in real-time.",
                        color = DarkOnSurface,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jnetaol.com"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Link, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Made By jnetaol.com", color = DarkPrimary)
                    }
                }
            },
            containerColor = DarkSurface,
            titleContentColor = DarkPrimary,
            textContentColor = DarkOnSurface,
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = DarkPrimary)
                }
            }
        )
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Check For Updates", color = DarkPrimary) },
            text = { Text("You are running the latest version (1.0.0).") },
            containerColor = DarkSurface,
            titleContentColor = DarkPrimary,
            textContentColor = DarkOnSurface,
            confirmButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("OK", color = DarkPrimary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        color = DarkPrimary
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // General section
            item {
                SectionHeader(title = "General")
            }

            item {
                SettingsItem(
                    title = "Rescan All Apps",
                    subtitle = "Refresh all app permission profiles",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        viewModel.scanApps()
                        Toast.makeText(context, "Scanning...", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Data management
            item {
                SectionHeader(title = "Data Management")
            }

            item {
                SettingsItem(
                    title = "Clear Permission Logs",
                    subtitle = "Remove all recorded permission access events",
                    icon = Icons.Default.DeleteSweep,
                    onClick = { showClearDialog = true }
                )
            }

            item {
                SettingsItem(
                    title = "Reset All Data",
                    subtitle = "Delete all data and run a fresh scan",
                    icon = Icons.Default.RestartAlt,
                    onClick = { showResetDialog = true }
                )
            }

            item {
                SectionHeader(title = "Debug")
            }

            item {
                SettingsItem(
                    title = "View Debug Log",
                    subtitle = "Inspect internal logs for troubleshooting",
                    icon = Icons.Default.Terminal,
                    onClick = { showLogDialog = true }
                )
            }

            // App info
            item {
                SectionHeader(title = "App Info")
            }

            item {
                SettingsItem(
                    title = "Version",
                    subtitle = "1.0.0",
                    icon = Icons.Default.Info
                )
            }

            item {
                SettingsItem(
                    title = "Check For Updates",
                    subtitle = "Verify you have the latest version",
                    icon = Icons.Default.SystemUpdateAlt,
                    onClick = { showUpdateDialog = true }
                )
            }

            item {
                SettingsItem(
                    title = "Share PermGuard",
                    subtitle = "Share with friends and family",
                    icon = Icons.Default.Share,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "PermGuard")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out PermGuard - monitor app permissions on your device!\nhttps://jnetaol.com"
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    }
                )
            }

            item {
                SettingsItem(
                    title = "About",
                    subtitle = "Made By jnetaol.com",
                    icon = Icons.Default.Android,
                    onClick = { showAboutDialog = true }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = DarkPrimary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnSurface
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnSurfaceVariant
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = DarkOutline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
