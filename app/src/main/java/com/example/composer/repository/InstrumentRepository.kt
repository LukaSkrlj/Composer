package com.example.composer.repository

import androidx.lifecycle.LiveData
import com.example.composer.data.InstrumentDao
import com.example.composer.models.Instrument
import com.example.composer.models.InstrumentWithMeasures

class InstrumentRepository(private val instrumentDao: InstrumentDao) {
    val instrumentsWithMeasures: LiveData<List<InstrumentWithMeasures>> =
        instrumentDao.getInstrumentsWithMeasures()

    suspend fun upsertInstrument(instrument: Instrument) {
        instrumentDao.upsertInstrument(instrument)
    }
}