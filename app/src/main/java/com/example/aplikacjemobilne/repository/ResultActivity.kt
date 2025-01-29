package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class ResultsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var buttonBackToMenu: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Inicjalizacja widoków
        pieChart = findViewById(R.id.pieChart)
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu)

        // Pobieranie danych z ResultsManager
        val correctAnswers = ResultsManager.getCorrectAnswers()
        val wrongAnswers = ResultsManager.getWrongAnswers()

        // Przygotowanie danych do wykresu kołowego
        val pieEntries = listOf(
            PieEntry(correctAnswers.toFloat(), "Correct"),
            PieEntry(wrongAnswers.toFloat(), "Incorrect")
        )

        // Tworzenie zestawu danych do wykresu
        val pieDataSet = PieDataSet(pieEntries, "Results")
        pieDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val pieData = PieData(pieDataSet)

        pieChart.data = pieData
        pieChart.centerText = "${(correctAnswers * 100) / (correctAnswers + wrongAnswers)}% Correct"
        pieChart.setCenterTextSize(20f)
        pieChart.invalidate()
        pieChart.setDrawHoleEnabled(false)

        // Obsługa kliknięcia przycisku "Back to Menu"
        buttonBackToMenu.setOnClickListener {
            ResultsManager.resetResults()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
