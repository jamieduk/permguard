package com.jnetaol.permguard.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.jnetaol.permguard.data.db.AppDatabase
import com.jnetaol.permguard.data.model.AppProfile
import com.jnetaol.permguard.data.model.PermissionCategory
import com.jnetaol.permguard.logger.DebugLogger

class PermissionScanner(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    companion object {
        val SENSITIVE_PERMISSIONS = setOf(
            "RECORD_AUDIO", "CAMERA", "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
            "ACCESS_BACKGROUND_LOCATION", "READ_CONTACTS", "WRITE_CONTACTS", "GET_ACCOUNTS",
            "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE", "MANAGE_EXTERNAL_STORAGE",
            "READ_PHONE_STATE", "CALL_PHONE", "READ_CALL_LOG", "WRITE_CALL_LOG",
            "PROCESS_OUTGOING_CALLS", "READ_SMS", "SEND_SMS", "RECEIVE_SMS",
            "BODY_SENSORS", "ACTIVITY_RECOGNITION", "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO", "READ_MEDIA_AUDIO", "ACCESS_MEDIA_LOCATION",
            "CAPTURE_AUDIO_OUTPUT", "USE_BIOMETRIC", "USE_FINGERPRINT"
        )

        fun calculatePrivacyScore(
            riskyPermissionCount: Int,
            totalPermissionCount: Int,
            isSystemApp: Boolean
        ): Int {
            if (totalPermissionCount == 0) return 100
            if (isSystemApp) return 90 - (riskyPermissionCount * 5).coerceAtMost(40)

            var score = 100
            score -= riskyPermissionCount * 7
            val extraFactor = (totalPermissionCount - riskyPermissionCount).coerceAtLeast(0)
            score -= extraFactor * 2
            return score.coerceIn(0, 100)
        }
    }

    suspend fun scanAllApps(): List<AppProfile> {
        DebugLogger.i("PG-200", "Starting full app scan")
        val db = AppDatabase.getInstance(context)
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val profiles = mutableListOf<AppProfile>()

        for (appInfo in packages) {
            try {
                val profile = scanApp(appInfo.packageName, appInfo)
                if (profile != null) {
                    profiles.add(profile)
                }
            } catch (e: Exception) {
                DebugLogger.w("PG-201", "Failed to scan ${appInfo.packageName}: ${e.message}")
            }
        }

        db.appProfileDao().deleteAll()
        db.appProfileDao().insertAll(profiles)
        DebugLogger.i("PG-202", "Scan complete: ${profiles.size} apps scanned")
        return profiles
    }

    fun scanApp(packageName: String, appInfo: ApplicationInfo? = null): AppProfile? {
        return try {
            val info = appInfo ?: pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val appName = pm.getApplicationLabel(info).toString()
            val declaredPerms = pkgInfo.requestedPermissions?.toList() ?: emptyList()
            val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            val riskyPerms = declaredPerms.count { perm ->
                val short = perm.substringAfterLast("android.permission.", perm)
                SENSITIVE_PERMISSIONS.any { it.equals(short, ignoreCase = true) }
            }

            val score = calculatePrivacyScore(riskyPerms, declaredPerms.size, isSystem)

            AppProfile(
                appName = appName,
                packageName = packageName,
                declaredPermissions = declaredPerms,
                riskyPermissionCount = riskyPerms,
                totalPermissionCount = declaredPerms.size,
                privacyScore = score,
                lastScanTimestamp = System.currentTimeMillis(),
                isSystemApp = isSystem
            )
        } catch (e: Exception) {
            DebugLogger.e("PG-203", "App scan error $packageName: ${e.message}")
            null
        }
    }

    fun getDeclaredPermissions(packageName: String): List<String> {
        return try {
            val pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            pkgInfo.requestedPermissions?.toList() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun categorizePermissions(permissions: List<String>): Map<PermissionCategory, List<String>> {
        val result = mutableMapOf<PermissionCategory, List<String>>()
        for (perm in permissions) {
            val cat = PermissionCategory.classify(perm)
            result[cat] = (result[cat] ?: emptyList()) + perm
        }
        return result.filter { it.value.isNotEmpty() }
    }
}
