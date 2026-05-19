package com.jnetaol.permguard.ui.screens.report

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.permguard.data.model.PrivacyReport
import com.jnetaol.permguard.ui.components.*
import com.jnetaol.permguard.ui.screens.AppViewModel
import com.jnetaol.permguard.ui.theme.*
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val permissionUsageCounts by viewModel.permissionUsageCounts.collectAsStateWithLifecycle()

    val report = remember(allProfiles, allLogs, permissionUsageCounts) {
        viewModel.generateReport()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy Report",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Summary header
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
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = DarkPrimary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Privacy Audit Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.US).format(Date(report.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkOutline
                        )
                    }
                }
            }

            // Stats grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "Apps",
                        value = "${report.totalApps}",
                        icon = Icons.Default.Apps,
                        color = DarkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Avg Score",
                        value = "${report.averageScore}",
                        icon = Icons.Default.Score,
                        color = scoreColor(report.averageScore),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Events",
                        value = "${report.totalPermissionAccesses}",
                        icon = Icons.Default.Timeline,
                        color = Color(0xFFFF9100),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Critical alerts
            item {
                SectionHeader(title = "Critical Alerts")
                if (report.criticalAlerts.isEmpty()) {
                    EmptyState(
                        title = "No Critical Issues",
                        message = "Your device's permission landscape looks clean",
                        icon = Icons.Default.CheckCircle
                    )
                } else {
                    report.criticalAlerts.forEach { alert ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ScoreRed.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    null,
                                    tint = ScoreRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = alert,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ScoreRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Top violated permissions
            item {
                SectionHeader(title = "Top Violated Permissions")
                if (report.topViolatedPermissions.isEmpty()) {
                    EmptyState(
                        title = "No Data",
                        message = "No permission usage data available yet",
                        icon = Icons.Default.DataUsage
                    )
                } else {
                    report.topViolatedPermissions.forEachIndexed { index, (permission, count) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "#${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = DarkPrimary,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = permission.substringAfterLast("."),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkOnSurface
                                    )
                                    Text(
                                        "$count access events",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkOnSurfaceVariant
                                    )
                                }
                                StatusBadge(label = "$count", color = DarkPrimary)
                            }
                        }
                    }
                }
            }

            // High risk apps
            item {
                SectionHeader(title = "High Risk Apps")
                if (report.highRiskApps.isEmpty()) {
                    EmptyState(
                        title = "No High Risk Apps",
                        message = "All apps have acceptable privacy scores",
                        icon = Icons.Default.VerifiedUser
                    )
                } else {
                    report.highRiskApps.forEach { profile ->
                        AppPermissionCard(
                            profile = profile,
                            onClick = { onAppClick(profile.packageName) }
                        )
                    }
                }
            }

            // Export button
            item {
                Spacer(Modifier.height(8.dp))
                GlowButton(
                    text = "Export Report",
                    onClick = { exportReport(context, report) },
                    icon = Icons.Default.FileDownload,
                    modifier = Modifier.fillMaxWidth(),
                    color = DarkPrimary
                )
            }

            item {
                TextButton(
                    onClick = {
                        val shareText = buildReportText(report)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "PermGuard Privacy Report")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Report"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Share,
                        null,
                        tint = DarkPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Share Report", color = DarkPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

private fun buildReportText(report: PrivacyReport): String {
    val sb = StringBuilder()
    sb.appendLine("╔══════════════════════════════════╗")
    sb.appendLine("║     PermGuard Privacy Report     ║")
    sb.appendLine("╚══════════════════════════════════╝")
    sb.appendLine()
    sb.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(report.timestamp))}")
    sb.appendLine("Total Apps: ${report.totalApps}")
    sb.appendLine("Average Privacy Score: ${report.averageScore}/100")
    sb.appendLine("Total Events: ${report.totalPermissionAccesses}")
    sb.appendLine()

    if (report.criticalAlerts.isNotEmpty()) {
        sb.appendLine("--- CRITICAL ALERTS ---")
        report.criticalAlerts.forEach { sb.appendLine("  \u26A0 $it") }
        sb.appendLine()
    }

    if (report.topViolatedPermissions.isNotEmpty()) {
        sb.appendLine("--- TOP VIOLATED PERMISSIONS ---")
        report.topViolatedPermissions.forEach { (perm, count) ->
            sb.appendLine("  $perm: $count")
        }
        sb.appendLine()
    }

    if (report.highRiskApps.isNotEmpty()) {
        sb.appendLine("--- HIGH RISK APPS ---")
        report.highRiskApps.forEach { app ->
            sb.appendLine("  ${app.appName} (${app.packageName}) - Score: ${app.privacyScore}")
        }
    }

    sb.appendLine()
    sb.appendLine("Report generated by PermGuard - Made By jnetaol.com")
    return sb.toString()
}

private fun exportReport(context: Context, report: PrivacyReport) {
    try {
        val file = File(context.getExternalFilesDir(null), "permguard_report_${System.currentTimeMillis()}.txt")
        FileWriter(file).use { it.write(buildReportText(report)) }
        Toast.makeText(context, "Report saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
