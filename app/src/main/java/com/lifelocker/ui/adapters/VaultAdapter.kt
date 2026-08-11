package com.lifelocker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifelocker.R
import com.lifelocker.data.VaultItem
import com.lifelocker.databinding.ItemVaultBinding
import com.lifelocker.utils.RevealStateManager

class VaultAdapter(
    private val onItemClick: (VaultItem) -> Unit,
    private val onCopyUsernameClick: (VaultItem) -> Unit,
    private val onCopyPasswordClick: (VaultItem) -> Unit,
    private val onRevealPasswordClick: (VaultItem) -> Unit,
    private val onMaskPasswordClick: (VaultItem) -> Unit,
    private val onFavoriteToggle: (VaultItem) -> Unit,
    private val onDeleteClick: (VaultItem) -> Unit,
    private val onDecryptSecret: (String) -> String
) : ListAdapter<VaultItem, VaultAdapter.VaultViewHolder>(VaultDiffCallback()) {

    fun refreshRevealStates() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaultViewHolder {
        val binding = ItemVaultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VaultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VaultViewHolder(private val binding: ItemVaultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VaultItem) {
            val context = binding.root.context
            binding.tvVaultTitle.text = item.title
            binding.tvVaultUsername.text = item.username.ifEmpty { "No username / identifier" }
            binding.tvVaultCategory.text = item.category

            val categoryColor = when (item.category.lowercase()) {
                "banking", "finance" -> ContextCompat.getColor(context, R.color.cat_finance)
                "email", "social" -> ContextCompat.getColor(context, R.color.cat_personal)
                "wi-fi", "wifi" -> ContextCompat.getColor(context, R.color.cat_education)
                "work", "college" -> ContextCompat.getColor(context, R.color.cat_work)
                "government" -> ContextCompat.getColor(context, R.color.cat_travel)
                "subscriptions", "streaming" -> ContextCompat.getColor(context, R.color.cat_vehicle)
                else -> ContextCompat.getColor(context, R.color.accent)
            }
            binding.viewVaultCategoryBar.setBackgroundColor(categoryColor)

            if (item.isFavorite) {
                binding.btnFavoriteVault.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                binding.btnFavoriteVault.setImageResource(android.R.drawable.btn_star_big_off)
            }

            val isRevealed = RevealStateManager.isRevealed(item.id)
            if (isRevealed) {
                val plainText = onDecryptSecret(item.encryptedSecret)
                binding.tvVaultSecretMasked.text = plainText.ifEmpty { "(Empty)" }
                binding.btnToggleSecretVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                binding.tvVaultSecretMasked.text = RevealStateManager.getMaskedDisplay()
                binding.btnToggleSecretVisibility.setImageResource(android.R.drawable.ic_menu_view)
            }

            binding.btnToggleSecretVisibility.setOnClickListener {
                if (RevealStateManager.isRevealed(item.id)) {
                    onMaskPasswordClick(item)
                } else {
                    onRevealPasswordClick(item)
                }
            }

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnEditVault.setOnClickListener { onItemClick(item) }
            binding.btnFavoriteVault.setOnClickListener { onFavoriteToggle(item) }
            binding.btnCopySecret.setOnClickListener { onCopyPasswordClick(item) }

            binding.btnMoreVault.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Edit Credential")
                if (item.username.isNotEmpty()) {
                    popup.menu.add("Copy Username")
                }
                popup.menu.add("Copy Password")
                popup.menu.add(if (item.isFavorite) "Remove from Favorites" else "Add to Favorites")
                popup.menu.add("Delete")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title.toString()) {
                        "Edit Credential" -> onItemClick(item)
                        "Copy Username" -> onCopyUsernameClick(item)
                        "Copy Password" -> onCopyPasswordClick(item)
                        "Add to Favorites", "Remove from Favorites" -> onFavoriteToggle(item)
                        "Delete" -> onDeleteClick(item)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    class VaultDiffCallback : DiffUtil.ItemCallback<VaultItem>() {
        override fun areItemsTheSame(oldItem: VaultItem, newItem: VaultItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: VaultItem, newItem: VaultItem): Boolean =
            oldItem == newItem
    }
}
