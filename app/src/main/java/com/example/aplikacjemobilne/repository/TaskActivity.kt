package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        findViewById<View>(R.id.buttonTask1).setOnClickListener { openTask1Activity() }
        findViewById<View>(R.id.buttonTask2).setOnClickListener { openTask2Activity() }
        findViewById<View>(R.id.buttonTask3).setOnClickListener { openTask3Activity() }
    }

    private fun openTask1Activity() {
        startActivity(Intent(this, Task1Activity::class.java))
    }

    private fun openTask2Activity() {
        startActivity(Intent(this, Task2Activity::class.java))
    }

    private fun openTask3Activity() {
        startActivity(Intent(this, Task3Activity::class.java))
    }
}
