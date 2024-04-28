package com.example.se_project
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import android.content.Intent
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
internal class MainActivity2Test {

    @JvmField
    @Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setup() {
        // Initialize or mock Firebase
        FirebaseApp.initializeApp(RuntimeEnvironment.systemContext)
        firebaseAuth = mock(FirebaseAuth::class.java)
        firestore = mock(FirebaseFirestore::class.java)

        `when`(FirebaseAuth.getInstance()).thenReturn(firebaseAuth)
        `when`(FirebaseFirestore.getInstance()).thenReturn(firestore)

        setupFirebaseAuthResponses()
        setupFirebaseFirestoreResponses()
    }

    private fun setupFirebaseAuthResponses() {
        val mockAuthResultTask = Tasks.forResult(mock(AuthResult::class.java))
        `when`(firebaseAuth.signInAnonymously()).thenReturn(mockAuthResultTask)
    }

    private fun setupFirebaseFirestoreResponses() {
        val mockCollection = mock(CollectionReference::class.java)
        val mockDocument = mock(DocumentReference::class.java)
        val mockQuerySnapshotTask = Tasks.forResult(mock(QuerySnapshot::class.java))

        `when`(firestore.collection(anyString())).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
        `when`(mockDocument.get()).thenReturn(mockQuerySnapshotTask)
    }


    @Test
    fun testSignInAnonymously_Successful() {
        // Prepare the mock task to simulate a successful sign-in
        val mockAuthResult = Mockito.mock(AuthResult::class.java)
        val mockAuthResultTask = Tasks.forResult(mockAuthResult)
        `when`(firebaseAuth.signInAnonymously()).thenReturn(mockAuthResultTask)

        // Build the activity
        val activity = Robolectric.buildActivity(MainActivity2::class.java).create().get()
        activity.firebaseAuth = firebaseAuth  // Inject the mocked FirebaseAuth

        // Act
        activity.signInAnonymously()

        // Assert
        verify(firebaseAuth).signInAnonymously()
        assertTrue("FirebaseAuth signInAnonymously task was not successful", mockAuthResultTask.isSuccessful)
    }

    @Test
    fun testAddExpense_LaunchesCorrectActivity() {
        // Setup
        val controller: ActivityController<MainActivity2> =
            Robolectric.buildActivity(MainActivity2::class.java).create().start()
        val activity: MainActivity2 = controller.get()

        // Trigger the button click
        activity.findViewById<View>(R.id.Add_Expense).performClick()

        // Assert
        val expectedIntent = Intent(
            activity,
            AddExpense::class.java
        )
        val actualIntent: Intent = Shadows.shadowOf(activity).getNextStartedActivity()
        Assert.assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testDeleteExpense_CallsFirestore() {
        // Setup
        val expenseId = "test_expense_id"
        val mockTask = Tasks.forResult<Void?>(null)
        `when`(firestore.collection("expenses").document(expenseId).delete()).thenReturn(mockTask)
        val controller: ActivityController<MainActivity2> =
            Robolectric.buildActivity(MainActivity2::class.java)
        val activity: MainActivity2 = controller.get()
        activity.firestore = firestore

        // Act
        activity.deleteExpense(expenseId)

        // Assert
        verify(firestore).collection("expenses").document(expenseId).delete()
        // Verify UI message or other actions following the deletion
    }
}
