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

    @Query("SELECT MAX(position) FROM Instrument WHERE composition_id = :compositionId")
    fun getMaxPosition(compositionId: Int): Int

    @Query("DELETE FROM Instrument")
    fun deleteInstruments()

    @Transaction()
    @Insert()
    fun insertInstrument(instrument: Instrument): Long
}
