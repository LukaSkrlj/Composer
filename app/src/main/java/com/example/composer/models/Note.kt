package com.example.composer.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.composer.constants.REST

@Entity(tableName = "Note",
    foreignKeys = [ForeignKey(
        entity = Measure::class,
        parentColumns = ["id"],
        childColumns = ["measure_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Note(
    @PrimaryKey(autoGenerate = true) var noteId: Int = 0,
    @ColumnInfo(name = "measure_id") var measureId: Int,
    var pitch: String = REST,
    var left: Int = 0, var top: Int = 0, var right: Int = 0, var bottom: Int = 0,
    var dx: Float = 0f, var dy: Float = 0f, var resourceId: String = "",
    var length: Float = 0.25f
)
