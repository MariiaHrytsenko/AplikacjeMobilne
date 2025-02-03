package com.example.aplikacjemobilne.repository

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddLanguageActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var editTextLanguageCode: EditText
    private lateinit var editTextLanguageName: EditText
    private lateinit var buttonSave: Button
    private var isEditMode = false
    private var originalCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_language)

        database = AppDatabase.getDatabase(this)
        initializeViews()
        checkForEditMode()
    }

    private fun initializeViews() {
        editTextLanguageCode = findViewById(R.id.editTextLanguageCode)
        editTextLanguageName = findViewById(R.id.editTextLanguageName)
        buttonSave = findViewById(R.id.buttonAddLanguage)

        buttonSave.setOnClickListener {
            saveLanguage()
        }
    }

    private fun checkForEditMode() {
        val languageCode = intent.getStringExtra("language_code")
        val languageName = intent.getStringExtra("language_name")

        if (languageCode != null && languageName != null) {
            isEditMode = true
            originalCode = languageCode
            editTextLanguageCode.setText(languageCode)
            editTextLanguageName.setText(languageName)
            buttonSave.text = "Save Changes"
            title = "Edit Language"
        } else {
            isEditMode = false
            buttonSave.text = "Add Language"
            title = "Add New Language"
        }
    }

    private fun saveLanguage() {
        val languageCode = editTextLanguageCode.text.toString().trim().uppercase()
        val languageName = editTextLanguageName.text.toString().trim()

        if (languageCode.isEmpty() || languageName.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Sprawdź czy język już istnieje
                val existingLanguages = database.languageDao().getAllLanguages()
                val languageExists = existingLanguages.any { 
                    (it.code.equals(languageCode, ignoreCase = true) && (!isEditMode || it.code != originalCode)) || 
                    (it.name.equals(languageName, ignoreCase = true) && (!isEditMode || it.code != originalCode))
                }

                if (languageExists) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddLanguageActivity, 
                            "Language with this code or name already exists!", 
                            Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Jeśli edytujemy, najpierw usuń stary język
                if (isEditMode) {
                    val oldLanguage = existingLanguages.find { it.code == originalCode }
                    if (oldLanguage != null) {
                        database.languageDao().delete(oldLanguage)
                    }
                }

                // Dodaj nowy język
                val newLanguage = Language(code = languageCode, name = languageName)
                database.languageDao().insert(newLanguage)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddLanguageActivity, 
                        if (isEditMode) "Language updated successfully" else "Language added successfully", 
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("AddLanguageActivity", "Error saving language", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddLanguageActivity, "Error saving language", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}