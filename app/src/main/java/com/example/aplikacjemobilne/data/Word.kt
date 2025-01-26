package com.example.aplikacjemobilne.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val languageCode: String // Powiązanie z Language (code)
)