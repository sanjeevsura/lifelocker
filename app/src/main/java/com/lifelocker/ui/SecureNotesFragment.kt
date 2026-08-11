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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.SecureNote
import com.lifelocker.data.SecureNoteRepository
import com.lifelocker.databinding.FragmentSecureNotesBinding
import com.lifelocker.ui.adapters.SecureNoteAdapter
import com.lifelocker.viewmodel.SecureNoteViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SecureNotesFragment : Fragment() {

    private var _binding: FragmentSecureNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SecureNoteViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(secureNoteRepository = SecureNoteRepository(app.database.secureNoteDao()))
    }

    private lateinit var adapter: SecureNoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSecureNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SecureNoteAdapter(
            onItemClick = { note ->
                val bundle = Bundle().apply { putInt("noteId", note.id) }
                findNavController().navigate(R.id.action_notes_to_addEdit, bundle)
            },
            onFavoriteToggle = { viewModel.toggleFavorite(it) },
            onTrashClick = { note ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Move to Trash")
                    .setMessage("Move \"${note.title}\" to trash?")
                    .setPositiveButton("Move") { _, _ -> viewModel.moveToTrash(note) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collectLatest { notes ->
                adapter.submitList(notes)
                binding.emptyStateNotes.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
                binding.rvNotes.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        binding.searchViewNotes.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { viewModel.setSearchQuery(query.orEmpty()) }
            override fun onQueryTextChange(newText: String?) = true.also { viewModel.setSearchQuery(newText.orEmpty()) }
        })

        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_notes_to_addEdit)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
