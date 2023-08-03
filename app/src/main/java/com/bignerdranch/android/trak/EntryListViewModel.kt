package com.bignerdranch.android.trak

import androidx.lifecycle.ViewModel

class EntryListViewModel : ViewModel() {
    private val entryRepository = EntryRepository.get()
    val entryListLiveData = entryRepository.getEntries()

    fun addEntry(entry: Entry) {
        entryRepository.addEntry(entry)
    }

    fun deleteEntry(entry: Entry) {
        entryRepository.deleteEntry(entry)
    }
}