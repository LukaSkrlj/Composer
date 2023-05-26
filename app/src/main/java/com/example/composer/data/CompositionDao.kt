package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.composer.models.Composition

@Dao
interface CompositionDao {
    @Upsert()
    suspend fun upsertComposition(composition: Composition)

    @Delete
    suspend fun deleteComposition(composition: Composition)

//    @Transaction
//    @Query("SELECT * FROM Composition")
//    fun getCompositionWIthMeasures(): List<CompositionWithMeasures>

    @Query("SELECT * FROM Composition")
    fun getCompositions(): LiveData<List<Composition>>
}