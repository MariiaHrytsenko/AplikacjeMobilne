package com.example.aplikacjemobilne.repository

import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aplikacjemobilne.R

class Task3Activity : AppCompatActivity() {

    private lateinit var word1: TextView
    private lateinit var word2: TextView
    private lateinit var word3: TextView
    private lateinit var translation1: TextView
    private lateinit var translation2: TextView
    private lateinit var translation3: TextView
    private lateinit var buttonCheckMatch: Button

    private var correctMatches = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_3)


        word1 = findViewById(R.id.word1)
        word2 = findViewById(R.id.word2)
        word3 = findViewById(R.id.word3)
        translation1 = findViewById(R.id.translation1)
        translation2 = findViewById(R.id.translation2)
        translation3 = findViewById(R.id.translation3)
        buttonCheckMatch = findViewById(R.id.buttonCheckMatch)


        setupDragAndDrop(translation1)
        setupDragAndDrop(translation2)
        setupDragAndDrop(translation3)

        word1.setOnDragListener { v, event -> onDragEvent(event, word1) }
        word2.setOnDragListener { v, event -> onDragEvent(event, word2) }
        word3.setOnDragListener { v, event -> onDragEvent(event, word3) }

        buttonCheckMatch.setOnClickListener { checkMatches() }
    }

    private fun setupDragAndDrop(view: TextView) {
        view.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                val dragData = android.content.ClipData.newPlainText("", "")
                val dragShadowBuilder = View.DragShadowBuilder(view)
                v.startDragAndDrop(dragData, dragShadowBuilder, null, 0)
                true
            } else {
                false
            }
        }
    }

    private fun onDragEvent(event: DragEvent, targetWord: TextView): Boolean {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as View
                draggedView.x = event.x - draggedView.width / 2
                draggedView.y = event.y - draggedView.height / 2
                draggedView.visibility = View.VISIBLE
            }
        }
        return true
    }

    private fun checkMatches() {
        var correctMatches = 0

        if (word1.text == translation1.text) correctMatches++
        if (word2.text == translation2.text) correctMatches++
        if (word3.text == translation3.text) correctMatches++

        if (correctMatches == 3) {
            Toast.makeText(this, "All correct!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Try again! You matched $correctMatches out of 3", Toast.LENGTH_LONG).show()
        }
    }
}
