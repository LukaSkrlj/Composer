package com.example.composer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.composer.data.ComposerDatabase
import com.example.composer.models.Instrument
import com.example.composer.models.InstrumentWithMeasures
import com.example.composer.repository.InstrumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class InstrumentViewModel(application: Application) : AndroidViewModel(application) {
    val instrumentsWithMeasures: LiveData<List<InstrumentWithMeasures>>
    private val repository: InstrumentRepository

    init {
        val compositionDao = ComposerDatabase.getDatabase(application).instrumentDao()
        repository = InstrumentRepository(compositionDao)
        instrumentsWithMeasures = repository.instrumentsWithMeasures
    }

    fun deleteInstruments() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteInstruments()
        }
    }

    fun upsertInstrument(instrument: Instrument) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertInstrument(instrument)
        }
    }

    fun insertInstrument(instrument: Instrument): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            val instrumentId = async { repository.insertInstrument(instrument) }
            result.postValue(instrumentId.await())
        }
        return result
    }

}