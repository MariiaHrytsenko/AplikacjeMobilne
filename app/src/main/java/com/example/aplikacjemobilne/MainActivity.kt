package com.example.aplikacjemobilne

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja widoków
        val editTextLanguageCode = findViewById<EditText>(R.id.editTextLanguageCode)
        val editTextLanguageName = findViewById<EditText>(R.id.editTextLanguageName)
        val buttonAddLanguage = findViewById<Button>(R.id.buttonAddLanguage)
        val textViewOutput = findViewById<TextView>(R.id.textViewOutput)

        // Inicjalizacja bazy danych
        database = AppDatabase.getDatabase(this)

        // Obsługa przycisku dodania języka
        buttonAddLanguage.setOnClickListener {
            val languageCode = editTextLanguageCode.text.toString().trim()
            val languageName = editTextLanguageName.text.toString().trim()

            if (languageCode.isNotEmpty() && languageName.isNotEmpty()) {
                val newLanguage = Language(code = languageCode, name = languageName)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        database.languageDao().insert(newLanguage)
                        val allLanguages = database.languageDao().getAllLanguages()

                        val output = allLanguages.joinToString(separator = "\n") { "${it.code}: ${it.name}" }

                        Log.d(TAG, "Languages in database: $output")

                        // Zaktualizuj widok na głównym wątku
                        runOnUiThread {
                            textViewOutput.text = "Languages in DB:\n$output"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error inserting language", e)
                        runOnUiThread {
                            textViewOutput.text = "Error: ${e.message}"
                        }
                    }
                }
            } else {
                textViewOutput.text = "Please enter both code and name!"
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

                Log.d(TAG, "Existing languages loaded: $output")

                // Zaktualizuj widok na głównym wątku
                runOnUiThread {
                    textView.text = if (allLanguages.isNotEmpty()) {
                        "Languages in DB:\n$output"
                    } else {
                        "No languages in DB."
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading languages", e)
                runOnUiThread {
                    textView.text = "Error loading data: ${e.message}"
                }
            }
        }
    }
}