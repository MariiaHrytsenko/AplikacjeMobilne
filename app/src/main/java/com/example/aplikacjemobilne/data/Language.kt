package com.example.aplikacjemobilne.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Language(
    @PrimaryKey val code: String, // np. "EN", "PL"
    val name: String              // np. "English", "Polski"
)