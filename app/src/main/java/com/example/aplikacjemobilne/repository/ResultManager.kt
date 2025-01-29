package com.example.aplikacjemobilne.repository

object ResultsManager {
    private var correctAnswers = 0
    private var wrongAnswers = 0

    fun getCorrectAnswers(): Int {
        return correctAnswers
    }

    fun getWrongAnswers(): Int {
        return wrongAnswers
    }

    fun updateResults(isCorrect: Boolean) {
        if (isCorrect) {
            correctAnswers++
        } else {
            wrongAnswers++
        }
    }

    fun resetResults() {
        correctAnswers = 0
        wrongAnswers = 0
    }
}
