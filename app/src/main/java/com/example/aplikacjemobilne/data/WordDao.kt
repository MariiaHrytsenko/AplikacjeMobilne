package com.example.aplikacjemobilne.data

import androidx.room.*

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: Word)

    @Query("SELECT * FROM Word")
    suspend fun getAllWords(): List<Word>

    @Query("SELECT * FROM Word WHERE id = :id")
    suspend fun getWordById(id: Int): Word

    @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)
}