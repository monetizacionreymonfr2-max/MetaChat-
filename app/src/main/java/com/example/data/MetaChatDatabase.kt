package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocalContactEntity::class, LocalMessageEntity::class], version = 1, exportSchema = false)
abstract class MetaChatDatabase : RoomDatabase() {
    
    abstract fun metaChatDao(): MetaChatDao

    companion object {
        @Volatile
        private var INSTANCE: MetaChatDatabase? = null

        fun getDatabase(context: Context): MetaChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetaChatDatabase::class.java,
                    "metachat_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
