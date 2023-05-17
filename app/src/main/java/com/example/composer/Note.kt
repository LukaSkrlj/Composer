package com.example.composer

import android.graphics.drawable.Drawable

data class NoteSize(val left: Int = 0, val top: Int = 0, val right: Int, val bottom: Int)
data class NotePosition(val dx: Int, val dy: Int)
data class Note(val noteName: String, val noteWidth: Int, val noteHeight: Int, val resourceID: Int)
