package com.example.composer.models

data class FavoriteModel(
    val symphonyName: String,
    val userID: String,
    val compositionId: String,
    val symphonyDurationSeconds: Int,
    val symphonyComposer: String,
    val userName: String,
)
