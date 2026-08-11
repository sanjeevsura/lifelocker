package com.lifelocker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelocker.data.ReminderItem
import com.lifelocker.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter(
    private val onItemClick: (ReminderItem) -> Unit,
    private val onToggleComplete: (ReminderItem) -> Unit,
    private val onDeleteClick: (ReminderItem) -> Unit
) : ListAdapter<ReminderItem, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReminderItem) {
            binding.tvReminderTitle.text = item.title
            binding.tvReminderDate.text = "Due: ${dateFormat.format(Date(item.dueDateMillis))}"
            binding.tvReminderPriority.text = item.priority
            binding.cbReminderComplete.isChecked = item.isCompleted

            binding.cbReminderComplete.setOnClickListener { onToggleComplete(item) }
            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnDeleteReminder.setOnClickListener { onDeleteClick(item) }
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<ReminderItem>() {
        override fun areItemsTheSame(oldItem: ReminderItem, newItem: ReminderItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ReminderItem, newItem: ReminderItem): Boolean =
            oldItem == newItem
    }
}
