package com.example.se_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.se_project.databinding.ActivityAddExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import java.util.Calendar

class AddExpense : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private var type: String? = null // Corrected variable declaration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        type = intent.getStringExtra("type") // Corrected method to get intent extras

        if (type == "Income") {
            binding.incomeRadio.isChecked = true
        } else {
            binding.expenseRadio.isChecked = true
        }

        // Set up click listeners with Kotlin syntax
        binding.incomeRadio.setOnClickListener {
            type = "Income"
        }

        binding.expenseRadio.setOnClickListener {
            type = "Expense"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveExpense -> {
                createExpense()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createExpense() {
        val expenseId = UUID.randomUUID().toString()
        val amount = binding.amount.text.toString().trim()
        val note = binding.note.text.toString()
        val category = binding.category.text.toString()
        val incomeChecked = binding.incomeRadio.isChecked

        if (incomeChecked) {
            type = "Income"
        } else {
            type = "Expense"
        }

        if (amount.isEmpty()) {
            binding.amount.error = "Empty"
            return
        }

        val amountLong = amount.toLongOrNull() ?: return // Handle number format exception
        val currentTimeMillis = Calendar.getInstance().timeInMillis
        val userId = FirebaseAuth.getInstance().uid

        // Check if userId is null before proceeding
        if (userId == null) {
            // Handle the error, e.g., prompt the user to log in
            Toast.makeText(this, "User must be logged in to save an expense.", Toast.LENGTH_SHORT).show()
            return
        }

        // Assuming ExpenseModule is defined correctly and userId is required to be non-null
        val expenseModule = ExpenseModule(expenseId, note, category, type, amountLong, currentTimeMillis, userId)

        FirebaseFirestore.getInstance()
            .collection("expenses")
            .document(expenseId)
            .set(expenseModule)
            .addOnSuccessListener {
                finish() // Close the activity on successful upload
            }
            .addOnFailureListener { e ->
                // Handle the error, e.g., show a toast message
                Toast.makeText(this, "Failed to save expense: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}
