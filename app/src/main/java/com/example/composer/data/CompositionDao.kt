package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.composer.models.Composition
import com.example.composer.models.CompositionWithInstruments

@Dao
interface CompositionDao {
    @Upsert()
    suspend fun upsertComposition(composition: Composition)

    @Insert()
    suspend fun insertComposition(composition: Composition): Long

    @Delete
    suspend fun deleteComposition(composition: Composition)

    @Query("UPDATE Composition SET name= :compositionName, author = :authorName WHERE id = :compositionId")
    suspend fun  updateCompositionInfo(compositionName: String, authorName: String, compositionId: Int)

    @Transaction
    @Query("SELECT * FROM Composition WHERE id = :id")
    fun getCompositionWithInstruments(id: Int): LiveData<CompositionWithInstruments>


    @Query("SELECT * FROM Composition")
    fun getCompositions(): LiveData<List<Composition>>

    @Query("SELECT * FROM Composition ORDER BY id DESC LIMIT 1")
    fun getLastComposition(): LiveData<Composition>
}
