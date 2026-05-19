package com.jnetaol.permguard

import android.app.Application
import com.jnetaol.permguard.data.db.AppDatabase
import com.jnetaol.permguard.logger.DebugLogger

class PermGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugLogger.init(this)
        DebugLogger.i("PG-001", "PermGuard application started")

        // Warm up the database
        AppDatabase.getInstance(this)
        DebugLogger.i("PG-002", "Database initialized")
    }
}
