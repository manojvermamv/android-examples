//package com.anubhav.codereverse.uiapp.database.local
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
//
//@Database(entities = [], version = 2, exportSchema = true)
//@TypeConverters(TypeConverter::class)
//abstract class AppDatabase : RoomDatabase() {
//
//    companion object {
//        // Singleton prevents multiple
//        // instances of database opening at the same time.
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            // if the INSTANCE is not null, then return it,
//            // if it is, then create the database
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "main_db"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//
//    }
//
//}