package com.lifelocker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelocker.data.SecureNote
import com.lifelocker.databinding.ItemSecureNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecureNoteAdapter(
    private val onItemClick: (SecureNote) -> Unit,
    private val onFavoriteToggle: (SecureNote) -> Unit,
    private val onTrashClick: (SecureNote) -> Unit
) : ListAdapter<SecureNote, SecureNoteAdapter.NoteVH>(NoteDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteVH {
        val binding = ItemSecureNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteVH(binding)
    }

    override fun onBindViewHolder(holder: NoteVH, position: Int) = holder.bind(getItem(position))

    inner class NoteVH(private val binding: ItemSecureNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: SecureNote) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteCategory.text = note.category
            binding.tvNoteDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(note.updatedAt))
            binding.btnNoteFavorite.setImageResource(
                if (note.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            binding.root.setOnClickListener { onItemClick(note) }
            binding.btnNoteFavorite.setOnClickListener { onFavoriteToggle(note) }
            binding.btnNoteMore.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Edit")
                popup.menu.add(if (note.isFavorite) "Remove Favorite" else "Add Favorite")
                popup.menu.add("Move to Trash")
                popup.setOnMenuItemClickListener { item ->
                    when (item.title.toString()) {
                        "Edit" -> onItemClick(note)
                        "Add Favorite", "Remove Favorite" -> onFavoriteToggle(note)
                        "Move to Trash" -> onTrashClick(note)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    class NoteDiff : DiffUtil.ItemCallback<SecureNote>() {
        override fun areItemsTheSame(a: SecureNote, b: SecureNote) = a.id == b.id
        override fun areContentsTheSame(a: SecureNote, b: SecureNote) = a == b
    }
}
