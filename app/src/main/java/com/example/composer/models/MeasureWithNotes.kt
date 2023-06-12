package com.example.composer.models

import androidx.room.Embedded
import androidx.room.Relation

data class MeasureWithNotes(
    @Embedded var measure: Measure = Measure(),
    @Relation(
        parentColumn = "id",
        entityColumn = "measure_id",
    )
    var notes: List<Note> = emptyList()
)
