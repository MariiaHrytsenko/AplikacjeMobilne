package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

class Task1Activity : AppCompatActivity() {

    private lateinit var textViewWord: TextView
    private lateinit var buttonOption1: Button
    private lateinit var buttonOption2: Button
    private lateinit var buttonOption3: Button
    private lateinit var buttonOption4: Button
    private lateinit var buttonNextWord: Button
    private lateinit var buttonShowResults: Button

    private var questionIndex = 0

    private val words = listOf(
        WordData("Hello", "Hola", listOf("Bonjour", "Ciao", "Hallo")),
        WordData("Goodbye", "Adiós", listOf("Au revoir", "Arrivederci", "Tschüss")),
        WordData("Please", "Por favor", listOf("S'il vous plaît", "Per favore", "Bitte"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_1)

        // Ініціалізація елементів
        textViewWord = findViewById(R.id.textViewWord)
        buttonOption1 = findViewById(R.id.buttonOption1)
        buttonOption2 = findViewById(R.id.buttonOption2)
        buttonOption3 = findViewById(R.id.buttonOption3)
        buttonOption4 = findViewById(R.id.buttonOption4)
        buttonNextWord = findViewById(R.id.buttonNextWord)
        buttonShowResults = findViewById(R.id.buttonShowResults)

        // Завантажуємо перше слово
        loadWordAndOptions()

        // Обробники натискань
        buttonOption1.setOnClickListener { checkAnswer(buttonOption1.text.toString()) }
        buttonOption2.setOnClickListener { checkAnswer(buttonOption2.text.toString()) }
        buttonOption3.setOnClickListener { checkAnswer(buttonOption3.text.toString()) }
        buttonOption4.setOnClickListener { checkAnswer(buttonOption4.text.toString()) }

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
        val options = listOf(wordData.correctTranslation, *wordData.wrongTranslations.toTypedArray()).shuffled()

        buttonOption1.text = options[0]
        buttonOption2.text = options[1]
        buttonOption3.text = options[2]
        buttonOption4.text = options[3]

        buttonOption1.visibility = View.VISIBLE
        buttonOption2.visibility = View.VISIBLE
        buttonOption3.visibility = View.VISIBLE
        buttonOption4.visibility = View.VISIBLE
        buttonNextWord.visibility = View.VISIBLE
        buttonShowResults.visibility = View.VISIBLE
    }

    private fun checkAnswer(selectedAnswer: String) {
        val correctTranslation = words[questionIndex].correctTranslation

        if (selectedAnswer == correctTranslation) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(true)
        } else {
            Toast.makeText(this, "Incorrect.", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(false)
        }
    }

    private fun loadNextWord() {
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

    data class WordData(val word: String, val correctTranslation: String, val wrongTranslations: List<String>)
}
