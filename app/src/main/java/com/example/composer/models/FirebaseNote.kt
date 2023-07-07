package com.example.composer.models

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseNote(
    val noteId: Int,
    val measureId: Int,
    val pitch: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val dx: Float,
    val dy: Float,
    val resourceId: String,
    val length: Float
    // Add other properties as needed
)
