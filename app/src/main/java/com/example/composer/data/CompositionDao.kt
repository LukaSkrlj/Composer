package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.composer.models.Composition
import com.example.composer.models.CompositionWithMeasures

@Dao
interface CompositionDao {
    @Upsert()
    suspend fun upsertComposition(composition: Composition)

    @Delete
    suspend fun deleteComposition(composition: Composition)

    @Transaction
    @Query("SELECT * FROM Composition WHERE id = :id")
    fun getCompositionWIthMeasures(id: Int): List<CompositionWithMeasures>

    @Query("SELECT * FROM Composition")
    fun getCompositions(): LiveData<List<Composition>>
}