package com.example.aplikacjemobilne.repository

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

// Główna aktywność do dodawania nowych słów
class AddWordActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddWordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        val editTextWord = findViewById<EditText>(R.id.editTextWord)
        val buttonConfirm = findViewById<Button>(R.id.buttonConfirm)


        buttonConfirm.setOnClickListener {
            val word = editTextWord.text.toString()

            if (word.isNotEmpty()) {
                Toast.makeText(this, "Word added: $word", Toast.LENGTH_SHORT).show()

                // Tutaj możemy dodać dodatkową logikę do zapisania słowa w bazie danych
            } else {
                // Jeśli słowo jest puste, wyświetlamy komunikat o błędzie
                Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
