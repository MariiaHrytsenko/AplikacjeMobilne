package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.buttonAddLanguageActivity).setOnClickListener { openAddLanguageActivity() }
        findViewById<View>(R.id.buttonManageLanguages).setOnClickListener { openManageLanguagesActivity() }
    }

    private fun openAddLanguageActivity() {
        startActivity(Intent(this, AddLanguageActivity::class.java))
    }

    private fun openManageLanguagesActivity() {
        startActivity(Intent(this, LanguageListActivity::class.java))
    }
}
