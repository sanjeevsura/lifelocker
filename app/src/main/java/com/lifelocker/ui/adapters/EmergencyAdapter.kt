package com.lifelocker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelocker.data.EmergencyContact
import com.lifelocker.databinding.ItemEmergencyContactBinding

class EmergencyAdapter(
    private val onItemClick: (EmergencyContact) -> Unit,
    private val onCallClick: (EmergencyContact) -> Unit,
    private val onDeleteClick: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, EmergencyAdapter.EmergencyViewHolder>(EmergencyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmergencyViewHolder {
        val binding = ItemEmergencyContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmergencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmergencyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EmergencyViewHolder(private val binding: ItemEmergencyContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EmergencyContact) {
            binding.tvContactName.text = item.name
            binding.tvContactRelation.text = "${item.relationship} | ${item.phone}"

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnCallContact.setOnClickListener { onCallClick(item) }
            binding.btnDeleteContact.setOnClickListener { onDeleteClick(item) }
        }
    }

    class EmergencyDiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean =
            oldItem == newItem
    }
}
