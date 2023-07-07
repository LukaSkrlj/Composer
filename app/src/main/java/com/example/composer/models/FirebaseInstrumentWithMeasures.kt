package com.example.composer.models

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseInstrumentWithMeasures(
    val instrument: FirebaseInstrument,
    val measures: List<FirebaseMeasureWithNotes>
)
