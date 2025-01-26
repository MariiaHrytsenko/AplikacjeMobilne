package com.example.aplikacjemobilne.data

import androidx.room.*

@Dao
interface LearningSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: LearningSet)

    @Query("SELECT * FROM LearningSet")
    suspend fun getAllLearningSets(): List<LearningSet>

    @Delete
    suspend fun delete(set: LearningSet)
}