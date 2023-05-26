package com.example.composer.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var pitch: String = "c3",
    var left: Int = 0, var top: Int = 0, var right: Int, var bottom: Int,
    var dx: Float, var dy: Float
)
