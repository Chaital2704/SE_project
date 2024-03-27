
package com.example.se_project

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ExpensesAdapter(private val context: Context) : RecyclerView.Adapter<ExpensesAdapter.MyViewHolder>() {

    private val expenseModuleList = mutableListOf<ExpenseModule>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val note: TextView = itemView.findViewById(R.id.note)
        val category: TextView = itemView.findViewById(R.id.category)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.date) // Assuming you have a TextView with this ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate your item layout and return the ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val expenseModel = expenseModuleList[position]
        holder.note.text = expenseModel.note
        holder.category.text = expenseModel.category
        holder.amount.text = expenseModel.amount.toString()
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