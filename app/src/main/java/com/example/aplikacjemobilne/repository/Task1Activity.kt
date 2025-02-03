package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.Language
import com.example.aplikacjemobilne.data.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Task1Activity : AppCompatActivity() {
    companion object {
        private const val TAG = "Task1Activity"
        private const val OPTIONS_COUNT = 4
        private const val MIN_WORDS_REQUIRED = 4
        private const val DEFAULT_ROUNDS = 10
    }

    private lateinit var database: AppDatabase
    private lateinit var textViewWord: TextView
    private lateinit var buttonOption1: Button
    private lateinit var buttonOption2: Button
    private lateinit var buttonOption3: Button
    private lateinit var buttonOption4: Button
    private lateinit var buttonNextWord: Button
    private lateinit var buttonBackToMenu: Button

    private var questionIndex = 0
    private var currentWords: List<WordWithTranslations> = listOf()
    private var currentCorrectTranslation: String = ""
    private var hasAnswered = false
    private var languages: List<Language> = listOf()
    private var selectedLanguageCode: String? = null
    private var selectedSourceLanguageCode: String? = null
    private var totalRounds: Int = DEFAULT_ROUNDS

    data class WordWithTranslations(
        val sourceWord: Word,
        val translations: List<Word>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_1)

        database = AppDatabase.getDatabase(this)

        initializeViews()
        setupButtonListeners()
        loadLanguagesAndShowDialog()
    }

    private fun initializeViews() {
        textViewWord = findViewById(R.id.textViewWord)
        buttonOption1 = findViewById(R.id.buttonOption1)
        buttonOption2 = findViewById(R.id.buttonOption2)
        buttonOption3 = findViewById(R.id.buttonOption3)
        buttonOption4 = findViewById(R.id.buttonOption4)
        buttonNextWord = findViewById(R.id.buttonNextWord)
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu)

        setButtonsVisible(false)
    }

    private fun setupButtonListeners() {
        buttonOption1.setOnClickListener { checkAnswer(buttonOption1.text.toString()) }
        buttonOption2.setOnClickListener { checkAnswer(buttonOption2.text.toString()) }
        buttonOption3.setOnClickListener { checkAnswer(buttonOption3.text.toString()) }
        buttonOption4.setOnClickListener { checkAnswer(buttonOption4.text.toString()) }
        
        buttonNextWord.setOnClickListener { 
            if (!hasAnswered) {
                Toast.makeText(this, "Please select an answer first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadNextWord() 
        }
        
        buttonBackToMenu.setOnClickListener {
            finish()
        }
    }

    private fun loadLanguagesAndShowDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                languages = database.languageDao().getAllLanguages()
                
                withContext(Dispatchers.Main) {
                    if (languages.isEmpty()) {
                        Toast.makeText(this@Task1Activity, "Please add some languages first", Toast.LENGTH_LONG).show()
                        finish()
                        return@withContext
                    }

                    showSetupDialog()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading languages", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Task1Activity, "Error loading languages", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showSetupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task1_setup, null)
        val targetLanguageSpinner = dialogView.findViewById<Spinner>(R.id.dialogSpinnerLanguage)
        val sourceLanguageSpinner = dialogView.findViewById<Spinner>(R.id.dialogSpinnerSourceLanguage)
        val roundsEditText = dialogView.findViewById<EditText>(R.id.dialogEditTextRounds)
        val errorText = dialogView.findViewById<TextView>(R.id.dialogTextViewError)
        
        // Setup language spinners
        val languagesList = languages.map { "${it.code}: ${it.name}" }
        
        val targetAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languagesList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        val sourceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languagesList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        targetLanguageSpinner.adapter = targetAdapter
        sourceLanguageSpinner.adapter = sourceAdapter

        // Set default rounds
        roundsEditText.setText(DEFAULT_ROUNDS.toString())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Setup Task 1")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start", null)
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .create()

        dialog.setOnShowListener { dialogInterface ->
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            
            fun validateSetup() {
                if (targetLanguageSpinner.selectedItemPosition == sourceLanguageSpinner.selectedItemPosition) {
                    errorText.text = "Source and target languages must be different"
                    errorText.visibility = View.VISIBLE
                    positiveButton.isEnabled = false
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val targetLanguageCode = languages[targetLanguageSpinner.selectedItemPosition].code
                        val sourceLanguageCode = languages[sourceLanguageSpinner.selectedItemPosition].code
                        val rounds = roundsEditText.text.toString().toIntOrNull() ?: DEFAULT_ROUNDS
                        
                        val allWords = database.wordDao().getAllWords()
                        val allTranslations = database.translationDao().getAllTranslations()
                        
                        // Get words in target language and check translations in both directions
                        val wordsWithTranslations = allWords.filter { word ->
                            word.languageCode == targetLanguageCode
                        }.map { sourceWord ->
                            val translationIds = allTranslations
                                .filter { it.wordId == sourceWord.id || it.translatedWordId == sourceWord.id }
                                .map { if (it.wordId == sourceWord.id) it.translatedWordId else it.wordId }
                            
                            val translations = allWords.filter { word -> 
                                word.id in translationIds && word.languageCode == sourceLanguageCode
                            }
                            
                            WordWithTranslations(sourceWord, translations)
                        }.filter { it.translations.isNotEmpty() }

                        val totalTranslations = wordsWithTranslations
                            .flatMap { it.translations }
                            .map { it.word }
                            .distinct()

                        withContext(Dispatchers.Main) {
                            when {
                                rounds < MIN_WORDS_REQUIRED -> {
                                    errorText.text = "Minimum $MIN_WORDS_REQUIRED rounds required"
                                    errorText.visibility = View.VISIBLE
                                    positiveButton.isEnabled = false
                                }
                                wordsWithTranslations.isEmpty() -> {
                                    errorText.text = "No translations found between selected languages"
                                    errorText.visibility = View.VISIBLE
                                    positiveButton.isEnabled = false
                                }
                                wordsWithTranslations.size < rounds -> {
                                    errorText.text = "Not enough words with translations. Available: ${wordsWithTranslations.size}"
                                    errorText.visibility = View.VISIBLE
                                    positiveButton.isEnabled = false
                                }
                                totalTranslations.size < MIN_WORDS_REQUIRED -> {
                                    errorText.text = "Need at least $MIN_WORDS_REQUIRED different translations in ${languages[sourceLanguageSpinner.selectedItemPosition].name}"
                                    errorText.visibility = View.VISIBLE
                                    positiveButton.isEnabled = false
                                }
                                else -> {
                                    errorText.visibility = View.GONE
                                    positiveButton.isEnabled = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            errorText.text = "Error validating setup"
                            errorText.visibility = View.VISIBLE
                            positiveButton.isEnabled = false
                        }
                    }
                }
            }

            // Initial validation
            validateSetup()

            // Setup listeners for validation
            targetLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    validateSetup()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            sourceLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    validateSetup()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            roundsEditText.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    validateSetup()
                }
            })

            positiveButton.setOnClickListener {
                selectedLanguageCode = languages[targetLanguageSpinner.selectedItemPosition].code
                selectedSourceLanguageCode = languages[sourceLanguageSpinner.selectedItemPosition].code
                totalRounds = roundsEditText.text.toString().toIntOrNull() ?: DEFAULT_ROUNDS
                loadWordsFromDatabase()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun loadWordsFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allWords = database.wordDao().getAllWords()
                val allTranslations = database.translationDao().getAllTranslations()
                
                // Get words in target language and check translations in both directions
                val wordsWithTranslations = allWords.filter { word ->
                    word.languageCode == selectedLanguageCode
                }.map { sourceWord ->
                    val translationIds = allTranslations
                        .filter { it.wordId == sourceWord.id || it.translatedWordId == sourceWord.id }
                        .map { if (it.wordId == sourceWord.id) it.translatedWordId else it.wordId }
                    
                    val translations = allWords.filter { word -> 
                        word.id in translationIds && word.languageCode == selectedSourceLanguageCode
                    }
                    
                    WordWithTranslations(sourceWord, translations)
                }.filter { it.translations.isNotEmpty() }

                currentWords = wordsWithTranslations.shuffled()
                
                withContext(Dispatchers.Main) {
                    questionIndex = 0
                    loadWordAndOptions()
                    setButtonsVisible(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading words", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Task1Activity, "Error loading words", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun loadWordAndOptions() {
        if (questionIndex >= totalRounds || questionIndex >= currentWords.size) {
            showResults()
            return
        }

        hasAnswered = false

        // Reset button colors
        val defaultColor = getColor(android.R.color.holo_purple)
        buttonOption1.setBackgroundColor(defaultColor)
        buttonOption2.setBackgroundColor(defaultColor)
        buttonOption3.setBackgroundColor(defaultColor)
        buttonOption4.setBackgroundColor(defaultColor)

        val currentWord = currentWords[questionIndex % currentWords.size]
        textViewWord.text = currentWord.sourceWord.word

        val correctTranslation = currentWord.translations.random()
        currentCorrectTranslation = correctTranslation.word

        // Get all translations in source language
        val allPossibleTranslations = currentWords
            .flatMap { it.translations }
            .map { it.word }
            .distinct()
            .filter { it != currentWord.sourceWord.word }
            .toMutableList()

        allPossibleTranslations.remove(currentCorrectTranslation)

        val wrongOptions = allPossibleTranslations
            .shuffled()
            .take(OPTIONS_COUNT - 1)

        val options = (wrongOptions + currentCorrectTranslation).shuffled()

        buttonOption1.text = options.getOrNull(0) ?: ""
        buttonOption2.text = options.getOrNull(1) ?: ""
        buttonOption3.text = options.getOrNull(2) ?: ""
        buttonOption4.text = options.getOrNull(3) ?: ""

        // Update Next Word button text based on current round
        val isLastRound = questionIndex == totalRounds - 1 || questionIndex == currentWords.size - 1
        buttonNextWord.text = if (isLastRound) "Show Results" else "Next Word"

        setButtonsEnabled(true)
    }

    private fun checkAnswer(selectedAnswer: String) {
        hasAnswered = true
        
        // Find the button with correct answer and the selected button
        val buttons = listOf(buttonOption1, buttonOption2, buttonOption3, buttonOption4)
        val correctButton = buttons.find { it.text == currentCorrectTranslation }
        val selectedButton = buttons.find { it.text == selectedAnswer }

        if (selectedAnswer == currentCorrectTranslation) {
            // Correct answer - make selected button green
            selectedButton?.setBackgroundColor(getColor(android.R.color.holo_green_light))
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(true)
        } else {
            // Wrong answer - make selected button red and correct button green
            selectedButton?.setBackgroundColor(getColor(android.R.color.holo_red_light))
            correctButton?.setBackgroundColor(getColor(android.R.color.holo_green_light))
            
            // Make other wrong buttons red
            buttons.forEach { button ->
                if (button != correctButton && button.text != selectedAnswer) {
                    button.setBackgroundColor(getColor(android.R.color.holo_red_light))
                }
            }
            
            Toast.makeText(this, "Incorrect. The correct answer was: $currentCorrectTranslation", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(false)
        }
        
        setButtonsEnabled(false)
    }

    private fun loadNextWord() {
        questionIndex++
        if (questionIndex < currentWords.size) {
            loadWordAndOptions()
        } else {
            showResults()
        }
    }

    private fun showResults() {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("taskNumber", 1)
        startActivity(intent)
        finish()
    }

    private fun setButtonsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.INVISIBLE
        buttonOption1.visibility = visibility
        buttonOption2.visibility = visibility
        buttonOption3.visibility = visibility
        buttonOption4.visibility = visibility
        buttonNextWord.visibility = visibility
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        buttonOption1.isEnabled = enabled
        buttonOption2.isEnabled = enabled
        buttonOption3.isEnabled = enabled
        buttonOption4.isEnabled = enabled
    }
}
