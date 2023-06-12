package com.example.composer.repository

import androidx.lifecycle.LiveData
import com.example.composer.data.MeasureDao
import com.example.composer.models.Measure
import com.example.composer.models.MeasureWithNotes

class MeasureRepository(private val measureDao: MeasureDao) {
    val measures: LiveData<List<Measure>> = measureDao.getMeasures()
    val measuresWithNotes: LiveData<List<MeasureWithNotes>> = measureDao.getMeasuresWithNotes()

    suspend fun upsertMeasure(measure: Measure) {
        measureDao.upsertMeasure(measure)
    }

    suspend fun deleteMeasure(measure: Measure) {
        measureDao.deleteMeasure(measure)
    }

    suspend fun deleteMeasures() {
        measureDao.deleteMeasures()
    }
}