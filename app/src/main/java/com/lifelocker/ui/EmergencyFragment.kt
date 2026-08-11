package com.lifelocker.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.EmergencyRepository
import com.lifelocker.databinding.FragmentEmergencyBinding
import com.lifelocker.ui.adapters.EmergencyAdapter
import com.lifelocker.viewmodel.EmergencyViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmergencyFragment : Fragment() {

    private var _binding: FragmentEmergencyBinding? = null
    private val binding get() = _binding!!

    private val emergencyViewModel: EmergencyViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(emergencyRepository = EmergencyRepository(app.database.emergencyDao()))
    }

    private lateinit var adapter: EmergencyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EmergencyAdapter(
            onItemClick = { contact ->
                val bundle = Bundle().apply { putInt("contactId", contact.id) }
                findNavController().navigate(R.id.action_emergency_to_addEdit, bundle)
            },
            onCallClick = { contact ->
                if (contact.phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.phone}")
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "No phone number available", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { contact ->
                emergencyViewModel.deleteContact(contact)
                Toast.makeText(requireContext(), "Emergency contact deleted", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvEmergencyContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEmergencyContacts.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            emergencyViewModel.contacts.collectLatest { list ->
                adapter.submitList(list)
                binding.emptyStateEmergency.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.searchViewEmergency.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                emergencyViewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                emergencyViewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        binding.fabAddEmergency.setOnClickListener {
            findNavController().navigate(R.id.action_emergency_to_addEdit)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
