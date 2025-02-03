package com.example.aplikacjemobilne.repository

import android.app.AlertDialog
import android.content.Intent
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

    override fun onResume() {
        super.onResume()
        // Odśwież listę po powrocie z edycji
        loadLanguagesAndWords()
    }

    private fun setupRecyclerView() {
        try {
            recyclerView = findViewById(R.id.recyclerViewWords)
            Log.d(TAG, "Found RecyclerView")
            
            adapter = WordListAdapter(
                onEditClick = { wordWithTranslations ->
                    val intent = Intent(this, EditWordActivity::class.java).apply {
                        putExtra(EditWordActivity.EXTRA_SOURCE_WORD_ID, wordWithTranslations.sourceWord.id)
                        putExtra(EditWordActivity.EXTRA_TARGET_WORD_ID, wordWithTranslations.translations[0].id)
                    }
                    startActivity(intent)
                },
                onDeleteClick = { wordWithTranslations ->
                    showDeleteConfirmationDialog(wordWithTranslations)
                }
            )
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

    private fun showDeleteConfirmationDialog(wordWithTranslations: WordListAdapter.WordWithTranslations) {
        AlertDialog.Builder(this)
            .setTitle("Delete Word")
            .setMessage("Are you sure you want to delete '${wordWithTranslations.sourceWord.word}' and all its translations?")
            .setPositiveButton("Delete") { _, _ ->
                deleteWord(wordWithTranslations)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWord(wordWithTranslations: WordListAdapter.WordWithTranslations) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delete translations first
                database.translationDao().deleteTranslationsForWord(wordWithTranslations.sourceWord.id)
                
                // Delete the source word and its translations
                database.wordDao().delete(wordWithTranslations.sourceWord)
                wordWithTranslations.translations.forEach { translation ->
                    database.wordDao().delete(translation)
                }

                withContext(Dispatchers.Main) {
                    adapter.removeWord(wordWithTranslations)
                    Toast.makeText(this@WordListActivity, "Word deleted successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("WordListActivity", "Error deleting word", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WordListActivity, "Error deleting word", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun Spinner.setOnItemSelectedListener(onItemSelected: (Int) -> Unit) {
        this.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                onItemSelected(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }
} 