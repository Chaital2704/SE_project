package com.example.se_project

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Expense(val day: String, val amount: Double)

class NewBarChartActivity : AppCompatActivity(), OnChartValueSelectedListener {
    private lateinit var barChart: BarChart
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var expensesRecyclerView: RecyclerView
    private var expensesList: List<Expense> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newchart)

        barChart = findViewById<BarChart>(R.id.chart)
        expensesRecyclerView = findViewById<RecyclerView>(R.id.expense_list)
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter()
        expensesRecyclerView.adapter = expenseAdapter
        loadExpenses()
        barChart.setOnChartValueSelectedListener(this)
    }

    private fun setupBarChart(expenses: List<Expense>) {
        expensesList = expenses // Store expenses for use in onValueSelected
        val entries = expenses.mapIndexed { index, expense -> BarEntry(index.toFloat(), expense.amount.toFloat()) }
        val dataSet = BarDataSet(entries, "")
        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(expenses.map { it.day })
        barChart.xAxis.position = XAxis.XAxisPosition.TOP
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawLabels(false)
        barChart.xAxis.isGranularityEnabled = true
        val yAxis = barChart.axisLeft
        yAxis.axisMaximum = entries.maxOf { it.y } * 1.1f // Scale Y-axis by 10% above max value for better visibility
        yAxis.axisMinimum = 0f
        yAxis.setDrawGridLines(true)
        barChart.axisRight.isEnabled = false
        barChart.invalidate() // refresh the chart with updated data
    }

    private fun loadExpenses() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val allDays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            .associateWith { Expense(it, 0.0) }.toMutableMap()

        FirebaseFirestore.getInstance().collection("expenses")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                result.documents
                    .mapNotNull { it.toObject(ExpenseModule::class.java) }
                    .groupBy {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it.time
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
                    }
                    .forEach { (day, expenses) ->
                        allDays[day] = Expense(day, expenses.sumOf { it.amount.toDouble() })
                    }

                val expenses = allDays.values.toList()
                expenseAdapter.setExpenses(expenses)
                setupBarChart(expenses)
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        e?.let {
            val day = expensesList[it.x.toInt()].day
            Toast.makeText(this, "Day selected: $day", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected() {
        Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show()
    }
}

class ExpenseAdapter(private var expenses: List<Expense> = listOf()) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {
    fun setExpenses(expenses: List<Expense>) {
        this.expenses = expenses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(expense: Expense) {
            itemView.findViewById<TextView>(R.id.day).text = "${expense.day}:"
            itemView.findViewById<TextView>(R.id.amount).text = String.format(Locale.getDefault(), "%,.2f", expense.amount)
        }
    }
}
