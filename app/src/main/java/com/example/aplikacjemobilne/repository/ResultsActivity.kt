package com.example.aplikacjemobilne.repository

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikacjemobilne.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class ResultsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var buttonBackToMenu: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Inicjalizacja widoków
        pieChart = findViewById(R.id.pieChart)
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu)

        setupPieChart()
        updateChartData()

        // Obsługa kliknięcia przycisku "Back to Menu"
        buttonBackToMenu.setOnClickListener {
            ResultsManager.resetResults()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupPieChart() {
        pieChart.apply {
            // Wygląd wykresu
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setDrawEntryLabels(false)

            // Animacja
            animateY(1400)

            // Legenda
            legend.apply {
                isEnabled = true
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textSize = 14f
                formSize = 16f
                xEntrySpace = 8f
                yEntrySpace = 4f
            }
        }
    }

    private fun updateChartData() {
        val correctAnswers = ResultsManager.getCorrectAnswers()
        val wrongAnswers = ResultsManager.getWrongAnswers()
        val total = correctAnswers + wrongAnswers
        val correctPercentage = if (total > 0) (correctAnswers * 100f) / total else 0f

        // Przygotowanie danych
        val entries = listOf(
            PieEntry(correctAnswers.toFloat(), "Correct"),
            PieEntry(wrongAnswers.toFloat(), "Incorrect")
        )

        // Kolory
        val colors = listOf(
            Color.rgb(76, 175, 80),  // Zielony dla poprawnych
            Color.rgb(244, 67, 54)    // Czerwony dla niepoprawnych
        )

        // Tworzenie zestawu danych
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 14f
            valueTypeface = Typeface.DEFAULT_BOLD
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(pieChart)
        }

        // Ustawienie danych
        pieChart.apply {
            data = PieData(dataSet)
            centerText = String.format("%.1f%%\nCorrect", correctPercentage)
            setCenterTextSize(20f)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            setCenterTextColor(Color.rgb(47, 79, 79))  // Ciemny niebieskozielony
            invalidate()
        }
    }
}
