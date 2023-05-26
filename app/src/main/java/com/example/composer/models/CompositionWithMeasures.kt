package com.example.composer.models

import androidx.room.Embedded
import androidx.room.Relation

data class CompositionWithMeasures(
    @Embedded val composition: Composition,
    @Relation(
        parentColumn = "id",
        entityColumn = "compositionId"
    )
    val measures: List<Measure>
)
