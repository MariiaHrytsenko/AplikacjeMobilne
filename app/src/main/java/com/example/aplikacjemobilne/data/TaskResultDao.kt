package com.example.aplikacjemobilne.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskResultDao {
    @Insert
    suspend fun insertTaskResult(taskResult: TaskResult)

    @Query("SELECT * FROM task_results WHERE taskNumber = :taskNumber ORDER BY date DESC")
    suspend fun getResultsForTask(taskNumber: Int): List<TaskResult>

    @Query("SELECT * FROM task_results ORDER BY date DESC")
    suspend fun getAllResults(): List<TaskResult>
} 