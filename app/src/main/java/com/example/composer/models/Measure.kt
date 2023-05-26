package com.example.composer.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Measure(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var timeSignatureTop: Int,
    var timeSignatureBottom: Int,
    var keySignature: String,
    var compositionId: Int
)
