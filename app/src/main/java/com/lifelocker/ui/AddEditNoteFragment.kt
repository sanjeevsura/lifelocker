package com.lifelocker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lifelocker.LifeLockerApp
import com.lifelocker.data.SecureNoteRepository
import com.lifelocker.databinding.FragmentAddEditNoteBinding
import com.lifelocker.viewmodel.SecureNoteViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class AddEditNoteFragment : Fragment() {

    private var _binding: FragmentAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SecureNoteViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(secureNoteRepository = SecureNoteRepository(app.database.secureNoteDao()))
    }

    private var noteId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteId = arguments?.getInt("noteId", 0) ?: 0

        if (noteId != 0) {
            binding.tvNoteFormTitle.text = "Edit Secure Note"
            viewLifecycleOwner.lifecycleScope.launch {
                val note = viewModel.getNoteById(noteId)
                note?.let {
                    binding.etNoteTitle.setText(it.title)
                    binding.etNoteContent.setText(viewModel.decryptContent(it))
                    binding.etNoteCategory.setText(it.category)
                    binding.etNoteTags.setText(it.tags)
                    binding.cbNoteFavorite.isChecked = it.isFavorite
                }
            }
        }

        binding.btnSaveNote.setOnClickListener {
            val title = binding.etNoteTitle.text.toString().trim()
            val content = binding.etNoteContent.text.toString()
            val category = binding.etNoteCategory.text.toString().trim().ifEmpty { "General" }
            val tags = binding.etNoteTags.text.toString().trim()
            val fav = binding.cbNoteFavorite.isChecked

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveNote(noteId, title, content, category, tags, fav) {
                Toast.makeText(requireContext(), "Note saved (encrypted)", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
