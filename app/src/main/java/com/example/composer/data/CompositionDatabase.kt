package com.example.composer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.composer.models.Composition

@Database(
    entities = [Composition::class],
    version = 1
)
abstract class CompositionDatabase : RoomDatabase() {
    abstract fun compositionDao(): CompositionDao

    companion object {
        @Volatile
        private var INSTANCE: CompositionDatabase? = null

        fun getDatabase(context: Context): CompositionDatabase {
            val temp = INSTANCE
            if (temp != null) {
                return temp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompositionDatabase::class.java,
                    name = "Composition"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}