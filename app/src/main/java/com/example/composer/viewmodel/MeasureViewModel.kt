package com.example.composer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.composer.data.ComposerDatabase
import com.example.composer.models.Measure
import com.example.composer.models.MeasureWithNotes
import com.example.composer.repository.MeasureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeasureViewModel(application: Application) : AndroidViewModel(application) {
    val measures: LiveData<List<Measure>>

    val measuresWithNotes: LiveData<List<MeasureWithNotes>>
    private var _itemId = MutableLiveData<Long>()
    val itemId: LiveData<Long> get() = _itemId
    private val repository: MeasureRepository

    init {
        val measureDao = ComposerDatabase.getDatabase(application).measureDao()
        repository = MeasureRepository(measureDao)
        measures = repository.measures
        measuresWithNotes = repository.measuresWithNotes
    }

    fun upsertMeasure(measure: Measure) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertMeasure(measure)
        }
    }

    fun insertMeasure(measure: Measure) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertMeasure(measure)
            _itemId.postValue(id)
        }
    }

    fun deleteMeasure(measure: Measure) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeasure(measure)
        }
    }

    fun deleteMeasures() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMeasures()
        }
    }
}