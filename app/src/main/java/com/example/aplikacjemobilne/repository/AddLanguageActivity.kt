package com.example.aplikacjemobilne.repository

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddLanguageActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddLanguageActivity"
    }

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_language)

        // Inicjalizacja widoków
        val editTextLanguageCode = findViewById<EditText>(R.id.editTextLanguageCode)
        val editTextLanguageName = findViewById<EditText>(R.id.editTextLanguageName)
        val buttonAddLanguage = findViewById<Button>(R.id.buttonAddLanguage)
        val textViewOutput = findViewById<TextView>(R.id.textViewOutput)

        // Inicjalizacja bazy danych
        database = AppDatabase.getDatabase(this)

        // Obsługa przycisku dodania języka
        buttonAddLanguage.setOnClickListener {
            val languageCode = editTextLanguageCode.text.toString().trim().uppercase()
            val languageName = editTextLanguageName.text.toString().trim()

            if (languageCode.isNotEmpty() && languageName.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Sprawdź czy język już istnieje
                        val existingLanguages = database.languageDao().getAllLanguages()
                        val languageExists = existingLanguages.any { 
                            it.code.equals(languageCode, ignoreCase = true) || 
                            it.name.equals(languageName, ignoreCase = true) 
                        }

                        if (languageExists) {
                            withContext(Dispatchers.Main) {
                                textViewOutput.text = "Error: Language with this code or name already exists!"
                                textViewOutput.setTextColor(ContextCompat.getColor(this@AddLanguageActivity, android.R.color.holo_red_dark))
                            }
                            return@launch
                        }

                        val newLanguage = Language(code = languageCode, name = languageName)
                        database.languageDao().insert(newLanguage)
                        val allLanguages = database.languageDao().getAllLanguages()
                        val output = allLanguages.joinToString(separator = "\n") { "${it.code}: ${it.name}" }

                        withContext(Dispatchers.Main) {
                            textViewOutput.text = "Languages in DB:\n$output"
                            textViewOutput.setTextColor(ContextCompat.getColor(this@AddLanguageActivity, android.R.color.black))
                            // Wyczyść pola po udanym dodaniu
                            editTextLanguageCode.text.clear()
                            editTextLanguageName.text.clear()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error inserting language", e)
                        withContext(Dispatchers.Main) {
                            textViewOutput.text = "Error: ${e.message}"
                            textViewOutput.setTextColor(ContextCompat.getColor(this@AddLanguageActivity, android.R.color.holo_red_dark))
                        }
                    }
                }
            } else {
                textViewOutput.text = "Please enter both code and name!"
                textViewOutput.setTextColor(ContextCompat.getColor(this@AddLanguageActivity, android.R.color.holo_red_dark))
            }
        }

        // Wyświetl istniejące języki przy starcie aplikacji
        loadLanguages(textViewOutput)
    }

    private fun loadLanguages(textView: TextView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allLanguages = database.languageDao().getAllLanguages()
                val output = allLanguages.joinToString(separator = "\n") { "${it.code}: ${it.name}" }

                withContext(Dispatchers.Main) {
                    textView.text = if (allLanguages.isNotEmpty()) {
                        "Added languages:\n$output"
                    } else {
                        "No languages added."
                    }
                    textView.setTextColor(ContextCompat.getColor(this@AddLanguageActivity, android.R.color.black))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading languages", e)
                withContext(Dispatchers.Main) {
                    textView.text = "Error loading data: ${e.message}"
                    textView.setTextColor(ContextCompat.getColor(this@AddLanguageActivity, android.R.color.holo_red_dark))
                }
            }
        }
    }
}