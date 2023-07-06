package com.example.composer.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.composer.data.NoteDao
import com.example.composer.models.Note

class NoteRepository(private val noteDao: NoteDao) {
    val notes: LiveData<List<Note>> = noteDao.getNotes()

    suspend fun upsertNote(note: Note) {
       noteDao.upsertNote(note).toString()
    }

    suspend fun updateNote(note: Note){
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note): Int{
        return noteDao.deleteNote(note)
    }

    suspend fun deleteNotes() {
        noteDao.deleteNotes()
    }
}
