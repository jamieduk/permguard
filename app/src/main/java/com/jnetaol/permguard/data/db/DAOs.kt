package com.jnetaol.permguard.data.db

import androidx.room.*
import com.jnetaol.permguard.data.model.AppProfile
import com.jnetaol.permguard.data.model.PermissionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AppProfileDao {
    @Query("SELECT * FROM app_profiles ORDER BY privacyScore ASC")
    fun getAllProfiles(): Flow<List<AppProfile>>

    @Query("SELECT * FROM app_profiles ORDER BY privacyScore ASC")
    suspend fun getAllProfilesList(): List<AppProfile>

    @Query("SELECT * FROM app_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): AppProfile?

    @Query("SELECT * FROM app_profiles WHERE packageName LIKE '%' || :query || '%' OR appName LIKE '%' || :query || '%' ORDER BY privacyScore ASC")
    fun searchProfiles(query: String): Flow<List<AppProfile>>

    @Query("SELECT * FROM app_profiles WHERE privacyScore < :threshold ORDER BY privacyScore ASC")
    fun getHighRiskProfiles(threshold: Int = 50): Flow<List<AppProfile>>

    @Query("SELECT COUNT(*) FROM app_profiles")
    fun count(): Flow<Int>

    @Query("SELECT AVG(privacyScore) FROM app_profiles")
    fun averageScore(): Flow<Double>

    @Query("SELECT SUM(riskyPermissionCount) FROM app_profiles")
    fun totalRiskyPermissions(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: AppProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<AppProfile>)

    @Update
    suspend fun update(profile: AppProfile)

    @Delete
    suspend fun delete(profile: AppProfile)

    @Query("DELETE FROM app_profiles WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("DELETE FROM app_profiles")
    suspend fun deleteAll()
}

@Dao
interface PermissionLogDao {
    @Query("SELECT * FROM permission_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PermissionLog>>

    @Query("SELECT * FROM permission_logs ORDER BY timestamp DESC")
    suspend fun getAllLogsList(): List<PermissionLog>

    @Query("SELECT * FROM permission_logs WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getLogsForPackage(packageName: String): Flow<List<PermissionLog>>

    @Query("SELECT * FROM permission_logs WHERE packageName = :packageName ORDER BY timestamp DESC")
    suspend fun getLogsForPackageList(packageName: String): List<PermissionLog>

    @Query("SELECT * FROM permission_logs WHERE permission LIKE '%' || :permission || '%' ORDER BY timestamp DESC")
    fun getLogsByPermission(permission: String): Flow<List<PermissionLog>>

    @Query("SELECT * FROM permission_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsInRange(startTime: Long, endTime: Long): Flow<List<PermissionLog>>

    @Query("SELECT * FROM permission_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getLogsInRangeList(startTime: Long, endTime: Long): List<PermissionLog>

    @Query("SELECT COUNT(*) FROM permission_logs WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun countLogsInRange(startTime: Long, endTime: Long): Int

    @Query("SELECT DISTINCT permission FROM permission_logs ORDER BY permission ASC")
    fun getDistinctPermissions(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM permission_logs")
    fun count(): Flow<Int>

    @Query("SELECT permission, COUNT(*) as cnt FROM permission_logs GROUP BY permission ORDER BY cnt DESC")
    fun permissionUsageCounts(): Flow<List<PermissionUsageCount>>

    @Query("SELECT * FROM permission_logs WHERE timestamp > :since ORDER BY timestamp DESC")
    fun getRecentLogs(since: Long): Flow<List<PermissionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: PermissionLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<PermissionLog>)

    @Query("DELETE FROM permission_logs")
    suspend fun deleteAll()

    @Query("DELETE FROM permission_logs WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM permission_logs WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}

data class PermissionUsageCount(
    val permission: String,
    val cnt: Int
)

@Dao
interface AggregateDao {
    @Query("SELECT app_profiles.* FROM app_profiles INNER JOIN (SELECT packageName, COUNT(*) as logCount FROM permission_logs GROUP BY packageName ORDER BY logCount DESC) logs ON app_profiles.packageName = logs.packageName")
    fun getMostActiveProfiles(): Flow<List<AppProfile>>

    @Query("SELECT COUNT(DISTINCT packageName) FROM permission_logs WHERE timestamp > :since")
    suspend fun activeAppCount(since: Long): Int

    @Query("SELECT COUNT(*) FROM permission_logs WHERE accessType = 'DENIED' AND timestamp > :since")
    suspend fun deniedAccessCount(since: Long): Int
}
