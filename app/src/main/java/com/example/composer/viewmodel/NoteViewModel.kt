package com.example.composer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.composer.data.ComposerDatabase
import com.example.composer.models.Note
import com.example.composer.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    val notes: LiveData<List<Note>>
    private val repository: NoteRepository

    init {
        val noteDao = ComposerDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        notes = repository.notes
    }

    fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertNote(note)
        }
    }

    fun deleteNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotes()
        }
    }
}