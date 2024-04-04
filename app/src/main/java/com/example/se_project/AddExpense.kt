package com.example.se_project

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.se_project.databinding.ActivityAddExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddExpense : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private var expenseId: String? = null
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        expenseId = intent.getStringExtra("expenseId")
        if (expenseId != null) {
            loadExpenseData(expenseId!!)
        } else {
            type = intent.getStringExtra("type")
            updateTypeRadioButton()
        }

        binding.incomeRadio.setOnClickListener { type = "Income" }
        binding.expenseRadio.setOnClickListener { type = "Expense" }
    }

    private fun updateTypeRadioButton() {
        when (type) {
            "Income" -> binding.incomeRadio.isChecked = true
            "Expense" -> binding.expenseRadio.isChecked = true
        }
    }

    private fun loadExpenseData(expenseId: String) {
        FirebaseFirestore.getInstance().collection("expenses").document(expenseId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(ExpenseModule::class.java)?.let { expense ->
                    with(binding) {
                        amount.setText(expense.amount.toString())
                        note.setText(expense.note)
                        category.setText(expense.category)
                        if (expense.type == "Income") incomeRadio.isChecked = true
                        else expenseRadio.isChecked = true
                    }
                } ?: Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveExpense -> {
                saveExpense()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveExpense() {
        if (validateInput()) {
            if (expenseId == null) createExpense() else updateExpense(expenseId!!)
        }
    }

    private fun validateInput(): Boolean {
        with(binding) {
            if (TextUtils.isEmpty(amount.text.toString())) {
                amount.error = "Please enter an amount"
                return false
            }
            if (TextUtils.isEmpty(note.text.toString())) {
                note.error = "Please enter a note"
                return false
            }
            if (TextUtils.isEmpty(category.text.toString())) {
                category.error = "Please enter a category"
                return false
            }
        }
        return true
    }

    private fun createExpense() {
        val newExpenseId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().uid ?: return showLoginError()

        with(binding) {
            val expense = ExpenseModule(
                expenseId = newExpenseId,
                note = note.text.toString(),
                category = category.text.toString(),
                type = if (incomeRadio.isChecked) "Income" else "Expense",
                amount = amount.text.toString().toLong(),
                time = Calendar.getInstance().timeInMillis,
                uid = userId
            )

            FirebaseFirestore.getInstance().collection("expenses").document(newExpenseId)
                .set(expense)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Expense saved successfully.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(applicationContext, "Failed to save expense: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updateExpense(expenseId: String) {
        val userId = FirebaseAuth.getInstance().uid ?: return showLoginError()

        with(binding) {
            val expense = ExpenseModule(
                expenseId = expenseId,
                note = note.text.toString(),
                category = category.text.toString(),
                type = if (incomeRadio.isChecked) "Income" else "Expense",
                amount = amount.text.toString().toLong(),
                time = Calendar.getInstance().timeInMillis,
                uid = userId
            )

            FirebaseFirestore.getInstance().collection("expenses").document(expenseId)
                .set(expense)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Expense updated successfully.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(applicationContext, "Failed to update expense: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showLoginError() {
        Toast.makeText(this, "User must be logged in to save an expense.", Toast.LENGTH_SHORT).show()
    }
}
