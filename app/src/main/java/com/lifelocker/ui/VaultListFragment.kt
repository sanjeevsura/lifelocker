package com.lifelocker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.VaultRepository
import com.lifelocker.databinding.FragmentVaultListBinding
import com.lifelocker.ui.adapters.VaultAdapter
import com.lifelocker.utils.BiometricHelper
import com.lifelocker.utils.RevealStateManager
import com.lifelocker.utils.SecureClipboardHelper
import com.lifelocker.utils.SensitiveActionAuthenticator
import com.lifelocker.viewmodel.VaultViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VaultListFragment : Fragment() {

    private var _binding: FragmentVaultListBinding? = null
    private val binding get() = _binding!!

    private val vaultViewModel: VaultViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(vaultRepository = VaultRepository(app.database.vaultDao()))
    }

    private lateinit var adapter: VaultAdapter
    private lateinit var sensitiveAuth: SensitiveActionAuthenticator

    private val revealListener: () -> Unit = {
        if (_binding != null) adapter.refreshRevealStates()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as LifeLockerApp
        sensitiveAuth = SensitiveActionAuthenticator(
            requireActivity(),
            app.securityManager,
            BiometricHelper(requireActivity())
        )

        adapter = VaultAdapter(
            onItemClick = { item ->
                val bundle = Bundle().apply { putInt("vaultItemId", item.id) }
                findNavController().navigate(R.id.action_vault_to_addEdit, bundle)
            },
            onCopyUsernameClick = { item ->
                if (item.username.isNotEmpty()) {
                    SecureClipboardHelper.copySecurely(requireContext(), "Username", item.username)
                    Snackbar.make(binding.root, "Username copied (auto-clears in 30s)", Snackbar.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No username to copy", Toast.LENGTH_SHORT).show()
                }
            },
            onCopyPasswordClick = { item ->
                sensitiveAuth.authenticate(
                    itemTitle = item.title,
                    subtitle = "Verify your identity to copy this password",
                    onAuthenticated = {
                        val decrypted = vaultViewModel.decryptSecret(item.encryptedSecret)
                        if (decrypted.isNotEmpty()) {
                            SecureClipboardHelper.copySecurely(requireContext(), "Password", decrypted)
                            Snackbar.make(binding.root, "Copied securely · clears in 30 seconds", Snackbar.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "No password to copy", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            onRevealPasswordClick = { item ->
                sensitiveAuth.authenticate(
                    itemTitle = item.title,
                    subtitle = "Verify your identity to view this secret.",
                    onAuthenticated = {
                        RevealStateManager.reveal(item.id) {
                            if (_binding != null) adapter.refreshRevealStates()
                        }
                        adapter.refreshRevealStates()
                    }
                )
            },
            onMaskPasswordClick = { item ->
                RevealStateManager.mask(item.id)
                adapter.refreshRevealStates()
            },
            onFavoriteToggle = { item -> vaultViewModel.toggleFavorite(item) },
            onDeleteClick = { item ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Credential?")
                    .setMessage("Are you sure you want to permanently delete \"${item.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        vaultViewModel.deleteVaultItem(item)
                        Snackbar.make(binding.root, "Credential deleted", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onDecryptSecret = { encrypted -> vaultViewModel.decryptSecret(encrypted) }
        )

        RevealStateManager.addListener(revealListener)

        binding.rvVault.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVault.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vaultViewModel.vaultItems.collectLatest { list ->
                adapter.submitList(list)
                binding.emptyStateVault.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                vaultViewModel.setCategoryFilter("All")
                return@setOnCheckedStateChangeListener
            }
            val category = when (checkedIds.first()) {
                R.id.chip_personal -> "Personal"
                R.id.chip_banking -> "Banking"
                R.id.chip_social -> "Social"
                R.id.chip_work -> "Work"
                R.id.chip_travel -> "Travel"
                else -> "All"
            }
            vaultViewModel.setCategoryFilter(category)
        }

        binding.searchViewVault.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                vaultViewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                vaultViewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        binding.fabAddVault.setOnClickListener {
            findNavController().navigate(R.id.action_vault_to_addEdit)
        }
    }

    override fun onPause() {
        super.onPause()
        RevealStateManager.maskAll()
        SecureClipboardHelper.clearClipboard(requireContext())
    }

    override fun onDestroyView() {
        RevealStateManager.removeListener(revealListener)
        RevealStateManager.maskAll()
        super.onDestroyView()
        _binding = null
    }
}
