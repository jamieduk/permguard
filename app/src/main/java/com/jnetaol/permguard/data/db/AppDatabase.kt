package com.jnetaol.permguard.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jnetaol.permguard.data.model.AppProfile
import com.jnetaol.permguard.data.model.PermissionLog

@Database(
    entities = [AppProfile::class, PermissionLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appProfileDao(): AppProfileDao
    abstract fun permissionLogDao(): PermissionLogDao
    abstract fun aggregateDao(): AggregateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "permguard.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
