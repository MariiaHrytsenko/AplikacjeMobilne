package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.buttonTask).setOnClickListener { openTaskActivity() }
        findViewById<View>(R.id.buttonDictionary).setOnClickListener { openDictionaryActivity() }
        findViewById<View>(R.id.buttonViewHistory).setOnClickListener { openResultsHistoryActivity() }
        findViewById<View>(R.id.buttonSettings).setOnClickListener { openSettingsActivity() }
    }

    private fun openTaskActivity() {
        startActivity(Intent(this, TaskActivity::class.java))
    }

    private fun openDictionaryActivity() {
        startActivity(Intent(this, DictionaryActivity::class.java))
    }

    private fun openResultsHistoryActivity() {
        startActivity(Intent(this, ResultsHistoryActivity::class.java))
    }

    private fun openSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}
