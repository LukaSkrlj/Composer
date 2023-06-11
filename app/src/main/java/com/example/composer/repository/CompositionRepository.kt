package com.example.composer.repository

import androidx.lifecycle.LiveData
import com.example.composer.data.CompositionDao
import com.example.composer.models.Composition
import com.example.composer.models.CompositionWithInstruments

class CompositionRepository(private val compositionDao: CompositionDao) {
    val compositions: LiveData<List<Composition>> = compositionDao.getCompositions()
    val lastComposition: LiveData<Composition> = compositionDao.getLastComposition()

    fun getCompositionWithInstruments(id: Int): LiveData<CompositionWithInstruments> {
        return compositionDao.getCompositionWithInstruments(id)
    }

    suspend fun upsertComposition(composition: Composition) {
        compositionDao.upsertComposition(composition)
    }

    suspend fun deleteComposition(composition: Composition) {
        compositionDao.deleteComposition(composition)
    }
}