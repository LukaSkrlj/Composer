package com.example.composer.repository

import androidx.lifecycle.LiveData
import com.example.composer.data.MeasureDao
import com.example.composer.models.Measure

class MeasureRepository(private val measureDao: MeasureDao) {
    val measures: LiveData<List<Measure>> = measureDao.getMeasures()

    suspend fun upsertMeasure(measure: Measure) {
        measureDao.upsertMeasure(measure)
    }

    suspend fun deleteMeasure(measure: Measure) {
        measureDao.deleteMeasure(measure)
    }
}