package com.jnetaol.permguard.logger

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    private const val TAG = "PermGuard"
    private const val FILE_NAME = "permguard_debug.log"
    private var logFile: File? = null
    private var enabled = false

    companion object {
        @JvmStatic
        fun init(context: Context) {
            logFile = File(context.getExternalFilesDir(null), FILE_NAME)
            enabled = true
            log("PG-000", "DebugLogger initialized", LogLevel.INFO)
        }

        @JvmStatic
        fun log(code: String, message: String, level: LogLevel = LogLevel.INFO) {
            if (!enabled) return
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
            val entry = "[$timestamp] [$level] [$code] $message\n"

            when (level) {
                LogLevel.DEBUG -> Log.d(TAG, "[$code] $message")
                LogLevel.INFO -> Log.i(TAG, "[$code] $message")
                LogLevel.WARN -> Log.w(TAG, "[$code] $message")
                LogLevel.ERROR -> Log.e(TAG, "[$code] $message")
            }

            try {
                logFile?.let { FileWriter(it, true).use { fw -> fw.append(entry) } }
            } catch (_: Exception) {}
        }

        @JvmStatic
        fun d(code: String, message: String) = log(code, message, LogLevel.DEBUG)

        @JvmStatic
        fun i(code: String, message: String) = log(code, message, LogLevel.INFO)

        @JvmStatic
        fun w(code: String, message: String) = log(code, message, LogLevel.WARN)

        @JvmStatic
        fun e(code: String, message: String) = log(code, message, LogLevel.ERROR)

        @JvmStatic
        fun getLogContent(): String {
            return try {
                logFile?.readText() ?: "No log file"
            } catch (e: Exception) {
                "Error reading log: ${e.message}"
            }
        }

        @JvmStatic
        fun clear() {
            try {
                logFile?.writeText("")
            } catch (_: Exception) {}
        }
    }
}

enum class LogLevel { DEBUG, INFO, WARN, ERROR }
