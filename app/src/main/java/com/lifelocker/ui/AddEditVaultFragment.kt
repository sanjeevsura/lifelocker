package com.lifelocker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.Slider
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.VaultRepository
import com.lifelocker.databinding.DialogPasswordGeneratorBinding
import com.lifelocker.databinding.FragmentAddEditVaultBinding
import com.lifelocker.utils.BiometricHelper
import com.lifelocker.utils.SecureClipboardHelper
import com.lifelocker.utils.SensitiveActionAuthenticator
import com.lifelocker.viewmodel.VaultViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class AddEditVaultFragment : Fragment() {

    private var _binding: FragmentAddEditVaultBinding? = null
    private val binding get() = _binding!!

    private val vaultViewModel: VaultViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(vaultRepository = VaultRepository(app.database.vaultDao()))
    }

    private var vaultItemId: Int = 0
    private lateinit var sensitiveAuth: SensitiveActionAuthenticator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditVaultBinding.inflate(inflater, container, false)
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

        vaultItemId = arguments?.getInt("vaultItemId", 0) ?: 0

        if (vaultItemId != 0) {
            binding.tvFormTitle.text = "Edit Vault Item"
            viewLifecycleOwner.lifecycleScope.launch {
                val item = vaultViewModel.getVaultItemById(vaultItemId)
                item?.let {
                    binding.etVaultTitle.setText(it.title)
                    binding.etVaultCategory.setText(it.category)
                    binding.etVaultUsername.setText(it.username)
                    binding.etVaultUrl.setText(it.url)
                    binding.etVaultSecret.setText(vaultViewModel.decryptSecret(it.encryptedSecret))
                    binding.etVaultTags.setText(it.tags)
                    binding.etVaultNotes.setText(it.notes)
                }
            }
        }

        binding.btnGeneratePassword.setOnClickListener {
            showPasswordGeneratorDialog()
        }

        binding.btnSaveVault.setOnClickListener {
            saveVaultItem()
        }
    }

    private fun showPasswordGeneratorDialog() {
        val dialogBinding = DialogPasswordGeneratorBinding.inflate(layoutInflater)

        fun regenerate() {
            val length = dialogBinding.sliderPasswordLength.value.toInt()
            val includeUpper = dialogBinding.cbUppercase.isChecked
            val includeLower = dialogBinding.cbLowercase.isChecked
            val includeNumbers = dialogBinding.cbNumbers.isChecked
            val includeSymbols = dialogBinding.cbSymbols.isChecked

            if (!includeUpper && !includeLower && !includeNumbers && !includeSymbols) {
                dialogBinding.etGeneratedPassword.setText("")
                dialogBinding.tvStrengthLabel.text = "Strength: —"
                return
            }

            val password = vaultViewModel.generatePassword(
                length = length,
                includeUpper = includeUpper,
                includeLower = includeLower,
                includeNumbers = includeNumbers,
                includeSymbols = includeSymbols
            )
            dialogBinding.etGeneratedPassword.setText(password)
            val strength = vaultViewModel.calculatePasswordStrength(password)
            dialogBinding.tvStrengthLabel.text = "Strength: ${strength.label}"
            dialogBinding.tvStrengthLabel.setTextColor(
                ContextCompat.getColor(requireContext(), strength.colorResId)
            )
        }

        dialogBinding.sliderPasswordLength.addOnChangeListener { slider: Slider, _, _ ->
            dialogBinding.tvPasswordLengthLabel.text = "Length: ${slider.value.toInt()}"
            regenerate()
        }

        val changeListener = { _: android.widget.CompoundButton, _: Boolean -> regenerate() }
        dialogBinding.cbUppercase.setOnCheckedChangeListener(changeListener)
        dialogBinding.cbLowercase.setOnCheckedChangeListener(changeListener)
        dialogBinding.cbNumbers.setOnCheckedChangeListener(changeListener)
        dialogBinding.cbSymbols.setOnCheckedChangeListener(changeListener)

        regenerate()

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Use Password") { dialog, _ ->
                binding.etVaultSecret.setText(dialogBinding.etGeneratedPassword.text.toString())
                dialog.dismiss()
            }
            .setNeutralButton("Copy") { _, _ ->
                val password = dialogBinding.etGeneratedPassword.text.toString()
                if (password.isNotEmpty()) {
                    sensitiveAuth.authenticate(
                        subtitle = "Authenticate to copy password",
                        onAuthenticated = {
                            SecureClipboardHelper.copySecurely(requireContext(), "Password", password)
                            Toast.makeText(requireContext(), "Copied securely (auto-clears in 30s)", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveVaultItem() {
        val title = binding.etVaultTitle.text.toString().trim()
        val category = binding.etVaultCategory.text.toString().trim().ifEmpty { "Personal" }
        val username = binding.etVaultUsername.text.toString().trim()
        val url = binding.etVaultUrl.text.toString().trim()
        val secret = binding.etVaultSecret.text.toString().trim()
        val hint = binding.etVaultPasswordHint.text.toString().trim()
        val tags = binding.etVaultTags.text.toString().trim()
        val extraNotes = binding.etVaultNotes.text.toString().trim()
        val fullNotes = if (hint.isNotEmpty()) "Hint: $hint\n$extraNotes".trim() else extraNotes

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        vaultViewModel.saveVaultItem(
            id = vaultItemId,
            title = title,
            itemType = "PASSWORD",
            username = username,
            url = url,
            tags = tags,
            plainSecret = secret,
            category = category,
            notes = fullNotes,
            isFavorite = false,
            onComplete = {
                Toast.makeText(requireContext(), "Vault item saved securely", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        )
    }

    override fun onPause() {
        super.onPause()
        SecureClipboardHelper.clearClipboard(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
