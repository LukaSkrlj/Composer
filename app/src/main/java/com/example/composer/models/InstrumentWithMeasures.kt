package com.example.composer.models

import androidx.room.Embedded
import androidx.room.Relation

data class InstrumentWithMeasures(
    @Embedded val instrument: Instrument,
    @Relation(
        parentColumn = "id",
        entityColumn = "instrument_id",
        entity = Measure::class
    )
    val measures: List<MeasureWithNotes> = emptyList()
)
