package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.composer.models.Note

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM Note")
    fun deleteNotes(): Void

    @Query("SELECT * FROM Note")
    fun getNotes(): LiveData<List<Note>>

    @Query("SELECT MAX(dx) FROM Note")
    fun getLastNote(): LiveData<Int>
}
