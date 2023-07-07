package com.example.composer.models

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseInstrument(
    val id: Int,
    val name: String,
    val position: Int,
    val compositionId: Int
    // Add other properties as needed
)
