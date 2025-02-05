package com.example.aplikacjemobilne.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Language::class, Word::class, Translation::class, LearningSet::class, LearningStatistic::class, TaskResult::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun languageDao(): LanguageDao
    abstract fun wordDao(): WordDao
    abstract fun translationDao(): TranslationDao
    abstract fun learningSetDao(): LearningSetDao
    abstract fun learningStatisticDao(): LearningStatisticDao
    abstract fun taskResultDao(): TaskResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "language_learning_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}