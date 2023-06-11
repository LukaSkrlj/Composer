package com.example.composer.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class Instrument(
    @PrimaryKey() val id: Int,
    val name: String,
    @ColumnInfo(name = "composition_id") val compositionId: Int
)
