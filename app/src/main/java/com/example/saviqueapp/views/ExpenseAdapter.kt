package com.example.saviqueapp.views

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saviqueapp.databinding.ItemExpenseBinding
import com.example.saviqueapp.models.Expense

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvDesc.text = expense.description
            binding.tvAmount.text = "R ${expense.amount}"
            binding.tvTime.text = "${expense.startTime} - ${expense.endTime}"

            // RUBRIC: Access photo from the list if stored
            if (!expense.photoUri.isNullOrEmpty()) {
                binding.ivReceiptPreview.visibility = View.VISIBLE
                binding.ivReceiptPreview.setImageURI(Uri.parse(expense.photoUri))
            } else {
                binding.ivReceiptPreview.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(old: Expense, new: Expense) = old.id == new.id
        override fun areContentsTheSame(old: Expense, new: Expense) = old == new
    }
}