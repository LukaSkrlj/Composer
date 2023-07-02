package com.example.composer.constants

val REST = "rest"

val INSTRUMENTS = InstrumentType.values().map { type -> type.toString().replace("_", " ") }