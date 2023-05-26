package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.composer.models.Measure

@Dao
interface MeasureDao {
    @Upsert
    suspend fun upsertMeasure(measure: Measure)

    @Delete
    suspend fun deleteMeasure(measure: Measure)

    @Query("SELECT * FROM Measure")
    fun getMeasures(): LiveData<List<Measure>>
}