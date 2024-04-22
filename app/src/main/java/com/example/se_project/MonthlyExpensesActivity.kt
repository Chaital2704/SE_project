package com.example.se_project

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MonthlyExpensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_expenses)

        updateExpensesDisplay()

        findViewById<Button>(R.id.btnSetLimits).setOnClickListener {
            showSetLimitsDialog()
        }
    }

    private fun showSetLimitsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_limits, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setTitle("Set Expense Limits")

        builder.setPositiveButton("Save") { _, _ ->
            val sharedPreferencesEditor = getSharedPreferences("ExpenseLimits", Context.MODE_PRIVATE).edit()

            dialogView.findViewById<EditText>(R.id.etHousingLimit).text.toString().toFloatOrNull()?.let {
                sharedPreferencesEditor.putFloat("Housing", it)
            }
            dialogView.findViewById<EditText>(R.id.etUtilitiesLimit).text.toString().toFloatOrNull()?.let {
                sharedPreferencesEditor.putFloat("Utilities", it)
            }
            dialogView.findViewById<EditText>(R.id.etFoodLimit).text.toString().toFloatOrNull()?.let {
                sharedPreferencesEditor.putFloat("Food", it)
            }
            dialogView.findViewById<EditText>(R.id.etTransportationLimit).text.toString().toFloatOrNull()?.let {
                sharedPreferencesEditor.putFloat("Transportation", it)
            }
            dialogView.findViewById<EditText>(R.id.etPersonalLimit).text.toString().toFloatOrNull()?.let {
                sharedPreferencesEditor.putFloat("Personal", it)
            }

            sharedPreferencesEditor.apply()

            // Refresh the expenses display to reflect new limits
            updateExpensesDisplay()
        }

        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }





    private fun updateExpensesDisplay() {
        val categories = arrayOf("Housing", "Utilities", "Food", "Transportation", "Personal")
        categories.forEach { category ->
            getMonthlyExpensesForCategory(category) { total ->
                runOnUiThread {
                    val limit = getUserDefinedLimit(category)
                    val categoryId = resources.getIdentifier(category + "Expenses", "id", packageName)
                    findViewById<TextView>(categoryId)?.let { textView ->
                        val text = "$category: $total INR"
                        val spannable = SpannableString(text)
                        val start = text.indexOf("$total")
                        val end = start + "$total".length

                        // Apply color span to only part of the text
                        spannable.setSpan(ForegroundColorSpan(if (total > limit) Color.RED else Color.GREEN), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        textView.text = spannable
                    }

                }
            }
        }
    }

    private fun getMonthlyExpensesForCategory(category: String, callback: (Float) -> Unit) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfMonth = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        FirebaseFirestore.getInstance().collection("expenses")
            .whereEqualTo("uid", uid)
            .whereEqualTo("category", category)
            .whereGreaterThanOrEqualTo("time", startOfMonth)
            .whereLessThanOrEqualTo("time", endOfMonth)
            .get()
            .addOnSuccessListener { documents ->
                var total = 0f
                documents.forEach { document ->
                    val expense = document.toObject(ExpenseModule::class.java)
                    total += expense.amount
                }
                callback(total)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching expenses: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getUserDefinedLimit(category: String): Float {
        val sharedPreferences = getSharedPreferences("ExpenseLimits", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat(category, 0f) // Default to 0 if not set
    }
}
