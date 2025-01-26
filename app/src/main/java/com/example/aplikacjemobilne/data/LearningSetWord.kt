package com.example.aplikacjemobilne.data

import androidx.room.Entity

@Entity(primaryKeys = ["setId", "wordId"])
data class LearningSetWord(
    val setId: Int,   // Powiązanie z LearningSet (id)
    val wordId: Int   // Powiązanie z Word (id)
)