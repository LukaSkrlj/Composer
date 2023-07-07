package com.example.composer.models

import com.example.composer.models.FirebaseNote
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseMeasure(
    val id: Int,
    val timeSignatureTop: Int,
    val timeSignatureBottom: Int,
    val keySignature: String,
    val instrumentId: Int,
    val clef: String,
    val position: Int,
    // Add other properties as needed
)
