package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.composer.models.Instrument
import com.example.composer.models.InstrumentWithMeasures

@Dao
interface InstrumentDao {
    @Upsert()
    suspend fun upsertInstrument(instrument: Instrument)

    @Transaction
    @Query("SELECT * FROM Instrument")
    fun getInstrumentsWithMeasures(): LiveData<List<InstrumentWithMeasures>>

    @Query("DELETE FROM Instrument")
    fun deleteInstruments()
}
