package com.bignerdranch.android.trak.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.trak.Entry

@Database(entities = [Entry::class], version = 2, exportSchema = false)
@TypeConverters(EntryTypeConverters::class)
abstract class EntryDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Entry ADD COLUMN trainer TEXT NOT NULL DEFAULT ''"
        )
    }
}