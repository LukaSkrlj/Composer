package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.composer.models.Measure
import com.example.composer.models.MeasureWithNotes
import com.example.composer.models.Note

@Dao
interface MeasureDao {
    @Upsert
    suspend fun upsertMeasure(measure: Measure)

    @Delete
    suspend fun deleteMeasure(measure: Measure)

    @Query("SELECT * FROM Measure")
    fun getMeasures(): LiveData<List<Measure>>


    @Query(
        "SELECT * FROM Measure JOIN Note ON Measure.id = Note.measure_id"
    )
    fun loadMeasureMap(): Map<Measure, List<Note>>

    @Query("SELECT * FROM Measure")
    fun test(): LiveData<List<MeasureWithNotes>>

    @Query("DELETE FROM Measure")
    fun deleteMeasures(): Void

}