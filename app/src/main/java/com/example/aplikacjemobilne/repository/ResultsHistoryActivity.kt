package com.example.aplikacjemobilne.repository

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjemobilne.R
import com.example.aplikacjemobilne.data.AppDatabase
import com.example.aplikacjemobilne.data.TaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ResultsHistoryActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerTaskFilter: Spinner
    private lateinit var buttonBack: Button
    private lateinit var adapter: ResultsAdapter
    private var currentFilter: Int = 0 // 0 means all tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_history)

        database = AppDatabase.getDatabase(this)
        
        initializeViews()
        setupSpinner()
        setupRecyclerView()
        loadResults()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewResults)
        spinnerTaskFilter = findViewById(R.id.spinnerTaskFilter)
        buttonBack = findViewById(R.id.buttonBack)

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSpinner() {
        val items = listOf("All Tasks", "Task 1", "Task 2", "Task 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTaskFilter.adapter = adapter

        spinnerTaskFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = position
                loadResults()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = ResultsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadResults() {
        CoroutineScope(Dispatchers.IO).launch {
            val results = if (currentFilter == 0) {
                database.taskResultDao().getAllResults()
            } else {
                database.taskResultDao().getResultsForTask(currentFilter)
            }

            withContext(Dispatchers.Main) {
                adapter.submitList(results)
            }
        }
    }

    inner class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {
        private var results: List<TaskResult> = listOf()

        fun submitList(newResults: List<TaskResult>) {
            results = newResults
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_result_history, parent, false)
            return ResultViewHolder(view)
        }

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.bind(results[position])
        }

        override fun getItemCount() = results.size

        inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textViewTaskName: TextView = itemView.findViewById(R.id.textViewTaskName)
            private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
            private val textViewCorrect: TextView = itemView.findViewById(R.id.textViewCorrect)
            private val textViewWrong: TextView = itemView.findViewById(R.id.textViewWrong)
            private val textViewAccuracy: TextView = itemView.findViewById(R.id.textViewAccuracy)

            fun bind(result: TaskResult) {
                textViewTaskName.text = "Task ${result.taskNumber}"
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val date = Date(result.date)
                textViewDate.text = dateFormat.format(date)
                
                textViewCorrect.text = "Correct: ${result.correctAnswers}"
                textViewWrong.text = "Wrong: ${result.wrongAnswers}"
                
                val accuracy = if (result.totalQuestions > 0) {
                    (result.correctAnswers.toFloat() / result.totalQuestions * 100).toInt()
                } else {
                    0
                }
                textViewAccuracy.text = "$accuracy% accuracy"
            }
        }
    }
} 