package com.example.denikv1

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CestaEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cestaDao(): CestaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //Metoda pro získání instance databáze. návrhový vzor Singleton (jedna instance databáze pro celou aplikaci)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "denik_data2"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}