package com.example.se_project

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LineHeightSpan
import android.text.style.RelativeSizeSpan
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.se_project.databinding.ActivityMain2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var expensesAdapter: ExpensesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        expensesAdapter = ExpensesAdapter(this)
        binding.recyclerView.adapter = expensesAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this) // Changed from 'new' to direct call

        // Initialize the Intent for navigating to the AddExpense activity
        val intent = Intent(this, AddExpense::class.java) // Assuming AddExpense exists

        // Set click listeners using Kotlin lambdas
        binding.AddIncome.setOnClickListener {
            intent.putExtra("type", "Income")
            startActivity(intent)
        }

        binding.AddExpense.setOnClickListener {
            intent.putExtra("type", "Expense")
            startActivity(intent)
        }

        // Custom styling for the balance TextView
        applyCustomTextStyles()
    }

    override fun onStart() {
        super.onStart()

        FirebaseAuth.getInstance().currentUser?.let {
            // User is already signed in
        } ?: run {
            // No user is signed in, attempt anonymous sign-in
            Toast.makeText(this, "Attempting to sign in...", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener { authResult ->
                    // Successfully signed in anonymously
                    // You might want to update UI or perform other operations here
                }
                .addOnFailureListener { e ->
                    // Handle sign-in failure
                    Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun applyCustomTextStyles() {
        val balanceText = "Your Balance\n30,000"
        val spannableString = SpannableString(balanceText)

        // Apply a bigger text size to "30,000"
        spannableString.setSpan(
            RelativeSizeSpan(1.5f), // Set the relative size to 1.5 times the default size
            balanceText.indexOf("30,000"),
            balanceText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply custom line height for the part before the balance amount
        spannableString.setSpan(
            object : LineHeightSpan {
                override fun chooseHeight(
                    text: CharSequence?,
                    start: Int,
                    end: Int,
                    spanstartv: Int,
                    lineHeight: Int,
                    fm: android.graphics.Paint.FontMetricsInt
                ) {
                    fm.bottom += 16 // Adjust bottom as needed
                    fm.descent += 16 // Adjust descent as needed
                }
            },
            0,
            balanceText.indexOf("\n"),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.bal.text = spannableString
    }
    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        val uid = FirebaseAuth.getInstance().uid ?: return // Get the UID or return if null

        FirebaseFirestore.getInstance()
            .collection("expenses")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                expensesAdapter.clear()
                for (ds in queryDocumentSnapshots.documents) {
                    val expenseModel = ds.toObject(ExpenseModule::class.java)
                    expenseModel?.let {
                        // Make sure to add only if the expenseModel is not null
                        expensesAdapter.add(it)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle the failure
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

}
