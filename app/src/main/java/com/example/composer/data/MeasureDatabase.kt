package com.example.composer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.composer.models.Measure

@Database(
    entities = [Measure::class],
    version = 1
)
abstract class MeasureDatabase : RoomDatabase() {
    abstract fun measureDao(): MeasureDao

    companion object {
        @Volatile
        private var INSTANCE: MeasureDatabase? = null

        fun getDatabase(context: Context): MeasureDatabase {
            val temp = INSTANCE
            if (temp != null) {
                return temp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeasureDatabase::class.java,
                    name = "Measure"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}