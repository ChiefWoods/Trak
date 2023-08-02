package com.bignerdranch.android.trak

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Entry(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    var title: String = "",
    var rested: Boolean = false,
    var weight: Double = 0.0,
    var gym: String = "",
    var date: Date = Date(),
    var time: Date = Date(),
    var trainer: String = ""
)
