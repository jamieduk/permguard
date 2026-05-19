package com.jnetaol.permguard.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jnetaol.permguard.data.db.AppDatabase
import com.jnetaol.permguard.data.model.*
import com.jnetaol.permguard.engine.PermissionScanner
import com.jnetaol.permguard.logger.DebugLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val scanner = PermissionScanner(application)

    val allProfiles: StateFlow<List<AppProfile>> = db.appProfileDao()
        .getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val highRiskProfiles: StateFlow<List<AppProfile>> = db.appProfileDao()
        .getHighRiskProfiles(50)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allLogs: StateFlow<List<PermissionLog>> = db.permissionLogDao()
        .getAllLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentLogs: StateFlow<List<PermissionLog>> = db.permissionLogDao()
        .getRecentLogs(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appCount: StateFlow<Int> = db.appProfileDao()
        .count()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val logCount: StateFlow<Int> = db.permissionLogDao()
        .count()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalRiskyPermissions: StateFlow<Int> = db.appProfileDao()
        .totalRiskyPermissions()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val permissionUsageCounts: StateFlow<List<PermissionUsageCount>> =
        db.permissionLogDao()
            .permissionUsageCounts()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<AppProfile>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) db.appProfileDao().getAllProfiles()
            else db.appProfileDao().searchProfiles(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedPermissionCategory = MutableStateFlow<PermissionCategory?>(null)
    val selectedPermissionCategory: StateFlow<PermissionCategory?> = _selectedPermissionCategory.asStateFlow()

    val filteredLogs: StateFlow<List<PermissionLog>> = combine(allLogs, _selectedPermissionCategory) { logs, category ->
        if (category == null) logs
        else logs.filter { log ->
            PermissionCategory.classify(log.permission) == category
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedApp = MutableStateFlow<AppProfile?>(null)
    val selectedApp: StateFlow<AppProfile?> = _selectedApp.asStateFlow()

    val selectedAppLogs: StateFlow<List<PermissionLog>> = _selectedApp
        .flatMapLatest { app ->
            if (app != null) db.permissionLogDao().getLogsForPackage(app.packageName)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        viewModelScope.launch {
            checkAndRunInitialScan()
        }
    }

    private suspend fun checkAndRunInitialScan() {
        val count = db.appProfileDao().getAllProfilesList().size
        if (count == 0) {
            DebugLogger.i("PG-300", "No profiles found, running initial scan")
            scanApps()
        }
    }

    fun scanApps() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                scanner.scanAllApps()
                DebugLogger.i("PG-301", "App scan completed successfully")
            } catch (e: Exception) {
                DebugLogger.e("PG-302", "App scan failed: ${e.message}")
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedPermissionCategory(category: PermissionCategory?) {
        _selectedPermissionCategory.value = category
    }

    fun selectApp(profile: AppProfile?) {
        _selectedApp.value = profile
    }

    fun generateReport(): PrivacyReport {
        val apps = allProfiles.value
        val logs = allLogs.value
        val highRisk = apps.filter { it.privacyScore < 50 }.take(10)
        val avgScore = if (apps.isNotEmpty()) apps.sumOf { it.privacyScore } / apps.size else 0

        val topViolated = permissionUsageCounts.value
            .sortedByDescending { it.cnt }
            .take(5)
            .map { it.permission to it.cnt }

        val criticalAlerts = mutableListOf<String>()
        if (apps.any { it.riskyPermissionCount > 10 }) {
            criticalAlerts.add("Apps with excessive dangerous permissions detected")
        }
        if (highRisk.size > 5) {
            criticalAlerts.add("Multiple high-risk apps found (${highRisk.size})")
        }
        if (logs.isNotEmpty()) {
            val recentAccess = logs.count { it.timestamp > System.currentTimeMillis() - 3600_000 }
            if (recentAccess > 50) {
                criticalAlerts.add("High permission access activity in last hour: $recentAccess events")
            }
        }

        return PrivacyReport(
            totalApps = apps.size,
            averageScore = avgScore,
            highRiskApps = highRisk,
            totalPermissionAccesses = logs.size,
            topViolatedPermissions = topViolated,
            criticalAlerts = criticalAlerts
        )
    }

    fun getAppProfile(packageName: String): AppProfile? {
        return allProfiles.value.find { it.packageName == packageName }
    }

    fun clearLogs() {
        viewModelScope.launch {
            db.permissionLogDao().deleteAll()
            DebugLogger.i("PG-303", "All logs cleared")
        }
    }

    fun scanSingleApp(packageName: String) {
        viewModelScope.launch {
            try {
                val profile = scanner.scanApp(packageName)
                if (profile != null) {
                    db.appProfileDao().insert(profile)
                    DebugLogger.i("PG-304", "Rescanned $packageName")
                }
            } catch (e: Exception) {
                DebugLogger.e("PG-305", "Rescan failed $packageName: ${e.message}")
            }
        }
    }
}
