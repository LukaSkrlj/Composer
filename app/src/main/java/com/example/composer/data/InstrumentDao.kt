package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.composer.models.Instrument
import com.example.composer.models.InstrumentWithMeasures

@Dao
interface InstrumentDao {
    @Upsert()
    suspend fun upsertInstrument(instrument: Instrument)

    @Transaction
    @Query("SELECT * FROM Instrument")
    fun getInstrumentsWithMeasures(): LiveData<List<InstrumentWithMeasures>>
}
