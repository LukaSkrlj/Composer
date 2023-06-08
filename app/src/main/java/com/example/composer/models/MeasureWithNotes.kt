package com.example.composer.models

import androidx.room.Embedded
import androidx.room.Relation

data class MeasureWithNotes(
    @Embedded val measure: Measure,
    @Relation(
        parentColumn = "id",
        entityColumn = "measure_id",
    )
    val notes: List<Note> = emptyList()
)
