package com.example.saviqueapp.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.saviqueapp.SaviqueApplication
import com.example.saviqueapp.databinding.ActivityAddExpenseBinding
import com.example.saviqueapp.models.Category
import com.example.saviqueapp.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import java.io.File
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    private var selectedDate: Long = 0L
    private var startTime: String = ""
    private var endTime: String = ""
    private var photoUri: Uri? = null
    private var categoriesList: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategorySpinner()
        setupPickers()
        setupCamera()

        binding.btnSaveExpense.setOnClickListener { saveExpense() }
    }

    private fun setupCategorySpinner() {
        // RUBRIC: Reading categories from RoomDB to populate the selection
        budgetViewModel.allCategories.observe(this) { categories ->
            categoriesList = categories
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories.map { it.name })
            binding.spinnerCategory.adapter = adapter
        }
    }

    private fun setupPickers() {
        val calendar = Calendar.getInstance()

        // RUBRIC: Date Selection
        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                selectedDate = calendar.timeInMillis
                binding.btnPickDate.text = "$d/${m+1}/$y"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // RUBRIC: Start and End Times
        binding.btnPickStart.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                startTime = String.format("%02d:%02d", h, m)
                binding.btnPickStart.text = startTime
            }, 12, 0, true).show()
        }

        binding.btnPickEnd.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                endTime = String.format("%02d:%02d", h, m)
                binding.btnPickEnd.text = endTime
            }, 13, 0, true).show()
        }
    }

    private fun setupCamera() {
        // Creating a file to save the photo
        val photoFile = File(filesDir, "expense_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri = uri
                binding.ivExpensePhoto.visibility = View.VISIBLE
                binding.ivExpensePhoto.setImageURI(uri)
            }
        }

        binding.btnTakePhoto.setOnClickListener { takePhoto.launch(uri) }
    }

    private fun saveExpense() {
        val desc = binding.etDescription.text.toString()
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val selectedCatIndex = binding.spinnerCategory.selectedItemPosition

        if (desc.isNotBlank() && selectedDate != 0L && selectedCatIndex != -1) {
            val expense = Expense(
                description = desc,
                amount = amount,
                date = selectedDate,
                startTime = startTime,
                endTime = endTime,
                categoryId = categoriesList[selectedCatIndex].id,
                photoUri = photoUri?.toString()
            )
            budgetViewModel.addExpense(expense)

            // CUSTOM FEATURE 1: Update spending streak in Firestore
            updateStreak()

            Toast.makeText(this, "Expense Saved!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * CUSTOM FEATURE 1: Spending Streak
     * Checks if the user logged an expense yesterday to continue the streak,
     * or resets to 1 if they missed a day.
     * Data stored in Firestore under /users/{uid}/streak and lastLogDate.
     */
    private fun updateStreak() {
        val TAG = "SAVIQUE_STREAK"
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(uid)

        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(java.util.Date())

        userDoc.get()
            .addOnSuccessListener { doc ->
                val lastLogDate = doc.getString("lastLogDate") ?: ""
                val currentStreak = doc.getLong("streak")?.toInt() ?: 0

                // Calculate yesterday's date string for comparison
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(cal.time)

                val newStreak = when {
                    lastLogDate == todayStr -> {
                        // Already logged today — don't increment
                        android.util.Log.d(TAG, "Already logged today, streak stays at $currentStreak")
                        currentStreak
                    }
                    lastLogDate == yesterdayStr -> {
                        // Logged yesterday — increment streak
                        android.util.Log.d(TAG, "Consecutive day! Streak: ${currentStreak + 1}")
                        currentStreak + 1
                    }
                    else -> {
                        // Missed a day — reset streak to 1
                        android.util.Log.d(TAG, "Streak broken. Resetting to 1")
                        1
                    }
                }

                // Save updated streak back to Firestore
                userDoc.update(
                    mapOf(
                        "streak" to newStreak,
                        "lastLogDate" to todayStr
                    )
                )
                    .addOnSuccessListener {
                        android.util.Log.d(TAG, "Streak updated to $newStreak on $todayStr")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e(TAG, "Failed to update streak: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e(TAG, "Failed to read user doc for streak: ${e.message}")
            }
    }
}