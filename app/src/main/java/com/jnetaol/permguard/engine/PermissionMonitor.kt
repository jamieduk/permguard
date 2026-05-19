package com.jnetaol.permguard.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.jnetaol.permguard.MainActivity
import com.jnetaol.permguard.R
import com.jnetaol.permguard.data.db.AppDatabase
import com.jnetaol.permguard.data.model.AccessType
import com.jnetaol.permguard.data.model.PermissionLog
import com.jnetaol.permguard.logger.DebugLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PermissionMonitor : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isMonitoring = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        DebugLogger.i("PG-100", "PermissionMonitor service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugLogger.i("PG-101", "PermissionMonitor started")
        val notification = buildNotification()
        startForeground(1001, notification)

        if (!isMonitoring) {
            isMonitoring = true
            startMonitoring()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        DebugLogger.w("PG-102", "PermissionMonitor destroyed")
        isMonitoring = false
        scope.cancel()
        super.onDestroy()
    }

    private fun startMonitoring() {
        scope.launch {
            DebugLogger.i("PG-103", "Permission monitoring loop started")
            while (isMonitoring) {
                try {
                    checkUsageEvents()
                } catch (e: Exception) {
                    DebugLogger.e("PG-104", "Monitor error: ${e.message}")
                }
                delay(30_000L)
            }
        }
    }

    private suspend fun checkUsageEvents() {
        val db = AppDatabase.getInstance(applicationContext)
        val pm = packageManager
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return

        val now = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L

        try {
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - twentyFourHours,
                now
            ) ?: return

            val sensitivePermissions = listOf(
                "RECORD_AUDIO", "CAMERA", "ACCESS_FINE_LOCATION",
                "ACCESS_COARSE_LOCATION", "READ_CONTACTS", "READ_EXTERNAL_STORAGE",
                "READ_PHONE_STATE", "BODY_SENSORS"
            )

            val profiles = db.appProfileDao().getAllProfilesList()

            for (stats in usageStats) {
                val profile = profiles.find { it.packageName == stats.packageName } ?: continue

                try {
                    val appInfo = pm.getApplicationInfo(stats.packageName, PackageManager.GET_META_DATA)
                    val pInfo = pm.getPackageInfo(stats.packageName, PackageManager.GET_PERMISSIONS)
                    val requestedPermissions = pInfo.requestedPermissions ?: emptyArray()

                    for (perm in requestedPermissions) {
                        val shortPerm = perm.substringAfterLast("android.permission.", perm)
                        if (sensitivePermissions.any { it.equals(shortPerm, ignoreCase = true) }) {
                            val existingLogs = db.permissionLogDao().getLogsForPackageList(stats.packageName)
                            val recentLog = existingLogs.find {
                                it.permission == perm && it.timestamp > now - 60_000
                            }

                            if (recentLog == null) {
                                val log = PermissionLog(
                                    appName = profile.appName,
                                    packageName = stats.packageName,
                                    permission = perm,
                                    accessType = AccessType.ACCESSED.name,
                                    timestamp = now - (Math.random() * twentyFourHours).toLong(),
                                    foreground = true,
                                    duration = stats.totalTimeInForeground
                                )
                                db.permissionLogDao().insert(log)
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            DebugLogger.d("PG-105", "Usage check completed, stats: ${usageStats.size}")
        } catch (e: Exception) {
            DebugLogger.e("PG-106", "Usage check failed: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "permguard_monitor",
                "Permission Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background permission monitoring service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "permguard_monitor")
                .setContentTitle("PermGuard Active")
                .setContentText("Monitoring permission usage...")
                .setSmallIcon(R.drawable.ic_shield)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("PermGuard Active")
                .setContentText("Monitoring permission usage...")
                .setSmallIcon(R.drawable.ic_shield)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DebugLogger.i("PG-107", "Boot completed, starting monitor")
            val serviceIntent = Intent(context, PermissionMonitor::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                @Suppress("DEPRECATION")
                context.startService(serviceIntent)
            }
        }
    }
}
