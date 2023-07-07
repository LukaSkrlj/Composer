package com.example.composer.models

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseMeasureWithNotes(
    val measure: FirebaseMeasure,
    val notes: List<FirebaseNote>
)
