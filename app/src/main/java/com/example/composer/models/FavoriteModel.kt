package com.example.composer.models

data class FavoriteModel(
    val symphonyName: String,
    val userID: String,
    val symphonyID: String,
    val symphonyDurationSeconds: Int,
    val symphonyComposer: String,
    val userName: String,
)
