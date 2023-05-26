package com.example.composer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.composer.data.CompositionDatabase
import com.example.composer.models.Composition
import com.example.composer.repository.CompositionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompositionViewModel(application: Application) : AndroidViewModel(application) {
    val compositions: LiveData<List<Composition>>
    private val repository: CompositionRepository

    init {
        val compositionDao = CompositionDatabase.getDatabase(application).compositionDao()
        repository = CompositionRepository(compositionDao)
        compositions = repository.compositions
    }

//    fun getCompositionWIthMeasures(id: Int): List<CompositionWithMeasures> {
//        return repository.getCompositionWIthMeasures(id)
//    }

    fun upsertComposition(composition: Composition) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertComposition(composition)
        }
    }

    fun deleteComposition(composition: Composition) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteComposition(composition)
        }
    }
}