package com.lifelocker.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.DocumentRepository
import com.lifelocker.data.EmergencyRepository
import com.lifelocker.data.ReminderRepository
import com.lifelocker.data.VaultRepository
import com.lifelocker.databinding.FragmentDashboardBinding
import com.lifelocker.utils.ExpiryHelper
import com.lifelocker.utils.FileStorageHelper
import com.lifelocker.viewmodel.DocumentViewModel
import com.lifelocker.viewmodel.EmergencyViewModel
import com.lifelocker.viewmodel.ReminderViewModel
import com.lifelocker.viewmodel.VaultViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    companion object {
        private const val TAG = "LL_Dashboard"
    }

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val vaultViewModel: VaultViewModel by viewModels {
        Log.d(TAG, "Creating VaultViewModel")
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(vaultRepository = VaultRepository(app.database.vaultDao()))
    }

    private val documentViewModel: DocumentViewModel by viewModels {
        Log.d(TAG, "Creating DocumentViewModel")
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(documentRepository = DocumentRepository(app.database.documentDao()))
    }

    private val reminderViewModel: ReminderViewModel by viewModels {
        Log.d(TAG, "Creating ReminderViewModel")
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(reminderRepository = ReminderRepository(app.database.reminderDao()))
    }

    private val emergencyViewModel: EmergencyViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(emergencyRepository = EmergencyRepository(app.database.emergencyDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Dashboard onCreateView")
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Dashboard onViewCreated")

        // Dynamic time-based greeting
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Good night"
        }
        binding.tvGreeting.text = greeting
        binding.tvSecurityStatus.text = "● Protected & Encrypted (Offline)"
        Log.d(TAG, "Dashboard UI basic setup done")

        Log.d(TAG, "Dashboard starting vaultCount observer")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                vaultViewModel.vaultCount.collectLatest { count ->
                    Log.d(TAG, "Dashboard vaultCount = $count")
                    binding.tvVaultCount.text = count.toString()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Dashboard vaultCount CRASH: ${e.message}", e)
            }
        }

        Log.d(TAG, "Dashboard starting documentCount observer")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                documentViewModel.documentCount.collectLatest { count ->
                    Log.d(TAG, "Dashboard documentCount = $count")
                    binding.tvDocumentCount.text = count.toString()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Dashboard documentCount CRASH: ${e.message}", e)
            }
        }

        Log.d(TAG, "Dashboard starting expiringDocuments observer")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                documentViewModel.expiringDocuments.collectLatest { list ->
                    Log.d(TAG, "Dashboard expiringDocuments size = ${list.size}")
                    val urgentCount = list.size
                    if (urgentCount > 0) {
                        val firstDoc = list.first()
                        binding.tvAttentionSummary.text = "⚠ ${firstDoc.title} (${firstDoc.category}) requires attention (Expires: ${firstDoc.expiryDate ?: "Soon"}). Total urgent: $urgentCount"
                        binding.tvAttentionSummary.setTextColor(requireContext().getColor(R.color.warning))
                    } else {
                        binding.tvAttentionSummary.text = "✓ All documents & warranties are up to date."
                        binding.tvAttentionSummary.setTextColor(requireContext().getColor(R.color.text_primary))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Dashboard expiringDocuments CRASH: ${e.message}", e)
            }
        }

        Log.d(TAG, "Dashboard starting reminderCount observer")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                reminderViewModel.activeReminderCount.collectLatest { count ->
                    Log.d(TAG, "Dashboard reminderCount = $count")
                    binding.tvReminderCount.text = count.toString()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Dashboard reminderCount CRASH: ${e.message}", e)
            }
        }

        binding.cardVault.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_vault)
        }

        binding.cardDocuments.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_documents)
        }

        binding.cardReminders.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_reminders)
        }

        binding.cardEmergency.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_emergency)
        }

        binding.btnQuickAddDoc.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_documents)
        }

        binding.btnQuickAddVault.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_vault)
        }

        binding.btnQuickAddReminder.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_reminders)
        }

        binding.btnOpenSettings.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_settings)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Dashboard onResume")
    }

    override fun onDestroyView() {
        Log.d(TAG, "Dashboard onDestroyView")
        super.onDestroyView()
        _binding = null
    }
}
