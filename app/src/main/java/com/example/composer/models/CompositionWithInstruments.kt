package com.example.composer.models

import androidx.room.Embedded
import androidx.room.Relation

data class CompositionWithInstruments(
    @Embedded val composition: Composition,
    @Relation(
        parentColumn = "id",
        entityColumn = "composition_id",
        entity = Instrument::class
    )
    val instruments: List<InstrumentWithMeasures> = emptyList()
)
