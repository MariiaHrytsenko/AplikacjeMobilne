package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
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

class Task2Activity : AppCompatActivity() {
    companion object {
        private const val TAG = "Task2Activity"
        private const val MIN_WORDS_REQUIRED = 4
        private const val DEFAULT_ROUNDS = 10
    }

    private lateinit var database: AppDatabase
    private lateinit var textViewWord: TextView
    private lateinit var editTextTranslation: EditText
    private lateinit var buttonCheck: Button
    private lateinit var buttonNextWord: Button
    private lateinit var buttonBackToMenu: Button
    private lateinit var textViewFeedback: TextView

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
        setContentView(R.layout.activity_task_2)

        database = AppDatabase.getDatabase(this)

        initializeViews()
        setupListeners()
        loadLanguagesAndShowDialog()
    }

    private fun initializeViews() {
        textViewWord = findViewById(R.id.textViewWord)
        editTextTranslation = findViewById(R.id.editTextTranslation)
        buttonCheck = findViewById(R.id.buttonCheck)
        buttonNextWord = findViewById(R.id.buttonNextWord)
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu)
        textViewFeedback = findViewById(R.id.textViewFeedback)

        setViewsVisible(false)
    }

    private fun setupListeners() {
        buttonCheck.setOnClickListener { 
            checkAnswer()
        }
        
        buttonNextWord.setOnClickListener { 
            loadNextWord() 
        }
        
        buttonBackToMenu.setOnClickListener {
            finish()
        }

        // Obsługa przycisku "Done" na klawiaturze
        editTextTranslation.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer()
                true
            } else {
                false
            }
        }
    }

    private fun loadLanguagesAndShowDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                languages = database.languageDao().getAllLanguages()
                
                withContext(Dispatchers.Main) {
                    if (languages.isEmpty()) {
                        Toast.makeText(this@Task2Activity, "Please add some languages first", Toast.LENGTH_LONG).show()
                        finish()
                        return@withContext
                    }

                    showSetupDialog()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading languages", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Task2Activity, "Error loading languages", Toast.LENGTH_SHORT).show()
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
            .setTitle("Setup Task 2")
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
                
                // Get all word pairs that have translations between selected languages
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
                    setViewsVisible(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading words", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Task2Activity, "Error loading words", Toast.LENGTH_SHORT).show()
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
        buttonNextWord.visibility = View.GONE
        textViewFeedback.visibility = View.GONE
        
        val currentWord = currentWords[questionIndex % currentWords.size]
        textViewWord.text = currentWord.sourceWord.word
        
        editTextTranslation.text.clear()
        editTextTranslation.isEnabled = true
        buttonCheck.isEnabled = true

        // Update Next Word button text based on current round
        val isLastRound = questionIndex == totalRounds - 1 || questionIndex == currentWords.size - 1
        buttonNextWord.text = if (isLastRound) "Show Results" else "Next Word"
    }

    private fun checkAnswer() {
        if (hasAnswered) return
        
        val userAnswer = editTextTranslation.text.toString().trim()
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Please enter your answer", Toast.LENGTH_SHORT).show()
            return
        }

        hasAnswered = true
        editTextTranslation.isEnabled = false
        buttonCheck.isEnabled = false
        buttonNextWord.visibility = View.VISIBLE
        textViewFeedback.visibility = View.VISIBLE

        val currentWord = currentWords[questionIndex % currentWords.size]
        val isCorrect = currentWord.translations.any { 
            it.word.equals(userAnswer, ignoreCase = true) 
        }

        if (isCorrect) {
            textViewFeedback.text = "Correct!"
            textViewFeedback.setTextColor(getColor(android.R.color.holo_green_dark))
            ResultsManager.updateResults(true)
        } else {
            val correctAnswers = currentWord.translations.joinToString(", ") { it.word }
            textViewFeedback.text = "Incorrect. Correct answer(s): $correctAnswers"
            textViewFeedback.setTextColor(getColor(android.R.color.holo_red_dark))
            ResultsManager.updateResults(false)
        }
    }

    private fun loadNextWord() {
        questionIndex++
        loadWordAndOptions()
    }

    private fun showResults() {
        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("taskNumber", 2)
        startActivity(intent)
        finish()
    }

    private fun setViewsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.INVISIBLE
        textViewWord.visibility = visibility
        editTextTranslation.visibility = visibility
        buttonCheck.visibility = visibility
    }
}
