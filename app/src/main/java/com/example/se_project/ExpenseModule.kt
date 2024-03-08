package com.example.se_project

class ExpenseModule(
    var expenseId: String,
    var note: String,
    var category: String,
    var amount: Long,
    var time: Long
) {
    // You can also override toString() for a meaningful representation of the object
    override fun toString(): String {
        return "ExpenseModule(expenseId='$expenseId', note='$note', category='$category', amount=$amount, time=$time)"
    }
}

