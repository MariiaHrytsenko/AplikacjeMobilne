package com.example.aplikacjemobilne.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "task_results")
data class TaskResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskNumber: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val totalQuestions: Int,
    val date: Long = System.currentTimeMillis()
) 