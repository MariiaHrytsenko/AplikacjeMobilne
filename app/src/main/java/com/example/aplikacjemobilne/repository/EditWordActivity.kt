package com.example.aplikacjemobilne.repository

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditWordActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SOURCE_WORD_ID = "source_word_id"
        const val EXTRA_TARGET_WORD_ID = "target_word_id"
    }

    private lateinit var database: AppDatabase
    private lateinit var editTextSourceWord: EditText
    private lateinit var editTextTargetWord: EditText
    private lateinit var buttonSave: Button
    private var sourceWordId: Int = 0
    private var targetWordId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_word)

        sourceWordId = intent.getIntExtra(EXTRA_SOURCE_WORD_ID, -1)
        targetWordId = intent.getIntExtra(EXTRA_TARGET_WORD_ID, -1)

        if (sourceWordId == -1 || targetWordId == -1) {
            Toast.makeText(this, "Invalid word IDs", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = AppDatabase.getDatabase(this)
        initializeViews()
        loadWords()
    }

    private fun initializeViews() {
        editTextSourceWord = findViewById(R.id.editTextSourceWord)
        editTextTargetWord = findViewById(R.id.editTextTargetWord)
        buttonSave = findViewById(R.id.buttonSave)

        buttonSave.setOnClickListener {
            saveWords()
        }
    }

    private fun loadWords() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sourceWord = database.wordDao().getWordById(sourceWordId)
                val targetWord = database.wordDao().getWordById(targetWordId)

                withContext(Dispatchers.Main) {
                    editTextSourceWord.setText(sourceWord.word)
                    editTextTargetWord.setText(targetWord.word)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditWordActivity, "Error loading words", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun saveWords() {
        val sourceWord = editTextSourceWord.text.toString().trim()
        val targetWord = editTextTargetWord.text.toString().trim()

        if (sourceWord.isEmpty() || targetWord.isEmpty()) {
            Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update source word
                database.wordDao().update(Word(
                    id = sourceWordId,
                    word = sourceWord,
                    languageCode = database.wordDao().getWordById(sourceWordId).languageCode
                ))

                // Update target word
                database.wordDao().update(Word(
                    id = targetWordId,
                    word = targetWord,
                    languageCode = database.wordDao().getWordById(targetWordId).languageCode
                ))

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditWordActivity, "Changes saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditWordActivity, "Error saving changes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 