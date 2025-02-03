package com.example.aplikacjemobilne.repository

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.Language
import com.example.aplikacjemobilne.data.Translation
import com.example.aplikacjemobilne.data.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Główna aktywność do dodawania nowych słów
class AddWordActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddWordActivity"
    }

    private lateinit var database: AppDatabase
    private lateinit var spinnerSourceLanguage: Spinner
    private lateinit var spinnerTargetLanguage: Spinner
    private lateinit var editTextWord: EditText
    private lateinit var editTextTranslation: EditText
    private lateinit var buttonAddTranslation: Button
    private lateinit var buttonConfirm: Button
    private lateinit var recyclerViewTranslations: RecyclerView
    private lateinit var translationAdapter: TranslationAdapter
    private var languages: List<Language> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        // Inicjalizacja bazy danych
        database = AppDatabase.getDatabase(this)

        // Inicjalizacja widoków
        initializeViews()
        
        // Ustawienie RecyclerView
        setupRecyclerView()
        
        // Załadowanie języków
        loadLanguages()

        // Obsługa przycisków
        setupButtonListeners()
    }

    private fun initializeViews() {
        spinnerSourceLanguage = findViewById(R.id.spinnerSourceLanguage)
        spinnerTargetLanguage = findViewById(R.id.spinnerTargetLanguage)
        editTextWord = findViewById(R.id.editTextWord)
        editTextTranslation = findViewById(R.id.editTextTranslation)
        buttonAddTranslation = findViewById(R.id.buttonAddTranslation)
        buttonConfirm = findViewById(R.id.buttonConfirm)
        recyclerViewTranslations = findViewById(R.id.recyclerViewTranslations)
    }

    private fun setupRecyclerView() {
        translationAdapter = TranslationAdapter()
        recyclerViewTranslations.apply {
            layoutManager = LinearLayoutManager(this@AddWordActivity)
            adapter = translationAdapter
        }
    }

    private fun loadLanguages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                languages = database.languageDao().getAllLanguages()
                
                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(
                        this@AddWordActivity,
                        android.R.layout.simple_spinner_item,
                        languages.map { it.name }
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }

                    spinnerSourceLanguage.adapter = adapter
                    spinnerTargetLanguage.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading languages", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddWordActivity, "Error loading languages", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupButtonListeners() {
        buttonAddTranslation.setOnClickListener {
            addTranslation()
        }

        buttonConfirm.setOnClickListener {
            saveWordWithTranslations()
        }
    }

    private fun addTranslation() {
        val sourceWord = editTextWord.text.toString().trim()
        val targetWord = editTextTranslation.text.toString().trim()

        if (sourceWord.isEmpty() || targetWord.isEmpty()) {
            Toast.makeText(this, "Please enter both words", Toast.LENGTH_SHORT).show()
            return
        }

        val sourceLanguage = languages[spinnerSourceLanguage.selectedItemPosition]
        val targetLanguage = languages[spinnerTargetLanguage.selectedItemPosition]

        if (sourceLanguage.code == targetLanguage.code) {
            Toast.makeText(this, "Please select different languages", Toast.LENGTH_SHORT).show()
            return
        }

        // Sprawdź czy dokładnie ta sama kombinacja słowa źródłowego i tłumaczenia już istnieje
        val existingTranslations = translationAdapter.getTranslations()
        if (existingTranslations.any { 
            it.sourceWord == sourceWord && 
            it.targetWord == targetWord && 
            it.sourceLanguage == sourceLanguage.name && 
            it.targetLanguage == targetLanguage.name 
        }) {
            Toast.makeText(this, "This translation already exists in current session!", Toast.LENGTH_SHORT).show()
            return
        }

        // Dodaj tłumaczenie do listy
        translationAdapter.addTranslation(
            TranslationAdapter.TranslationItem(
                sourceLanguage = sourceLanguage.name,
                targetLanguage = targetLanguage.name,
                sourceWord = sourceWord,
                targetWord = targetWord
            )
        )
        editTextTranslation.text.clear()
    }

    private fun saveWordWithTranslations() {
        val translations = translationAdapter.getTranslations()
        if (translations.isEmpty()) {
            Toast.makeText(this, "Please add at least one translation", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Sprawdź przed zapisem czy słowa nie zostały dodane w międzyczasie
                val allWords = database.wordDao().getAllWords()
                
                // Sprawdź wszystkie słowa przed zapisem
                for (translation in translations) {
                    val sourceLanguage = languages.first { it.name == translation.sourceLanguage }
                    val targetLanguage = languages.first { it.name == translation.targetLanguage }

                    // Sprawdź słowo źródłowe
                    val sourceWordExists = allWords.any { 
                        it.word.equals(translation.sourceWord, ignoreCase = true) && 
                        it.languageCode == sourceLanguage.code 
                    }

                    if (sourceWordExists) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddWordActivity, 
                                "Word '${translation.sourceWord}' already exists in ${sourceLanguage.name}!", 
                                Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    // Sprawdź tłumaczenie
                    val targetWordExists = allWords.any { 
                        it.word.equals(translation.targetWord, ignoreCase = true) && 
                        it.languageCode == targetLanguage.code 
                    }

                    if (targetWordExists) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddWordActivity, 
                                "Translation '${translation.targetWord}' already exists in ${targetLanguage.name}!", 
                                Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                }

                // Jeśli wszystko OK, zapisz każde tłumaczenie osobno
                for (translationItem in translations) {
                    val sourceLanguage = languages.first { it.name == translationItem.sourceLanguage }
                    val targetLanguage = languages.first { it.name == translationItem.targetLanguage }

                    // Zapisz słowo źródłowe
                    val sourceWord = Word(
                        word = translationItem.sourceWord,
                        languageCode = sourceLanguage.code
                    )
                    val sourceWordId = database.wordDao().insert(sourceWord)

                    // Zapisz słowo docelowe
                    val targetWord = Word(
                        word = translationItem.targetWord,
                        languageCode = targetLanguage.code
                    )
                    val targetWordId = database.wordDao().insert(targetWord)

                    // Zapisz relację tłumaczenia
                    val translation = Translation(
                        wordId = sourceWordId.toInt(),
                        translatedWordId = targetWordId.toInt()
                    )
                    database.translationDao().insert(translation)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddWordActivity, "All translations saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving translations", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddWordActivity, "Error saving translations", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}