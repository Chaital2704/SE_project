package com.example.se_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.se_project.databinding.ActivityAddExpenseBinding

class AddExpense : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveExpense -> {
                createExpense()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createExpense() {
        val amount = binding.amount.text.toString()
        val note = binding.note.text.toString()
        val category = binding.category.text.toString()
        val incomeChecked = binding.incomeRadio.isChecked()
        // Further processing with the expense data
    }

}
