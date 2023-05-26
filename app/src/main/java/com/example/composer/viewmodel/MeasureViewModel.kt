package com.example.composer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.composer.data.MeasureDatabase
import com.example.composer.models.Measure
import com.example.composer.repository.MeasureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeasureViewModel(application: Application) : AndroidViewModel(application) {
    val measures: LiveData<List<Measure>>
    private val repository: MeasureRepository

    init {
        val measureDao = MeasureDatabase.getDatabase(application).measureDao()
        repository = MeasureRepository(measureDao)
        measures = repository.measures
    }

    fun addNote(measure: Measure) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertMeasure(measure)
        }
    }

    fun deleteMeasure(measure: Measure) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeasure(measure)
        }
    }
}