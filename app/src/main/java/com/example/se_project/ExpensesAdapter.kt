package com.example.se_project

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class ExpensesAdapter(
    private val context: Context,
    private val listener: ExpenseActionsListener
) : RecyclerView.Adapter<ExpensesAdapter.MyViewHolder>() {

    private val expenseModuleList = mutableListOf<ExpenseModule>()

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val note: TextView = itemView.findViewById(R.id.note)
        val category: TextView = itemView.findViewById(R.id.category)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.date) // Assuming you have a TextView with this ID

        init {
            itemView.setOnClickListener {
                // Use the position associated with the ViewHolder
                val expenseModule = expenseModuleList[adapterPosition]
                // Show edit/delete options dialog
                AlertDialog.Builder(context)
                    .setTitle("Select Option")
                    .setItems(arrayOf("Edit", "Delete")) { _, which ->
                        when (which) {
                            0 -> listener.onEditClicked(expenseModule.expenseId) // Edit option
                            1 -> listener.onDeleteClicked(expenseModule.expenseId) // Delete option
                        }
                    }
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val expenseModel = expenseModuleList[position]
        holder.note.text = expenseModel.note
        holder.category.text = expenseModel.category
        holder.amount.text = expenseModel.amount.toString()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        holder.date.text = dateFormat.format(Date(expenseModel.time))

        val colorResId = if (expenseModel.type == "Income") R.color.green else R.color.red
        holder.amount.setTextColor(ContextCompat.getColor(context, colorResId))
    }

    override fun getItemCount(): Int = expenseModuleList.size

    @SuppressLint("NotifyDataSetChanged")
    fun add(expenseModel: ExpenseModule) {
        expenseModuleList.add(expenseModel)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        expenseModuleList.clear()
        notifyDataSetChanged()
    }
}
