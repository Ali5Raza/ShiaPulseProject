package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PrayerLog::class, QadaTally::class, FavoriteQuote::class, RamadanDayRecord::class, TasbeehDailyRecord::class, ZiyaratItem::class, ZiyaratVisitRecord::class, ZiyaratJournalRecord::class, AamalCustomActivity::class, AamalCompletion::class, HajjUmrahChecklistItem::class], version = 10, exportSchema = false)
abstract class PrayerDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao

    companion object {
        @Volatile
        private var INSTANCE: PrayerDatabase? = null

        fun getDatabase(context: Context): PrayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrayerDatabase::class.java,
                    "shia_prayer_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
