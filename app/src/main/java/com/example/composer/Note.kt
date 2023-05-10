package com.example.composer

data class NoteSize(val left: Int = 0, val top: Int = 0, val right: Int, val bottom: Int)
data class NotePosition(val dx: Int, val dy: Int)
data class Note(val noteSize: NoteSize, val notePosition: NotePosition)
