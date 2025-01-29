package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

class Task2Activity : AppCompatActivity() {

    private lateinit var textViewWord: TextView
    private lateinit var editTextTranslation: EditText
    private lateinit var buttonNextWord: Button
    private lateinit var buttonShowResults: Button

    private var questionIndex = 0

    private val words = listOf(
        WordData("Hello", "Hola"),
        WordData("Goodbye", "Adiós"),
        WordData("Please", "Por favor")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_2)

        textViewWord = findViewById(R.id.textViewWord)
        editTextTranslation = findViewById(R.id.editTextTranslation)
        buttonNextWord = findViewById(R.id.buttonNextWord)
        buttonShowResults = findViewById(R.id.buttonShowResults)

        loadWordAndOptions()

        buttonNextWord.setOnClickListener { loadNextWord() }
        buttonShowResults.setOnClickListener { showResults() }
    }

    private fun loadWordAndOptions() {
        if (questionIndex >= words.size) {
            showResults()
            return
        }

        val wordData = words[questionIndex]
        textViewWord.text = wordData.word

        editTextTranslation.visibility = View.VISIBLE
        buttonNextWord.visibility = View.VISIBLE
        buttonShowResults.visibility = View.VISIBLE
    }

    private fun checkAnswer(selectedAnswer: String) {
        //need to set LOWERCASE for every word we are checking
        val correctTranslation = words[questionIndex].correctTranslation


        if (selectedAnswer == correctTranslation) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(true)
        } else {
            Toast.makeText(this, "Incorrect.", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(true)
        }
    }

    private fun loadNextWord() {
        val userAnswer = editTextTranslation.text.toString().trim()
        checkAnswer(userAnswer)

        questionIndex++
        if (questionIndex < words.size) {
            loadWordAndOptions()
        } else {
            showResults()
        }
    }


    private fun showResults() {
        val intent = Intent(this, ResultsActivity::class.java)
        startActivity(intent)
    }
    data class WordData(val word: String, val correctTranslation: String)
}
