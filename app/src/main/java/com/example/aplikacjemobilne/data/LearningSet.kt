package com.example.aplikacjemobilne.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LearningSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)