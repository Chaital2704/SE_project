package com.example.se_project

data class ExpenseModule(
    var expenseId: String = "",
    var note: String = "",
    var category: String = "",
    var type: String? = null,
    var amount: Long = 0L,
    var time: Long = 0L,
    var uid: String = ""
) {
    // Override toString() for debugging
    override fun toString(): String {
        return "ExpenseModule(expenseId='$expenseId', note='$note', category='$category', type=$type, amount=$amount, time=$time, uid='$uid')"
    }
}
