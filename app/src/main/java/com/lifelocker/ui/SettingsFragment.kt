package com.lifelocker.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lifelocker.LifeLockerApp
import com.lifelocker.MainActivity
import com.lifelocker.R
import com.lifelocker.databinding.FragmentSettingsBinding
import com.lifelocker.utils.BackupManager
import com.lifelocker.utils.BiometricHelper
import com.lifelocker.utils.SecurityManager
import com.lifelocker.utils.SensitiveActionAuthenticator
import com.lifelocker.viewmodel.AuthViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(securityManager = app.securityManager)
    }

    private lateinit var sensitiveAuth: SensitiveActionAuthenticator
    private var suppressSwitchCallbacks = false

    private val autoLockLabels = listOf(
        "Immediately", "1 minute", "5 minutes", "10 minutes", "30 minutes", "Never"
    )
    private val autoLockValues = SecurityManager.AUTO_LOCK_OPTIONS

    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let { destinationUri ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val contentResolver = requireContext().contentResolver
                val outputStream = contentResolver.openOutputStream(destinationUri)
                val success = if (outputStream != null) {
                    BackupManager.createBackup(requireContext(), outputStream)
                } else false

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        if (success) "Encrypted backup saved successfully!" else "Failed to create encrypted backup",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private val restoreBackupLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val contentResolver = requireContext().contentResolver
                val inputStream = contentResolver.openInputStream(sourceUri)
                val success = if (inputStream != null) {
                    BackupManager.restoreBackup(requireContext(), inputStream)
                } else false

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        if (success) "Encrypted database restored successfully!" else "Failed to restore backup.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val biometricHelper = BiometricHelper(requireActivity())
        sensitiveAuth = SensitiveActionAuthenticator(
            requireActivity(),
            (requireActivity().application as LifeLockerApp).securityManager,
            biometricHelper
        )

        setupTheme()
        setupBiometricToggles(biometricHelper)
        setupAutoLockSpinner()
        setupScreenshotProtection()
        setupPinUpdate()
        setupBackupRestore()
        setupSecureNotes()
        setupLockNow()
    }

    private fun setupTheme() {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.rbThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        binding.rgThemeSelector.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rb_theme_light -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rb_theme_dark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun setupBiometricToggles(biometricHelper: BiometricHelper) {
        if (biometricHelper.hasFingerprintHardware()) {
            binding.layoutFingerprint.visibility = View.VISIBLE
            binding.dividerFingerprint.visibility = View.VISIBLE
            binding.switchFingerprint.isChecked = authViewModel.isFingerprintEnabled()
            binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
                if (suppressSwitchCallbacks) return@setOnCheckedChangeListener
                if (isChecked) {
                    authViewModel.setFingerprintEnabled(true)
                } else {
                    revertSwitch(binding.switchFingerprint, true)
                    requireAuth("Authenticate to disable fingerprint") {
                        authViewModel.setFingerprintEnabled(false)
                        revertSwitch(binding.switchFingerprint, false)
                    }
                }
            }
        }

        if (biometricHelper.hasFaceHardware()) {
            binding.layoutFaceUnlock.visibility = View.VISIBLE
            binding.dividerFace.visibility = View.VISIBLE
            binding.switchFaceUnlock.isChecked = authViewModel.isFaceUnlockEnabled()
            binding.switchFaceUnlock.setOnCheckedChangeListener { _, isChecked ->
                if (suppressSwitchCallbacks) return@setOnCheckedChangeListener
                if (isChecked) {
                    authViewModel.setFaceUnlockEnabled(true)
                } else {
                    revertSwitch(binding.switchFaceUnlock, true)
                    requireAuth("Authenticate to disable face unlock") {
                        authViewModel.setFaceUnlockEnabled(false)
                        revertSwitch(binding.switchFaceUnlock, false)
                    }
                }
            }
        }
    }

    private fun setupAutoLockSpinner() {
        binding.spinnerAutoLock.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            autoLockLabels
        )
        val currentMinutes = authViewModel.getAutoLockTimeoutMinutes()
        val index = autoLockValues.indexOf(currentMinutes).coerceAtLeast(0)
        binding.spinnerAutoLock.setSelection(index)

        binding.spinnerAutoLock.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                authViewModel.setAutoLockTimeoutMinutes(autoLockValues[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupScreenshotProtection() {
        binding.switchScreenshotProtection.isChecked = authViewModel.isScreenshotProtectionEnabled()
        binding.switchScreenshotProtection.setOnCheckedChangeListener { _, isChecked ->
            authViewModel.setScreenshotProtectionEnabled(isChecked)
            (requireActivity() as? MainActivity)?.applyScreenshotProtection()
        }
    }

    private fun setupPinUpdate() {
        binding.btnUpdatePin.setOnClickListener {
            val newPin = binding.etNewPin.text.toString().trim()
            if (newPin.length < 4) {
                Toast.makeText(requireContext(), "New PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            requireAuth("Authenticate to change master PIN") {
                if (authViewModel.setupPin(newPin)) {
                    binding.etNewPin.setText("")
                    Toast.makeText(requireContext(), "Master PIN updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBackupRestore() {
        binding.btnEncryptedBackup.setOnClickListener {
            requireAuth("Authenticate to export backup") {
                createBackupLauncher.launch("lifelocker_backup.enc")
            }
        }

        binding.btnRestoreDatabase.setOnClickListener {
            requireAuth("Authenticate to restore backup") {
                restoreBackupLauncher.launch(arrayOf("*/*"))
            }
        }
    }

    private fun setupSecureNotes() {
        binding.btnSecureNotes.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_notes)
        }
    }

    private fun setupLockNow() {
        binding.btnLockNow.setOnClickListener {
            authViewModel.lockApp()
            findNavController().navigate(
                R.id.nav_lock,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    private fun requireAuth(subtitle: String, onAuthenticated: () -> Unit) {
        sensitiveAuth.authenticate(subtitle = subtitle, onAuthenticated = onAuthenticated)
    }

    private fun revertSwitch(switch: com.google.android.material.materialswitch.MaterialSwitch, checked: Boolean) {
        suppressSwitchCallbacks = true
        switch.isChecked = checked
        suppressSwitchCallbacks = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
