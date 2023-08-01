package com.bignerdranch.android.trak.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.trak.Entry
import java.util.*

@Dao
interface EntryDao {
    @Query("SELECT * FROM entry")
    fun getEntries(): LiveData<List<Entry>>

    @Query("SELECT * FROM entry WHERE id=(:id)")
    fun getEntry(id: UUID): LiveData<Entry?>

    @Update
    fun updateEntry(entry: Entry)

    @Insert
    fun addEntry(entry: Entry)
}