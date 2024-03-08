package com.example.se_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.LineHeightSpan
import android.text.style.RelativeSizeSpan
import android.widget.TextView

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val balanceTextView: TextView = findViewById(R.id.bal)
        val balanceText = "Your Balance\n30,000"
        val spannableString = SpannableString(balanceText)

        // Apply a bigger text size to the balance amount ("30,000")
        spannableString.setSpan(
            RelativeSizeSpan(1.5f),  // Set the relative size to 1.5 times
            balanceText.indexOf("30,000"),  // Start index of the balance amount
            balanceText.length,  // End index of the balance amount
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            object : LineHeightSpan {
                override fun chooseHeight(
                    text: CharSequence,
                    start: Int,
                    end: Int,
                    spanstartv: Int,
                    v: Int,
                    fm: android.graphics.Paint.FontMetricsInt
                ) {
                    fm.bottom += 16 // Adjust the bottom padding as needed
                    fm.descent += 16 // Adjust the descent padding as needed
                }
            },
            0,  // Start index of the span
            balanceText.indexOf("\n"),  // End index of the span
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        balanceTextView.text = spannableString
    }
}