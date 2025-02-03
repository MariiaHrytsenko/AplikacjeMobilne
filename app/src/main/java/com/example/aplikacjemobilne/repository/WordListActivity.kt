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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordListActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "WordListActivity"
    }

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordListAdapter
    private lateinit var spinnerLanguageFilter: Spinner
    private var allWordsWithTranslations: List<WordListAdapter.WordWithTranslations> = listOf()
    private var languages: List<Language> = listOf()
    private var selectedLanguageCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_word_list)
            Log.d(TAG, "Layout set successfully")

            database = AppDatabase.getDatabase(this)
            Log.d(TAG, "Database initialized")
            
            setupRecyclerView()
            setupSpinner()
            Log.d(TAG, "Views setup completed")
            
            loadLanguagesAndWords()
            Log.d(TAG, "Started loading data")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing activity: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        try {
            recyclerView = findViewById(R.id.recyclerViewWords)
            Log.d(TAG, "Found RecyclerView")
            
            adapter = WordListAdapter()
            Log.d(TAG, "Created adapter")
            
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@WordListActivity)
                adapter = this@WordListActivity.adapter
            }
            Log.d(TAG, "RecyclerView configuration completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
            throw e
        }
    }

    private fun setupSpinner() {
        spinnerLanguageFilter = findViewById(R.id.spinnerLanguageFilter)
        spinnerLanguageFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    selectedLanguageCode = null
                } else {
                    selectedLanguageCode = languages[position - 1].code
                }
                filterWords()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedLanguageCode = null
                filterWords()
            }
        }
    }

    private fun loadLanguagesAndWords() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Załaduj języki
                languages = database.languageDao().getAllLanguages()
                
                // Przygotuj listę dla spinnera
                val languageNames = mutableListOf("All languages")
                languageNames.addAll(languages.map { "${it.code}: ${it.name}" })

                withContext(Dispatchers.Main) {
                    val spinnerAdapter = ArrayAdapter(
                        this@WordListActivity,
                        android.R.layout.simple_spinner_item,
                        languageNames
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    spinnerLanguageFilter.adapter = spinnerAdapter
                }

                // Załaduj słowa
                Log.d(TAG, "Starting to load words")
                val allWords = database.wordDao().getAllWords()
                Log.d(TAG, "Loaded ${allWords.size} words")
                
                val allTranslations = database.translationDao().getAllTranslations()
                Log.d(TAG, "Loaded ${allTranslations.size} translations")

                allWordsWithTranslations = allWords.map { sourceWord ->
                    Log.d(TAG, "Processing word: ${sourceWord.word}")
                    val translationIds = allTranslations
                        .filter { it.wordId == sourceWord.id }
                        .map { it.translatedWordId }
                    
                    val translations = allWords.filter { word -> 
                        word.id in translationIds
                    }
                    
                    WordListAdapter.WordWithTranslations(sourceWord, translations)
                }.filter { it.translations.isNotEmpty() }

                Log.d(TAG, "Created ${allWordsWithTranslations.size} word groups")

                withContext(Dispatchers.Main) {
                    filterWords()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WordListActivity, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun filterWords() {
        val filteredWords = if (selectedLanguageCode == null) {
            allWordsWithTranslations
        } else {
            allWordsWithTranslations.filter { wordWithTranslations ->
                wordWithTranslations.sourceWord.languageCode == selectedLanguageCode ||
                wordWithTranslations.translations.any { it.languageCode == selectedLanguageCode }
            }
        }

        if (filteredWords.isEmpty()) {
            Toast.makeText(this, "No words found for selected language", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "No words to display for language: $selectedLanguageCode")
        } else {
            adapter.setWords(filteredWords)
            Log.d(TAG, "Displayed ${filteredWords.size} words for language: $selectedLanguageCode")
        }
    }
} 