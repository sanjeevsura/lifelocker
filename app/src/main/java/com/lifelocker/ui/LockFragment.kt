package com.lifelocker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.databinding.FragmentLockBinding
import com.lifelocker.utils.BiometricCapability
import com.lifelocker.utils.BiometricHelper
import com.lifelocker.viewmodel.AuthViewModel
import com.lifelocker.viewmodel.ViewModelFactory

class LockFragment : Fragment() {

    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(securityManager = app.securityManager)
    }

    private lateinit var biometricHelper: BiometricHelper
    private var isFirstTimeSetup = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        biometricHelper = BiometricHelper(requireActivity())
        isFirstTimeSetup = !authViewModel.isPinSet()

        setupUiForMode()
        setupClickListeners()

        if (!isFirstTimeSetup && shouldShowBiometricUnlock()) {
            binding.root.post { triggerBiometricUnlock() }
        }
    }

    private fun setupUiForMode() {
        if (isFirstTimeSetup) {
            binding.tvLockTitle.text = "Create Master PIN"
            binding.tvLockSubtitle.text = "Create a 4 to 8 digit PIN for LifeLocker"
            binding.btnUnlock.text = "Continue"
            binding.tilPinConfirm.visibility = View.VISIBLE
            binding.btnBiometric.visibility = View.GONE
        } else {
            binding.tvLockTitle.text = "LifeLocker"
            binding.tvLockSubtitle.text = "Authenticate to access your secure information"
            binding.btnUnlock.text = "Use PIN instead"
            binding.tilPinConfirm.visibility = View.GONE

            if (shouldShowBiometricUnlock()) {
                binding.btnBiometric.visibility = View.VISIBLE
                binding.btnBiometric.text = biometricHelper.getUnlockButtonLabel()
            } else {
                binding.btnBiometric.visibility = View.GONE
                val message = biometricHelper.getCapabilityMessage()
                if (message.isNotEmpty()) {
                    binding.tvLockSubtitle.text = message
                }
            }
        }
    }

    private fun shouldShowBiometricUnlock(): Boolean {
        return authViewModel.isBiometricEnabled() &&
            biometricHelper.getCapability() == BiometricCapability.AVAILABLE
    }

    private fun setupClickListeners() {
        binding.btnUnlock.setOnClickListener {
            if (isFirstTimeSetup) {
                handleFirstTimePinSetup()
            } else {
                handlePinUnlock()
            }
        }

        binding.btnBiometric.setOnClickListener {
            triggerBiometricUnlock()
        }

        binding.btnEmergency.setOnClickListener {
            findNavController().navigate(R.id.action_lock_to_emergency)
        }
    }

    private fun handleFirstTimePinSetup() {
        val pin = binding.etPin.text.toString().trim()
        val confirmPin = binding.etPinConfirm.text.toString().trim()

        if (pin.length < 4) {
            Toast.makeText(requireContext(), "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
            return
        }
        if (pin != confirmPin) {
            Toast.makeText(requireContext(), "PINs do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (authViewModel.setupPin(pin)) {
            offerBiometricSetup()
        } else {
            Toast.makeText(requireContext(), "Failed to set PIN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun offerBiometricSetup() {
        val capability = biometricHelper.getCapability()
        if (capability != BiometricCapability.AVAILABLE) {
            navigateToDashboard()
            return
        }

        val label = biometricHelper.getUnlockButtonLabel()
        AlertDialog.Builder(requireContext())
            .setTitle("Enable biometric unlock?")
            .setMessage("Use $label for faster access. You can change this in Settings.")
            .setPositiveButton(label) { _, _ ->
                authViewModel.setBiometricEnabled(true)
                if (biometricHelper.hasFingerprintHardware()) {
                    authViewModel.setFingerprintEnabled(true)
                }
                if (biometricHelper.hasFaceHardware()) {
                    authViewModel.setFaceUnlockEnabled(true)
                }
                navigateToDashboard()
            }
            .setNegativeButton("Not now") { _, _ ->
                navigateToDashboard()
            }
            .setCancelable(false)
            .show()
    }

    private fun handlePinUnlock() {
        val pin = binding.etPin.text.toString().trim()
        if (pin.length < 4) {
            Toast.makeText(requireContext(), "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
            return
        }
        if (authViewModel.validatePin(pin)) {
            binding.etPin.setText("")
            navigateToDashboard()
        } else {
            Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerBiometricUnlock() {
        biometricHelper.showBiometricPrompt(
            title = "LifeLocker",
            subtitle = "Authenticate to access your secure information",
            description = biometricHelper.getBiometricDescription(),
            negativeButtonText = "Use PIN",
            onSuccess = {
                authViewModel.onBiometricSuccess()
                navigateToDashboard()
            },
            onError = { _ ->
                // User cancelled or failed — PIN field remains available
            },
            onNegativeButton = {
                binding.etPin.requestFocus()
            }
        )
    }

    private fun navigateToDashboard() {
        findNavController().navigate(R.id.action_lock_to_dashboard)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
