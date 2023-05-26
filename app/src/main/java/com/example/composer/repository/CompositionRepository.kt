package com.example.composer.repository

import androidx.lifecycle.LiveData
import com.example.composer.data.CompositionDao
import com.example.composer.models.Composition
import com.example.composer.models.CompositionWithMeasures

class CompositionRepository(private val compositionDao: CompositionDao) {
    val compositions: LiveData<List<Composition>> = compositionDao.getCompositions()

    suspend fun upsertComposition(composition: Composition) {
        compositionDao.upsertComposition(composition)
    }

    suspend fun deleteComposition(composition: Composition) {
        compositionDao.deleteComposition(composition)
    }

    fun getCompositionWIthMeasures(id: Int): List<CompositionWithMeasures> {
        return compositionDao.getCompositionWIthMeasures(id)
    }
}