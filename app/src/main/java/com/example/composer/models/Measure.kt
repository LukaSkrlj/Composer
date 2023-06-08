package com.example.composer.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Measure(
    @PrimaryKey() var id: Int = 0,
    var timeSignatureTop: Int = 4,
    var timeSignatureBottom: Int = 4,
    var keySignature: String,
    var compositionId: Int,
    var clef: String
)
