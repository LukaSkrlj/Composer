package com.example.composer.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Measure(
    @PrimaryKey() var id: Int = 0,
    var timeSignatureTop: Int = 4,
    var timeSignatureBottom: Int = 4,
    @ColumnInfo(name = "instrument_id") var instrumentId: Int = 0,
    var keySignature: String = "",
    var compositionId: Int = 0,
    var clef: String = "treble"
)
