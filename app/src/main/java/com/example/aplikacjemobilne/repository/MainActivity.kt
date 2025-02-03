package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonTask1 = findViewById<Button>(R.id.buttonTask1)
        val buttonTask2 = findViewById<Button>(R.id.buttonTask2)
        val buttonTask3 = findViewById<Button>(R.id.buttonTask3)
        val buttonAddWordActivity = findViewById<Button>(R.id.buttonAddWordActivity)
        val buttonAddLanguageActivity = findViewById<Button>(R.id.buttonAddLanguageActivity)
        val buttonWordList = findViewById<Button>(R.id.buttonWordList)
        val buttonViewHistory = findViewById<Button>(R.id.buttonViewHistory)
        val buttonManageLanguages = findViewById<Button>(R.id.buttonManageLanguages)

        buttonTask1.setOnClickListener {
            val intent = Intent(this, Task1Activity::class.java)
            startActivity(intent)
        }

        buttonTask2.setOnClickListener {
            val intent = Intent(this, Task2Activity::class.java)
            startActivity(intent)
        }

        buttonTask3.setOnClickListener {
            val intent = Intent(this, Task3Activity::class.java)
            startActivity(intent)
        }

        buttonAddWordActivity.setOnClickListener {
            val intent = Intent(this, AddWordActivity::class.java)
            startActivity(intent)
        }

        buttonAddLanguageActivity.setOnClickListener {
            val intent = Intent(this, AddLanguageActivity::class.java)
            startActivity(intent)
        }

        buttonWordList.setOnClickListener {
            startActivity(Intent(this, WordListActivity::class.java))
        }

        buttonViewHistory.setOnClickListener {
            startActivity(Intent(this, ResultsHistoryActivity::class.java))
        }

        buttonManageLanguages.setOnClickListener {
            startActivity(Intent(this, LanguageListActivity::class.java))
        }
    }
}
