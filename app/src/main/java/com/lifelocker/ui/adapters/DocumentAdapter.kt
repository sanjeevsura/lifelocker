package com.lifelocker.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelocker.R
import com.lifelocker.data.Document
import com.lifelocker.databinding.ItemDocumentBinding
import com.lifelocker.utils.FileStorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentAdapter(
    private val onItemClick: (Document) -> Unit,
    private val onItemLongClick: (Document) -> Unit,
    private val onFavoriteClick: (Document) -> Unit,
    private val onOpenClick: (Document) -> Unit,
    private val onShareClick: (Document) -> Unit,
    private val onExportClick: (Document) -> Unit,
    private val onDeleteClick: (Document) -> Unit,
    private val onAddReminderClick: (Document) -> Unit,
    private val onSelectionToggle: (Document) -> Unit
) : ListAdapter<Document, DocumentAdapter.DocumentViewHolder>(DocumentDiffCallback()) {

    var isMultiSelectMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    val selectedDocumentIds = mutableSetOf<Int>()

    fun toggleSelection(docId: Int) {
        if (selectedDocumentIds.contains(docId)) {
            selectedDocumentIds.remove(docId)
        } else {
            selectedDocumentIds.add(docId)
        }
        notifyDataSetChanged()
    }

    fun selectAll(allDocs: List<Document>) {
        selectedDocumentIds.clear()
        selectedDocumentIds.addAll(allDocs.map { it.id })
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedDocumentIds.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DocumentViewHolder(private val binding: ItemDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Document) {
            val context = binding.root.context
            binding.tvDocTitle.text = item.title
            binding.tvDocCategory.text = item.category

            val extStr = if (item.originalExtension.isNotEmpty()) item.originalExtension.uppercase() else "FILE"
            val sizeStr = if (item.fileSize > 0) FileStorageHelper.formatFileSize(item.fileSize) else ""
            binding.tvDocFileInfo.text = if (sizeStr.isNotEmpty()) "• $extStr · $sizeStr" else "• $extStr"

            // Favorite star icon
            if (item.isFavorite) {
                binding.btnFavoriteDoc.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                binding.btnFavoriteDoc.setImageResource(android.R.drawable.btn_star_big_off)
            }

            // Category color indicator
            val categoryColor = when (item.category.lowercase()) {
                "identity", "passport", "id card" -> ContextCompat.getColor(context, R.color.cat_personal)
                "education", "certificate" -> ContextCompat.getColor(context, R.color.cat_education)
                "finance", "invoice" -> ContextCompat.getColor(context, R.color.cat_finance)
                "medical", "health" -> ContextCompat.getColor(context, R.color.cat_medical)
                "travel" -> ContextCompat.getColor(context, R.color.cat_travel)
                "vehicle", "auto" -> ContextCompat.getColor(context, R.color.cat_vehicle)
                "work" -> ContextCompat.getColor(context, R.color.cat_work)
                "family" -> ContextCompat.getColor(context, R.color.cat_family)
                else -> ContextCompat.getColor(context, R.color.accent)
            }
            binding.viewCategoryIndicator.setBackgroundColor(categoryColor)

            // Expiry status calculation via ExpiryHelper
            if (!item.expiryDate.isNullOrEmpty()) {
                binding.layoutExpiryBadge.visibility = View.VISIBLE
                val status = com.lifelocker.utils.ExpiryHelper.calculateExpiryStatus(item.expiryDate)
                binding.tvDocExpiry.text = status.statusText
                binding.tvDocExpiry.setTextColor(ContextCompat.getColor(context, status.colorResId))
            } else {
                binding.layoutExpiryBadge.visibility = View.GONE
            }

            // Multi-selection UI
            if (isMultiSelectMode) {
                binding.cbSelectDocument.visibility = View.VISIBLE
                binding.cbSelectDocument.isChecked = selectedDocumentIds.contains(item.id)
                binding.btnMoreDoc.visibility = View.GONE
            } else {
                binding.cbSelectDocument.visibility = View.GONE
                binding.btnMoreDoc.visibility = View.VISIBLE
            }

            // Click Listeners
            binding.root.setOnClickListener {
                if (isMultiSelectMode) {
                    toggleSelection(item.id)
                    onSelectionToggle(item)
                } else {
                    onItemClick(item)
                }
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }

            binding.btnFavoriteDoc.setOnClickListener { onFavoriteClick(item) }

            binding.btnMoreDoc.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Open").setIcon(android.R.drawable.ic_menu_view)
                popup.menu.add("Share").setIcon(android.R.drawable.ic_menu_share)
                popup.menu.add("Export / Download").setIcon(android.R.drawable.ic_menu_save)
                popup.menu.add("Add Expiry Reminder").setIcon(android.R.drawable.ic_popup_reminder)
                popup.menu.add(if (item.isFavorite) "Remove from Favorites" else "Add to Favorites")
                popup.menu.add("Delete").setIcon(android.R.drawable.ic_menu_delete)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title.toString()) {
                        "Open" -> onOpenClick(item)
                        "Share" -> onShareClick(item)
                        "Export / Download" -> onExportClick(item)
                        "Add Expiry Reminder" -> onAddReminderClick(item)
                        "Add to Favorites", "Remove from Favorites" -> onFavoriteClick(item)
                        "Delete" -> onDeleteClick(item)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    class DocumentDiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean =
            oldItem == newItem
    }
}


