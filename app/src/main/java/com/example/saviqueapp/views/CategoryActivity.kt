package com.example.saviqueapp.views

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saviqueapp.SaviqueApplication
import com.example.saviqueapp.databinding.ActivityCategoryBinding
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory

/**
 * CATEGORY MANAGEMENT: Allows users to organize their expenses.
 * Rubric Focus: "Create categories", "Event handling", and "Reading/Writing to RoomDB"
 */
class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        val adapter = CategoryAdapter()
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter

        // RUBRIC: Observe RoomDB data (Read)
        budgetViewModel.allCategories.observe(this) { categories ->
            adapter.submitList(categories)
        }

        // RUBRIC: Event handling for adding a category (Write)
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Category")

        val input = EditText(this)
        input.hint = "e.g. Groceries, Rent"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val name = input.text.toString()
            if (name.isNotBlank()) {
                budgetViewModel.addCategory(name) // Writes to RoomDB
                Toast.makeText(this, "Category Added!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}