package com.example.composer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.composer.data.ComposerDatabase
import com.example.composer.models.Composition
import com.example.composer.models.CompositionWithInstruments
import com.example.composer.repository.CompositionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CompositionViewModel(application: Application) : AndroidViewModel(application) {
    val compositions: LiveData<List<Composition>>
    val lastComposition: LiveData<Composition>
    private val repository: CompositionRepository

    init {
        val compositionDao = ComposerDatabase.getDatabase(application).compositionDao()
        repository = CompositionRepository(compositionDao)
        compositions = repository.compositions
        lastComposition = repository.lastComposition
    }

    fun getCompositionWIthInstruments(id: Int): LiveData<CompositionWithInstruments> {
        return repository.getCompositionWithInstruments(id)
    }

    fun upsertComposition(composition: Composition) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertComposition(composition)
        }
    }

    fun updateCompositionInfo(
        compositionName: String,
        authorName: String,
        compositionId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCompositionInfo(compositionName, authorName, compositionId)
        }
    }

    fun insertComposition(composition: Composition): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            val compositionId = async { repository.insertComposition(composition) }
            result.postValue(compositionId.await())
        }
        return result
    }

    fun deleteComposition(composition: Composition) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteComposition(composition)
        }
    }
}
