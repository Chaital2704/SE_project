package com.example.se_project

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LineHeightSpan
import android.text.style.RelativeSizeSpan
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.se_project.databinding.ActivityMain2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

interface ExpenseActionsListener {
    fun onEditClicked(expenseId: String)
    fun onDeleteClicked(expenseId: String)
}
class MainActivity2 : AppCompatActivity(), ExpenseActionsListener {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var addExpenseLauncher: ActivityResultLauncher<Intent>
    private var balance: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        updateGreetingMessage()
        handleAuthentication()
        initializeExpenseLauncher()
        setupRecyclerView()
        setupButtons()
        signInAnonymously()
    }
    private fun handleAuthentication() {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                // User is signed in, fetch data
                getData()
            } else {
                // User is signed out, attempt to sign in anonymously
                firebaseAuth.signInAnonymously().addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign-in successful, fetch data
                        getData()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initializeExpenseLauncher() {
        addExpenseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Data might have changed, refresh the UI
                refreshDataAndUI()
            }
        }
    }

    private fun setupRecyclerView() {
        expensesAdapter = ExpensesAdapter(this, this)
        binding.recyclerView.apply {
            adapter = expensesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupButtons() {
        binding.AddIncome.setOnClickListener {
            val intent = Intent(this, AddExpense::class.java).apply { putExtra("type", "Income") }
            addExpenseLauncher.launch(intent)
        }

        binding.AddExpense.setOnClickListener {
            val intent = Intent(this, AddExpense::class.java).apply { putExtra("type", "Expense") }
            addExpenseLauncher.launch(intent)
        }
    }

    private fun signInAnonymously() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
//    private fun updateBalanceDisplay() {
//        val balanceText = "Your Balance\n$balance"
//        val balanceIndex = balanceText.indexOf("$balance")
//        val spannableString = SpannableString(balanceText)
//
//        spannableString.setSpan(
//            RelativeSizeSpan(1.5f), // Set the relative size to 1.5 times the default size
//            balanceIndex,
//            balanceIndex + balance.toString().length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//        // Apply custom line height for the part before the balance amount
//        spannableString.setSpan(
//            object : LineHeightSpan {
//                override fun chooseHeight(
//                    text: CharSequence?,
//                    start: Int,
//                    end: Int,
//                    spanstartv: Int,
//                    lineHeight: Int,
//                    fm: android.graphics.Paint.FontMetricsInt
//                ) {
//                    fm.bottom += 16 // Adjust bottom as needed
//                    fm.descent += 16 // Adjust descent as needed
//                }
//            },
//            0,
//            balanceIndex,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//        binding.bal.text = spannableString
//    }
    private fun updateGreetingMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..23 -> "Good Evening"
            else -> "Hello"
        }
        binding.greetingTextView.text = "$greeting, User!" // Assuming you have a TextView with the id greetingTextView in your layout
    }



    private fun refreshDataAndUI() {

        getData()
        updateGreetingMessage()
    }

    private fun getData() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis - 1

        // Reset balance before recalculating
        balance = 0

        FirebaseFirestore.getInstance().collection("expenses")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                expensesAdapter.clear()
                var totalExpensesToday = 0L
                documents.forEach { document ->
                    document.toObject(ExpenseModule::class.java)?.let { expense ->
                        expensesAdapter.add(expense)
                        when {
                            expense.time in startOfDay..endOfDay && expense.type == "Expense" -> totalExpensesToday += expense.amount
                            expense.time in startOfDay..endOfDay && expense.type == "Income" -> totalExpensesToday -= expense.amount
                        }
                        balance += if (expense.type == "Income") expense.amount else -expense.amount
                    }
                }
                updateUI(totalExpensesToday)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch expenses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateUI(totalExpensesToday: Long) {
        val totalExpensesText = "Total Expenses Today: $totalExpensesToday"
        val totalBalanceText = "Total Balance: \n$balance"
        binding.totalExpensesTodayTextView.text = totalExpensesText
        binding.bal.text = totalBalanceText
    }

    override fun onEditClicked(expenseId: String) {
        val intent = Intent(this, AddExpense::class.java).apply {
            putExtra("expenseId", expenseId)
        }
        addExpenseLauncher.launch(intent)
    }

    override fun onDeleteClicked(expenseId: String) {
        AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Do you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                deleteExpense(expenseId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteExpense(expenseId: String) {
        FirebaseFirestore.getInstance().collection("expenses")
            .document(expenseId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
                refreshDataAndUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting expense: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
