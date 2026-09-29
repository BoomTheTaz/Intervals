package com.example.intervals.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PresetEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class IntervalsDatabase : RoomDatabase()
{
    abstract fun presetDao(): PresetDao

    companion object
    {
        private val MIGRATION_1_2 = object : Migration(1, 2)
        {
            override fun migrate(db: SupportSQLiteDatabase)
            {
                db.execSQL("ALTER TABLE presets ADD COLUMN lastUsedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE presets SET lastUsedAt = ${System.currentTimeMillis()}")
            }
        }

        @Volatile
        private var instance: IntervalsDatabase? = null

        fun getInstance(context: Context): IntervalsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IntervalsDatabase::class.java,
                    "intervals.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
