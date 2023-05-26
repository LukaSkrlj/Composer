package com.example.composer.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
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
}