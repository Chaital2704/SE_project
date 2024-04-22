package com.example.se_project

import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class MainActivity2Test {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val firebaseAuth = mock(FirebaseAuth::class.java)
    private val firestore = mock(FirebaseFirestore::class.java)

    @Test
    fun testSignInAnonymously_Successful() {
        // Setup
        val activity = MainActivity2()
        `when`(firebaseAuth.signInAnonymously()).thenReturn(/* Mock successful task */)

        // Act
        activity.signInAnonymously()

        // Assert
        verify(firebaseAuth, times(1)).signInAnonymously()
        // Additional assertions can be done to check UI or data changes
    }

    @Test
    fun testAddExpense_LaunchesCorrectActivity() {
        val activity = MainActivity2()
        activity.addExpenseLauncher = /* Setup your activity launcher mock or capture the intent */
            activity.setupButtons()

        // Trigger the button click
        activity.findViewById(R.id.AddExpense).performClick()

        // Assert
        val expectedIntent = Intent(activity, AddExpense::class.java)
        val actualIntent = shadowOf(activity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testDeleteExpense_CallsFirestore() {
        // Setup
        val expenseId = "test_expense_id"
        val activity = MainActivity2()
        `when`(firestore.collection("expenses").document(expenseId).delete()).thenReturn(/* Mock successful task */)

        // Act
        activity.deleteExpense(expenseId)

        // Assert
        verify(firestore, times(1)).collection("expenses").document(expenseId).delete()
        // Verify UI message or other actions following the deletion
    }
}
