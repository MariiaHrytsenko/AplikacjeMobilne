package com.example.aplikacjemobilne.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Translation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordId: Int,            // Powiązanie z Word (id)
    val translatedWordId: Int   // Powiązanie z Word (id)
)