package com.example.composer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.composer.data.ComposerDatabase
import com.example.composer.models.Instrument
import com.example.composer.models.InstrumentWithMeasures
import com.example.composer.repository.InstrumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InstrumentViewModel(application: Application) : AndroidViewModel(application) {
    val instrumentsWithMeasures: LiveData<List<InstrumentWithMeasures>>
    private val repository: InstrumentRepository

    init {
        val compositionDao = ComposerDatabase.getDatabase(application).instrumentDao()
        repository = InstrumentRepository(compositionDao)
        instrumentsWithMeasures = repository.instrumentsWithMeasures
    }

    fun upsertInstrument(instrument: Instrument) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertInstrument(instrument)
        }
    }
}