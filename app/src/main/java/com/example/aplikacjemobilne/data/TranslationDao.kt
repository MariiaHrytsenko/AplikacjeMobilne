package com.example.aplikacjemobilne.data

import androidx.room.*

@Dao
interface TranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(translation: Translation): Long

    @Query("SELECT * FROM Translation")
    suspend fun getAllTranslations(): List<Translation>

    @Delete
    suspend fun delete(translation: Translation)
}