package com.lifelocker.ui

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
import com.lifelocker.data.ReminderRepository
import com.lifelocker.databinding.FragmentReminderListBinding
import com.lifelocker.ui.adapters.ReminderAdapter
import com.lifelocker.viewmodel.ReminderViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReminderListFragment : Fragment() {

    private var _binding: FragmentReminderListBinding? = null
    private val binding get() = _binding!!

    private val reminderViewModel: ReminderViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(reminderRepository = ReminderRepository(app.database.reminderDao()))
    }

    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReminderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReminderAdapter(
            onItemClick = { reminder ->
                val bundle = Bundle().apply { putInt("reminderId", reminder.id) }
                findNavController().navigate(R.id.action_reminders_to_addEdit, bundle)
            },
            onToggleComplete = { reminder ->
                reminderViewModel.toggleCompletion(reminder)
            },
            onDeleteClick = { reminder ->
                reminderViewModel.deleteReminder(reminder)
                Toast.makeText(requireContext(), "Reminder deleted", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            reminderViewModel.reminders.collectLatest { list ->
                adapter.submitList(list)
                binding.emptyStateReminders.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Connect filter chips
        binding.chipGroupReminders.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                reminderViewModel.setCategoryFilter(null)
                return@setOnCheckedStateChangeListener
            }
            val filter = when (checkedIds.first()) {
                R.id.chip_today -> "Today"
                R.id.chip_upcoming -> "Upcoming"
                R.id.chip_overdue -> "Overdue"
                R.id.chip_done -> "Done"
                else -> null
            }
            reminderViewModel.setCategoryFilter(filter)
        }

        binding.searchViewReminders.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                reminderViewModel.setSearchQuery(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                reminderViewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        binding.fabAddReminder.setOnClickListener {
            findNavController().navigate(R.id.action_reminders_to_addEdit)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
