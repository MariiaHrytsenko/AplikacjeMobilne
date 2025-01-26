package com.example.aplikacjemobilne.data

import androidx.room.*

@Dao
interface LearningStatisticDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statistic: LearningStatistic)

    @Query("SELECT * FROM LearningStatistic WHERE setId = :setId")
    suspend fun getStatisticsForSet(setId: Int): List<LearningStatistic>

    @Delete
    suspend fun delete(statistic: LearningStatistic)
}