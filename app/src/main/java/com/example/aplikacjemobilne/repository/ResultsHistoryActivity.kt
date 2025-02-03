package com.example.aplikacjemobilne.repository

import android.graphics.Color
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
    private lateinit var lineChart: LineChart
    private lateinit var adapter: ResultsAdapter
    private var currentFilter: Int = 0 // 0 means all tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_history)

        database = AppDatabase.getDatabase(this)
        
        initializeViews()
        setupSpinner()
        setupRecyclerView()
        setupChart()
        loadResults()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewResults)
        spinnerTaskFilter = findViewById(R.id.spinnerTaskFilter)
        buttonBack = findViewById(R.id.buttonBack)
        lineChart = findViewById(R.id.lineChart)

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

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun updateChart(results: List<TaskResult>) {
        if (results.isEmpty()) {
            lineChart.clear()
            return
        }

        val entries = results.mapIndexed { index, result ->
            val accuracy = if (result.totalQuestions > 0) {
                (result.correctAnswers.toFloat() / result.totalQuestions * 100)
            } else {
                0f
            }
            Entry(index.toFloat(), accuracy)
        }

        val dates = results.map { result ->
            val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
            dateFormat.format(Date(result.date))
        }

        val dataSet = LineDataSet(entries, "Accuracy %").apply {
            color = Color.rgb(65, 105, 225) // Royal Blue
            setCircleColor(Color.rgb(65, 105, 225))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            xAxis.labelRotationAngle = -45f
            notifyDataSetChanged()
            invalidate()
        }
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
                updateChart(results)
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