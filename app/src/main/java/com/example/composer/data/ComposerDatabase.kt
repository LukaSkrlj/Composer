package com.example.composer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.composer.models.Composition
import com.example.composer.models.Measure
import com.example.composer.models.Note

@Database(
    entities = [Note::class, Composition::class, Measure::class],
    version = 1
)
abstract class ComposerDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun compositionDao(): CompositionDao
    abstract fun measureDao(): MeasureDao


    companion object {
        @Volatile
        private var INSTANCE: ComposerDatabase? = null

        fun getDatabase(context: Context): ComposerDatabase {
            val temp = INSTANCE
            if (temp != null) {
                return temp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ComposerDatabase::class.java,
                    name = "Composer"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}