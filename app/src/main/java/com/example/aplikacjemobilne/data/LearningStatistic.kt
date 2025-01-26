package com.example.aplikacjemobilne.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LearningStatistic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setId: Int,    // Powiązanie z LearningSet (id)
    val wordId: Int,   // Powiązanie z Word (id)
    val attempts: Int,
    val correct: Int
)