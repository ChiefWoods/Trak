package com.bignerdranch.android.trak

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.trak.database.EntryDao
import com.bignerdranch.android.trak.database.EntryDatabase
import com.bignerdranch.android.trak.database.migration_1_2
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "entry-database"

class EntryRepository private constructor(context: Context) {
    private val database: EntryDatabase =
        Room.databaseBuilder(context.applicationContext, EntryDatabase::class.java, DATABASE_NAME)
            .addMigrations(migration_1_2)
            .build()
    private val entryDao: EntryDao = database.entryDao()

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun getEntries(): LiveData<List<Entry>> = entryDao.getEntries()

    fun getEntry(id: UUID): LiveData<Entry?> = entryDao.getEntry(id)

    fun updateEntry(entry: Entry) {
        executor.execute {
            entryDao.updateEntry(entry)
        }
    }

    fun addEntry(entry: Entry) {
        executor.execute {
            entryDao.addEntry(entry)
        }
    }

    fun deleteEntry(entry: Entry) {
        executor.execute {
            entryDao.deleteEntry(entry)
        }
    }

    companion object {
        private var INSTANCE: EntryRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = EntryRepository(context)
            }
        }

        fun get(): EntryRepository {
            return INSTANCE ?: throw IllegalStateException("EntryRepository must be initialized")
        }
    }
}
