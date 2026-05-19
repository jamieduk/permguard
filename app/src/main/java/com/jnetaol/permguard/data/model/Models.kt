package com.jnetaol.permguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "app_profiles",
    indices = [
        Index(value = ["packageName"], unique = true),
        Index(value = ["privacyScore"])
    ]
)
data class AppProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val declaredPermissions: List<String> = emptyList(),
    val riskyPermissionCount: Int = 0,
    val totalPermissionCount: Int = 0,
    val privacyScore: Int = 100,
    val lastScanTimestamp: Long = System.currentTimeMillis(),
    val iconBase64: String = "",
    val isSystemApp: Boolean = false
)

@Entity(
    tableName = "permission_logs",
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["permission"]),
        Index(value = ["timestamp"])
    ]
)
data class PermissionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val permission: String,
    val accessType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val foreground: Boolean = true,
    val duration: Long = 0
)

enum class AccessType(val label: String) {
    GRANTED("Granted"),
    ACCESSED("Accessed"),
    REVOKED("Revoked"),
    DENIED("Denied"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String): AccessType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class PermissionCategory(
    val label: String,
    val color: String,
    val permissions: List<String>
) {
    MICROPHONE("Microphone", "#FF1744", listOf("RECORD_AUDIO", "CAPTURE_AUDIO_OUTPUT")),
    CAMERA("Camera", "#D500F9", listOf("CAMERA")),
    LOCATION("Location", "#00E5FF", listOf(
        "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
        "ACCESS_BACKGROUND_LOCATION", "ACCESS_MEDIA_LOCATION"
    )),
    CONTACTS("Contacts", "#FF9100", listOf(
        "READ_CONTACTS", "WRITE_CONTACTS", "GET_ACCOUNTS"
    )),
    STORAGE("Storage", "#76FF03", listOf(
        "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
        "MANAGE_EXTERNAL_STORAGE", "READ_MEDIA_IMAGES",
        "READ_MEDIA_VIDEO", "READ_MEDIA_AUDIO"
    )),
    PHONE("Phone", "#448AFF", listOf(
        "READ_PHONE_STATE", "CALL_PHONE", "READ_CALL_LOG",
        "WRITE_CALL_LOG", "PROCESS_OUTGOING_CALLS", "READ_SMS",
        "SEND_SMS", "RECEIVE_SMS"
    )),
    SENSORS("Sensors", "#69F0AE", listOf("BODY_SENSORS", "ACTIVITY_RECOGNITION")),
    OTHER("Other", "#9E9E9E", emptyList());

    companion object {
        fun classify(permission: String): PermissionCategory {
            val shortName = permission.substringAfterLast("android.permission.", permission)
            return entries.firstOrNull { cat ->
                cat.permissions.any { it.equals(shortName, ignoreCase = true) }
            } ?: OTHER
        }
    }
}

data class PrivacyReport(
    val timestamp: Long = System.currentTimeMillis(),
    val totalApps: Int = 0,
    val averageScore: Int = 0,
    val highRiskApps: List<AppProfile> = emptyList(),
    val totalPermissionAccesses: Int = 0,
    val topViolatedPermissions: List<Pair<String, Int>> = emptyList(),
    val criticalAlerts: List<String> = emptyList()
)
