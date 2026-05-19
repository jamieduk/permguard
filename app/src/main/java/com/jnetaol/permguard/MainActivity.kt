package com.jnetaol.permguard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jnetaol.permguard.engine.PermissionMonitor
import com.jnetaol.permguard.ui.screens.AppViewModel
import com.jnetaol.permguard.ui.screens.apps.AppsScreen
import com.jnetaol.permguard.ui.screens.detail.AppDetailScreen
import com.jnetaol.permguard.ui.screens.home.HomeScreen
import com.jnetaol.permguard.ui.screens.report.ReportScreen
import com.jnetaol.permguard.ui.screens.settings.SettingsScreen
import com.jnetaol.permguard.ui.screens.timeline.TimelineScreen
import com.jnetaol.permguard.ui.theme.PermGuardTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()
    private val REQUEST_USAGE_STATS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestUsageStatsPermission()
        startMonitoringService()

        setContent {
            PermGuardTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToTimeline = { navController.navigate("timeline") },
                            onNavigateToApps = { navController.navigate("apps") },
                            onNavigateToReport = { navController.navigate("report") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onAppClick = { pkg -> navController.navigate("app/$pkg") }
                        )
                    }

                    composable("timeline") {
                        TimelineScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onAppClick = { pkg -> navController.navigate("app/$pkg") }
                        )
                    }

                    composable("apps") {
                        AppsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onAppClick = { pkg -> navController.navigate("app/$pkg") }
                        )
                    }

                    composable(
                        "app/{packageName}",
                        arguments = listOf(navArgument("packageName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                        AppDetailScreen(
                            viewModel = viewModel,
                            packageName = packageName,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("report") {
                        ReportScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onAppClick = { pkg -> navController.navigate("app/$pkg") }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_USAGE_STATS) {
            if (hasUsageStatsPermission()) {
                Toast.makeText(this, "Usage access granted. Starting monitoring...", Toast.LENGTH_SHORT).show()
                startMonitoringService()
            } else {
                Toast.makeText(this, "Usage access is needed for permission monitoring", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            Toast.makeText(
                this,
                "PermGuard needs Usage Access permission to monitor app activity",
                Toast.LENGTH_LONG
            ).show()
            startActivityForResult(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                REQUEST_USAGE_STATS
            )
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val granted = try {
            val appOps = getSystemService(APP_OPS_SERVICE) as? android.app.AppOpsManager
            if (appOps != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val mode = appOps.unsafeCheckOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    packageName
                )
                mode == android.app.AppOpsManager.MODE_ALLOWED
            } else {
                @Suppress("DEPRECATION")
                appOps?.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    packageName
                ) == android.app.AppOpsManager.MODE_ALLOWED
            }
        } catch (_: Exception) {
            false
        }
        return granted
    }

    private fun startMonitoringService() {
        val intent = Intent(this, PermissionMonitor::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            @Suppress("DEPRECATION")
            startService(intent)
        }
    }
}
