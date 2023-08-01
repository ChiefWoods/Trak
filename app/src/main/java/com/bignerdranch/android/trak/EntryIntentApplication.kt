package com.bignerdranch.android.trak

import android.app.Application

class EntryIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EntryRepository.initialize(this)
    }
}