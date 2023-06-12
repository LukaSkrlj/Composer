package com.example.composer.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class Composition(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String,
    var author: String,
    var userId: Int? = null
)
