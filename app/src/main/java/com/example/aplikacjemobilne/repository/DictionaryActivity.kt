package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

class DictionaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)

        findViewById<View>(R.id.buttonAddWordActivity).setOnClickListener { openAddWordActivity() }
        findViewById<View>(R.id.buttonWordList).setOnClickListener { openWordListActivity() }
    }

    private fun openAddWordActivity() {
        startActivity(Intent(this, AddWordActivity::class.java))
    }

    private fun openWordListActivity() {
        startActivity(Intent(this, WordListActivity::class.java))
    }
}
