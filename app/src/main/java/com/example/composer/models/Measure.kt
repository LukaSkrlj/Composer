package com.example.composer.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Measure",
    foreignKeys = [ForeignKey(
        entity = Instrument::class,
        parentColumns = ["id"],
        childColumns = ["instrument_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Measure(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var timeSignatureTop: Int = 4,
    var timeSignatureBottom: Int = 4,
    var keySignature: String,
    @ColumnInfo(name = "instrument_id") var instrumentId: Int,
    var clef: String,
    var position: Int = 0
)
