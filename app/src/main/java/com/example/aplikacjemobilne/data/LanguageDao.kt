package com.example.aplikacjemobilne.data

import androidx.room.*

@Dao
interface LanguageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(language: Language)

    @Query("SELECT * FROM Language")
    suspend fun getAllLanguages(): List<Language>

    @Delete
    suspend fun delete(language: Language)
}