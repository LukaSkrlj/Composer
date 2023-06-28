package com.example.composer.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "Instrument",
    foreignKeys = [ForeignKey(
        entity = Composition::class,
        parentColumns = ["id"],
        childColumns = ["composition_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Instrument(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "piano",
    var position: Int = 0,
    @ColumnInfo(name = "composition_id") var compositionId: Int
)
