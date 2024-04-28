package com.example.se_project
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Calendar
import java.util.Collections


class BarChartActivity : AppCompatActivity() {

private lateinit var chart: BarChart // Correctly declare chart as BarChart

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barchart)
        try {
        chart = findViewById<BarChart>(R.id.chart1) // Make sure this is cast correctly
        setupChart()
        getWeeklyExpenses(object : WeeklyExpensesCallback {
        override fun onCallback(weeklyExpenses: List<Float?>?) {
        try {
        weeklyExpenses?.let {
        if (it.isEmpty()) {
        Log.e("BarChartActivity", "No expense data available for the week")
        } else {
        updateChartWithData(it.filterNotNull())
        }
        } ?: Log.e("BarChartActivity", "Weekly expenses callback returned null")
        } catch (e: Exception) {
        Log.e("BarChartActivity", "Error processing weekly expenses: ${e.message}", e)
        }
        }
        })
        } catch (e: Exception) {
        Log.e("BarChartActivity", "Failed to initialize chart or fetch data: ${e.message}", e)
        }
        }


private fun setupChart() {
        chart.description.isEnabled = false
        chart.setMaxVisibleValueCount(60)
        chart.setPinchZoom(false)
        chart.setDrawBarShadow(false)
        chart.setDrawGridBackground(false)
        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.legend.isEnabled = false
        chart.animateY(1500)
        }


    private fun updateChartWithData(weeklyExpenses: List<Float>) {
        val values = ArrayList<BarEntry>()
        for (i in weeklyExpenses.indices) {
            values.add(BarEntry(i.toFloat(), weeklyExpenses[i]))
        }

        val set1: BarDataSet
        if (chart.data != null && chart.data.dataSetCount > 0) {
            set1 = chart.data.getDataSetByIndex(0) as BarDataSet
            set1.values = values
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "Weekly Expenses")
            set1.colors = ColorTemplate.VORDIPLOM_COLORS.toList()
            set1.setDrawValues(true)
            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            chart.data = data
            chart.setFitBars(true)
        }
        chart.invalidate()
    }



interface WeeklyExpensesCallback {
    fun onCallback(weeklyExpenses: List<Float?>?)
}

    private fun getWeeklyExpenses(callback: WeeklyExpensesCallback) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().uid ?: return // Handle null UID case early

        val startOfWeek: Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }

        val endOfWeek: Calendar = (startOfWeek.clone() as Calendar).apply {
            add(Calendar.DATE, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        db.collection("expenses")
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("date", Timestamp(startOfWeek.time))
            .whereLessThanOrEqualTo("date", Timestamp(endOfWeek.time))
            .get()
            .addOnSuccessListener { documents ->
                val weeklyExpenses = MutableList(7) { 0f }
                for (document in documents) {
                    document.getTimestamp("date")?.toDate()?.let { date ->
                        document.getDouble("amount")?.toFloat()?.let { amount ->
                            Calendar.getInstance().apply {
                                time = date
                                val dayOfWeek = get(Calendar.DAY_OF_WEEK) - startOfWeek.get(Calendar.DAY_OF_WEEK)
                                weeklyExpenses[(dayOfWeek + 7) % 7] += amount // Safely index within bounds
                            }
                        }
                    }
                }
                callback.onCallback(weeklyExpenses)
            }
            .addOnFailureListener { e ->
                Log.e("getWeeklyExpenses", "Error fetching weekly expenses", e)
                callback.onCallback(null) // Notify callback about the failure
            }
    }

}