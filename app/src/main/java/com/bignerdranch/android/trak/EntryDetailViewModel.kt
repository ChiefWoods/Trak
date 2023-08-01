package com.bignerdranch.android.trak

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class EntryDetailViewModel : ViewModel() {
    private val entryRepository: EntryRepository = EntryRepository.get()
    private val entryIdLiveData = MutableLiveData<UUID>()

    var entryLiveData: LiveData<Entry?> = Transformations.switchMap(entryIdLiveData) {
        entryRepository.getEntry(it)
    }

    fun loadEntry(entryId: UUID) {
        entryIdLiveData.value = entryId
    }

    fun saveEntry(entry: Entry) {
        entryRepository.updateEntry(entry)
    }
}