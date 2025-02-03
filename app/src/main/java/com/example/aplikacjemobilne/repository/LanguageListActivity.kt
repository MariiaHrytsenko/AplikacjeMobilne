package com.example.aplikacjemobilne.repository

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
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

class LanguageListActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LanguageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_list)

        database = AppDatabase.getDatabase(this)
        setupViews()
        loadLanguages()
    }

    override fun onResume() {
        super.onResume()
        loadLanguages()
    }

    private fun setupViews() {
        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewLanguages)
        adapter = LanguageAdapter(
            onEditClick = { language ->
                val intent = Intent(this, AddLanguageActivity::class.java).apply {
                    putExtra("language_code", language.code)
                    putExtra("language_name", language.name)
                }
                startActivity(intent)
            },
            onDeleteClick = { language ->
                showDeleteConfirmationDialog(language)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LanguageListActivity)
            adapter = this@LanguageListActivity.adapter
        }

        // Setup Add button
        findViewById<Button>(R.id.buttonAddLanguage).setOnClickListener {
            startActivity(Intent(this, AddLanguageActivity::class.java))
        }
    }

    private fun loadLanguages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val languages = database.languageDao().getAllLanguages()
                withContext(Dispatchers.Main) {
                    adapter.setLanguages(languages)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LanguageListActivity, "Error loading languages", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(language: Language) {
        AlertDialog.Builder(this)
            .setTitle("Delete Language")
            .setMessage("Are you sure you want to delete ${language.name} (${language.code})?")
            .setPositiveButton("Delete") { _, _ ->
                deleteLanguage(language)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteLanguage(language: Language) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                database.languageDao().delete(language)
                withContext(Dispatchers.Main) {
                    adapter.removeLanguage(language)
                    Toast.makeText(this@LanguageListActivity, "Language deleted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LanguageListActivity, "Error deleting language", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 