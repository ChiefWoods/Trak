package com.bignerdranch.android.trak

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Entry(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = ""
)
