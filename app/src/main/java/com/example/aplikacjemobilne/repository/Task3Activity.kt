package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.Language
import com.example.aplikacjemobilne.data.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Task3Activity : AppCompatActivity() {
    companion object {
        private const val TAG = "Task3Activity"
        private const val MIN_WORDS_REQUIRED = 4
        private const val DEFAULT_ROUNDS = 10
        private const val WORDS_PER_ROUND = 4
    }

    private lateinit var database: AppDatabase
    private lateinit var word1: TextView
    private lateinit var word2: TextView
    private lateinit var word3: TextView
    private lateinit var word4: TextView
    private lateinit var translation1: TextView
    private lateinit var translation2: TextView
    private lateinit var translation3: TextView
    private lateinit var translation4: TextView
    private lateinit var buttonCheckMatch: Button
    private lateinit var buttonBackToMenu: Button
    private lateinit var drawingView: DrawingView
    private lateinit var rootLayout: ConstraintLayout

    private var languages: List<Language> = listOf()
    private var selectedLanguageCode: String? = null
    private var selectedSourceLanguageCode: String? = null
    private var totalRounds: Int = DEFAULT_ROUNDS
    private var availableWords: List<WordWithTranslations> = listOf()
    private var questionIndex = 0
    private var selectedWord: TextView? = null
    private var connections: MutableMap<TextView, TextView> = mutableMapOf()

    data class WordWithTranslations(
        val sourceWord: Word,
        val translations: List<Word>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_3)

        database = AppDatabase.getDatabase(this)
        rootLayout = findViewById(R.id.rootLayout)
        
        // Dodaj DrawingView do layoutu
        drawingView = DrawingView(this)
        rootLayout.addView(drawingView, 0) // dodaj na samym spodzie
        
        initializeViews()
        setupWordClickListeners()
        loadLanguagesAndShowDialog()
    }

    private fun initializeViews() {
        word1 = findViewById(R.id.word1)
        word2 = findViewById(R.id.word2)
        word3 = findViewById(R.id.word3)
        word4 = findViewById(R.id.word4)
        translation1 = findViewById(R.id.translation1)
        translation2 = findViewById(R.id.translation2)
        translation3 = findViewById(R.id.translation3)
        translation4 = findViewById(R.id.translation4)
        buttonCheckMatch = findViewById(R.id.buttonCheckMatch)
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu)

        buttonBackToMenu.setOnClickListener {
            finish()
        }

        buttonCheckMatch.setOnClickListener {
            checkMatches()
        }

        setViewsVisible(false)
    }

    private fun setupWordClickListeners() {
        val words = listOf(word1, word2, word3, word4)
        val translations = listOf(translation1, translation2, translation3, translation4)

        words.forEach { word ->
            word.setOnClickListener {
                handleWordClick(word)
            }
        }

        translations.forEach { translation ->
            translation.setOnClickListener {
                handleWordClick(translation)
            }
        }
    }

    private fun handleWordClick(clickedView: TextView) {
        if (selectedWord == null) {
            // Pierwsze kliknięcie
            selectedWord = clickedView
            clickedView.setBackgroundResource(R.drawable.selected_word_background)
        } else if (selectedWord == clickedView) {
            // Kliknięto ten sam element - odznacz
            selectedWord?.setBackgroundResource(if (isSourceWord(clickedView)) R.drawable.word_background else R.drawable.translation_background)
            selectedWord = null
        } else if (isSourceWord(selectedWord!!) == isSourceWord(clickedView)) {
            // Kliknięto element z tej samej kolumny - zmień zaznaczenie
            selectedWord?.setBackgroundResource(if (isSourceWord(selectedWord!!)) R.drawable.word_background else R.drawable.translation_background)
            selectedWord = clickedView
            clickedView.setBackgroundResource(R.drawable.selected_word_background)
        } else {
            // Kliknięto element z przeciwnej kolumny - połącz
            connectWords(selectedWord!!, clickedView)
            selectedWord = null
        }
    }

    private fun isSourceWord(view: TextView): Boolean {
        return view in listOf(word1, word2, word3, word4)
    }

    private fun connectWords(first: TextView, second: TextView) {
        // Usuń poprzednie połączenia i zresetuj ich tła
        connections.forEach { (source, target) ->
            if (source == first || target == first || source == second || target == second) {
                source.setBackgroundResource(if (isSourceWord(source)) R.drawable.word_background else R.drawable.translation_background)
                target.setBackgroundResource(if (isSourceWord(target)) R.drawable.word_background else R.drawable.translation_background)
            }
        }
        
        // Usuń poprzednie połączenia dla obu słów
        connections.entries.removeIf { it.key == first || it.value == first || it.key == second || it.value == second }
        
        // Dodaj nowe połączenie
        if (isSourceWord(first)) {
            connections[first] = second
        } else {
            connections[second] = first
        }
        
        // Ustaw nowe tło dla połączonych słów
        first.setBackgroundResource(R.drawable.connected_word_background)
        second.setBackgroundResource(R.drawable.connected_word_background)
        
        // Przerysuj linie
        drawingView.invalidate()
    }

    private fun resetWordBackgrounds() {
        val words = listOf(word1, word2, word3, word4)
        val translations = listOf(translation1, translation2, translation3, translation4)

        words.forEach { word ->
            word.setBackgroundResource(R.drawable.word_background)
        }
        translations.forEach { translation ->
            translation.setBackgroundResource(R.drawable.translation_background)
        }
    }

    inner class DrawingView(context: android.content.Context) : View(context) {
        private val paint = Paint().apply {
            color = Color.rgb(0, 150, 150)
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            connections.forEach { (source, target) ->
                // Tablice do przechowywania współrzędnych
                val sourceLocation = IntArray(2)
                val targetLocation = IntArray(2)
                
                // Pobierz globalne współrzędne dla obu widoków
                source.getLocationOnScreen(sourceLocation)
                target.getLocationOnScreen(targetLocation)

                // Odejmij statusbar height dla poprawnego pozycjonowania
                val statusBarHeight = getStatusBarHeight()
                
                // Oblicz punkty początkowe i końcowe linii
                val startX = sourceLocation[0] + source.width.toFloat()
                val startY = (sourceLocation[1] - statusBarHeight) + source.height.toFloat() / 2
                val endX = targetLocation[0].toFloat()
                val endY = (targetLocation[1] - statusBarHeight) + target.height.toFloat() / 2

                canvas.drawLine(startX, startY, endX, endY, paint)
            }
        }

        private fun getStatusBarHeight(): Int {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            invalidate()
        }
    }

    private fun loadLanguagesAndShowDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                languages = database.languageDao().getAllLanguages()
                withContext(Dispatchers.Main) {
                    showSetupDialog()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading languages", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Task3Activity, "Error loading languages", Toast.LENGTH_SHORT).show()
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

        roundsEditText.setText(DEFAULT_ROUNDS.toString())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Setup Task 3")
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
                                wordsWithTranslations.isEmpty() -> {
                                    errorText.text = "No translations found between selected languages"
                                    errorText.visibility = View.VISIBLE
                                    positiveButton.isEnabled = false
                                }
                                wordsWithTranslations.size < MIN_WORDS_REQUIRED -> {
                                    errorText.text = "Need at least $MIN_WORDS_REQUIRED words with translations to start"
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

            validateSetup()

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

            positiveButton.setOnClickListener {
                selectedLanguageCode = languages[targetLanguageSpinner.selectedItemPosition].code
                selectedSourceLanguageCode = languages[sourceLanguageSpinner.selectedItemPosition].code
                totalRounds = roundsEditText.text.toString().toIntOrNull() ?: DEFAULT_ROUNDS
                
                dialog.dismiss()
                loadWordsFromDatabase()
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

                availableWords = wordsWithTranslations.shuffled()
                
                withContext(Dispatchers.Main) {
                    questionIndex = 0
                    loadWordAndTranslations()
                    setViewsVisible(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading words", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Task3Activity, "Error loading words", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun loadWordAndTranslations() {
        if (questionIndex >= totalRounds) {
            showResults()
            return
        }

        // Losowo wybierz 4 RÓŻNE słowa z dostępnej puli
        val currentSet = availableWords.shuffled().take(WORDS_PER_ROUND)
        val words = listOf(word1, word2, word3, word4)
        val translations = listOf(translation1, translation2, translation3, translation4)

        // Losowo pomieszaj tłumaczenia
        val shuffledTranslations = currentSet.map { it.translations.first() }.shuffled()

        currentSet.forEachIndexed { index, wordWithTranslations ->
            words[index].apply {
                text = wordWithTranslations.sourceWord.word
                visibility = View.VISIBLE
                tag = wordWithTranslations.translations.first().word
            }
            translations[index].apply {
                text = shuffledTranslations[index].word
                visibility = View.VISIBLE
            }
        }

        // Wyczyść poprzednie połączenia i zresetuj tła
        connections.clear()
        resetWordBackgrounds()
        drawingView.invalidate()

        questionIndex++
    }

    private fun checkMatches() {
        var correctMatches = 0
        val words = listOf(word1, word2, word3, word4)

        words.forEach { word ->
            if (word.visibility == View.VISIBLE) {
                val connectedTranslation = connections[word]
                if (connectedTranslation != null && connectedTranslation.text == word.tag) {
                    correctMatches++
                }
            }
        }

        val totalVisible = words.count { it.visibility == View.VISIBLE }
        
        if (correctMatches == totalVisible) {
            Toast.makeText(this, "All correct!", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(true)
        } else {
            Toast.makeText(this, "Some matches were incorrect!", Toast.LENGTH_SHORT).show()
            ResultsManager.updateResults(false)
        }

        // Zawsze przechodzimy do następnej rundy
        loadWordAndTranslations()
    }

    private fun showResults() {
        val intent = Intent(this, ResultsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setViewsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.INVISIBLE
        word1.visibility = visibility
        word2.visibility = visibility
        word3.visibility = visibility
        word4.visibility = visibility
        translation1.visibility = visibility
        translation2.visibility = visibility
        translation3.visibility = visibility
        translation4.visibility = visibility
        buttonCheckMatch.visibility = visibility
    }
}
