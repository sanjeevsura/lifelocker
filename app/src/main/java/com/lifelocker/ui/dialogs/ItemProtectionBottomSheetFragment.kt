package com.lifelocker.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lifelocker.LifeLockerApp
import com.lifelocker.databinding.DialogItemProtectionBinding
import com.lifelocker.utils.BiometricHelper

class ItemProtectionBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: DialogItemProtectionBinding? = null
    private val binding get() = _binding!!

    var itemTitle: String = "Protected Item"
    var itemSubtitle: String = "Verify your identity to view this secret."
    var itemPassword: String? = null
    var onAuthenticated: (() -> Unit)? = null
    var onCancelled: (() -> Unit)? = null
    var onRecoveryAuthorized: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogItemProtectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvProtectionTitle.text = itemTitle
        binding.tvProtectionSubtitle.text = itemSubtitle

        val app = requireActivity().application as LifeLockerApp
        val biometricHelper = BiometricHelper(requireActivity())
        val securityManager = app.securityManager

        val biometricAvailable = biometricHelper.isBiometricAvailable() && securityManager.isAnyBiometricEnabled()
        binding.btnAuthBiometric.visibility = if (biometricAvailable) View.VISIBLE else View.GONE

        binding.btnAuthBiometric.setOnClickListener {
            biometricHelper.showBiometricPrompt(
                title = "Unlock Protected Item",
                subtitle = "Verify your identity to view $itemTitle",
                description = biometricHelper.getBiometricDescription(),
                negativeButtonText = "Use Master Code",
                onSuccess = {
                    dismiss()
                    onAuthenticated?.invoke()
                },
                onError = { err ->
                    Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                },
                onNegativeButton = {
                    showMasterCodeInput()
                }
            )
        }

        binding.btnAuthItemPassword.setOnClickListener {
            binding.layoutItemPasswordInput.visibility = View.VISIBLE
            binding.layoutMasterCodeInput.visibility = View.GONE
            binding.etItemPassword.requestFocus()
        }

        binding.btnSubmitItemPassword.setOnClickListener {
            val entered = binding.etItemPassword.text.toString().trim()
            if (itemPassword.isNullOrEmpty() || entered == itemPassword) {
                dismiss()
                onAuthenticated?.invoke()
            } else {
                Toast.makeText(requireContext(), "Incorrect item password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAuthMasterCode.setOnClickListener {
            showMasterCodeInput()
        }

        binding.btnSubmitMasterCode.setOnClickListener {
            val pin = binding.etMasterCode.text.toString().trim()
            if (securityManager.validatePin(pin)) {
                dismiss()
                onAuthenticated?.invoke()
            } else {
                Toast.makeText(requireContext(), "Incorrect Master Code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvForgotItemPassword.setOnClickListener {
            showRecoveryDialog(biometricAvailable)
        }
    }

    private fun showMasterCodeInput() {
        binding.layoutMasterCodeInput.visibility = View.VISIBLE
        binding.layoutItemPasswordInput.visibility = View.GONE
        binding.etMasterCode.requestFocus()
    }

    private fun showRecoveryDialog(biometricAvailable: Boolean) {
        val options = mutableListOf<String>()
        if (biometricAvailable) options.add("Use Biometrics")
        options.add("Use Master Code")

        AlertDialog.Builder(requireContext())
            .setTitle("Recover Protected Item")
            .setMessage("Verify identity to reset protection and re-encrypt item.")
            .setItems(options.toTypedArray()) { _, which ->
                val selected = options[which]
                if (selected == "Use Biometrics") {
                    val biometricHelper = BiometricHelper(requireActivity())
                    biometricHelper.showBiometricPrompt(
                        title = "Recovery Verification",
                        subtitle = "Authenticate to reset item protection",
                        onSuccess = {
                            dismiss()
                            onRecoveryAuthorized?.invoke()
                        },
                        onError = { err ->
                            Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    promptMasterCodeForRecovery()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptMasterCodeForRecovery() {
        val app = requireActivity().application as LifeLockerApp
        val etPin = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter Master PIN"
        }
        val container = android.widget.FrameLayout(requireContext()).apply {
            setPadding(40, 20, 40, 20)
            addView(etPin)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Master Recovery")
            .setMessage("Enter Master PIN to authorize item reset")
            .setView(container)
            .setPositiveButton("Authorize") { _, _ ->
                val pin = etPin.text.toString().trim()
                if (app.securityManager.validatePin(pin)) {
                    dismiss()
                    onRecoveryAuthorized?.invoke()
                } else {
                    Toast.makeText(requireContext(), "Incorrect Master PIN", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ItemProtectionBottomSheet"
    }
}
