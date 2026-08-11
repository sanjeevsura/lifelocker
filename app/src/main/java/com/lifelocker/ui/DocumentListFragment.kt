package com.lifelocker.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.Document
import com.lifelocker.data.DocumentRepository
import com.lifelocker.databinding.FragmentDocumentListBinding
import com.lifelocker.ui.adapters.DocumentAdapter
import com.lifelocker.utils.FileStorageHelper
import com.lifelocker.viewmodel.DocumentSortOrder
import com.lifelocker.viewmodel.DocumentViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DocumentListFragment : Fragment() {

    private var _binding: FragmentDocumentListBinding? = null
    private val binding get() = _binding!!

    private val documentViewModel: DocumentViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(documentRepository = DocumentRepository(app.database.documentDao()))
    }

    private lateinit var adapter: DocumentAdapter
    private var pendingExportDocument: Document? = null
    private var rawDocumentList = emptyList<Document>()

    // SAF Import Launcher
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            documentViewModel.importFileFromUri(requireContext(), uri) { success, fileName ->
                if (success) {
                    Snackbar.make(binding.root, "$fileName imported securely to Vault.", Snackbar.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to import file: $fileName", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // SAF Export Launcher
    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { targetUri: Uri? ->
        val doc = pendingExportDocument
        if (targetUri != null && doc != null && doc.filePath.isNotEmpty()) {
            val success = FileStorageHelper.exportFileToUri(requireContext(), doc.filePath, targetUri)
            if (success) {
                Snackbar.make(binding.root, "File exported successfully.", Snackbar.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Failed to export file", Toast.LENGTH_SHORT).show()
            }
        }
        pendingExportDocument = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DocumentAdapter(
            onItemClick = { doc ->
                val bundle = Bundle().apply { putInt("documentId", doc.id) }
                findNavController().navigate(R.id.action_documents_to_detail, bundle)
            },
            onItemLongClick = { doc ->
                enterMultiSelectMode(doc)
            },
            onFavoriteClick = { doc -> documentViewModel.toggleFavorite(doc) },
            onOpenClick = { doc -> handleOpenFile(doc) },
            onShareClick = { doc -> handleShareFile(doc) },
            onExportClick = { doc -> handleExportFile(doc) },
            onDeleteClick = { doc -> handleDeleteFile(doc) },
            onAddReminderClick = { doc ->
                val bundle = Bundle().apply {
                    putInt("documentId", doc.id)
                    putString("title", "Expiry: ${doc.title}")
                    putString("category", doc.category)
                    putString("dueDate", doc.expiryDate)
                }
                findNavController().navigate(R.id.action_documents_to_add_reminder, bundle)
            },
            onSelectionToggle = {
                updateMultiSelectHeader()
            }
        )

        binding.rvDocuments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDocuments.adapter = adapter

        // Observer for Documents list
        viewLifecycleOwner.lifecycleScope.launch {
            documentViewModel.documents.collectLatest { list ->
                rawDocumentList = list
                updateVaultSummary(list)
                applyFilter(list)
            }
        }

        // Filter chips listener
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, _ ->
            applyFilter(rawDocumentList)
        }

        // Search bar listener
        binding.searchViewDocuments.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                documentViewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                documentViewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        // Sort Button
        binding.btnSortDocuments.setOnClickListener { view ->
            showSortPopupMenu(view)
        }

        // FAB + Add / Import buttons
        binding.fabAddDocument.setOnClickListener {
            findNavController().navigate(R.id.action_documents_to_addEdit)
        }

        binding.btnEmptyAdd.setOnClickListener {
            findNavController().navigate(R.id.action_documents_to_addEdit)
        }

        binding.btnEmptyImport.setOnClickListener {
            importLauncher.launch(arrayOf("*/*"))
        }

        // Multi-select bar buttons
        binding.btnSelectAll.setOnClickListener {
            adapter.selectAll(rawDocumentList)
            updateMultiSelectHeader()
        }

        binding.btnDeleteSelected.setOnClickListener {
            val selectedIds = adapter.selectedDocumentIds.toList()
            val toDelete = rawDocumentList.filter { selectedIds.contains(it.id) }
            if (toDelete.isNotEmpty()) {
                documentViewModel.deleteMultipleDocuments(toDelete)
                Snackbar.make(binding.root, "${toDelete.size} items deleted", Snackbar.LENGTH_LONG).show()
                exitMultiSelectMode()
            }
        }

        binding.btnCancelSelection.setOnClickListener {
            exitMultiSelectMode()
        }
    }

    private fun updateVaultSummary(list: List<Document>) {
        binding.tvSummaryTotal.text = list.size.toString()
        val favoritesCount = list.count { it.isFavorite }
        binding.tvSummaryFavorites.text = favoritesCount.toString()
        val expiringCount = list.count { !it.expiryDate.isNullOrEmpty() }
        binding.tvSummaryExpiring.text = expiringCount.toString()
    }

    private fun applyFilter(rawList: List<Document>) {
        val selectedChipId = binding.chipGroupFilters.checkedChipId
        val filtered = when (selectedChipId) {
            R.id.chip_favorites -> rawList.filter { it.isFavorite }
            R.id.chip_documents -> rawList.filter { it.category.contains("Document", ignoreCase = true) || it.mimeType.contains("pdf", ignoreCase = true) }
            R.id.chip_ids -> rawList.filter { it.category.contains("ID", ignoreCase = true) || it.category.contains("Passport", ignoreCase = true) || it.category.contains("Identity", ignoreCase = true) }
            R.id.chip_certificates -> rawList.filter { it.category.contains("Certificate", ignoreCase = true) }
            R.id.chip_warranties -> rawList.filter { it.category.contains("Warranty", ignoreCase = true) }
            R.id.chip_invoices -> rawList.filter { it.category.contains("Invoice", ignoreCase = true) || it.category.contains("Finance", ignoreCase = true) }
            R.id.chip_insurance -> rawList.filter { it.category.contains("Insurance", ignoreCase = true) }
            R.id.chip_medical -> rawList.filter { it.category.contains("Medical", ignoreCase = true) || it.category.contains("Health", ignoreCase = true) }
            R.id.chip_travel -> rawList.filter { it.category.contains("Travel", ignoreCase = true) }
            R.id.chip_notes -> rawList.filter { it.category.contains("Note", ignoreCase = true) }
            else -> rawList
        }
        adapter.submitList(filtered)
        binding.emptyStateDocuments.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvDocuments.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showSortPopupMenu(view: View) {
        val popup = PopupMenu(view.context, view)
        popup.menu.add(0, 1, 0, "Favorites First")
        popup.menu.add(0, 2, 1, "Newest First")
        popup.menu.add(0, 3, 2, "Oldest First")
        popup.menu.add(0, 4, 3, "Name (A - Z)")
        popup.menu.add(0, 5, 4, "Name (Z - A)")
        popup.menu.add(0, 6, 5, "Expiry Soonest")

        popup.setOnMenuItemClickListener { menuItem ->
            val sortOrder = when (menuItem.itemId) {
                1 -> DocumentSortOrder.FAVORITES_FIRST
                2 -> DocumentSortOrder.NEWEST
                3 -> DocumentSortOrder.OLDEST
                4 -> DocumentSortOrder.NAME_AZ
                5 -> DocumentSortOrder.NAME_ZA
                6 -> DocumentSortOrder.EXPIRY_SOONEST
                else -> DocumentSortOrder.FAVORITES_FIRST
            }
            documentViewModel.setSortOrder(sortOrder)
            true
        }
        popup.show()
    }

    private fun enterMultiSelectMode(initialDoc: Document) {
        adapter.isMultiSelectMode = true
        adapter.toggleSelection(initialDoc.id)
        binding.layoutMultiSelectBar.visibility = View.VISIBLE
        updateMultiSelectHeader()
    }

    private fun updateMultiSelectHeader() {
        val count = adapter.selectedDocumentIds.size
        binding.tvSelectCount.text = "$count selected"
        if (count == 0) {
            exitMultiSelectMode()
        }
    }

    private fun exitMultiSelectMode() {
        adapter.clearSelection()
        adapter.isMultiSelectMode = false
        binding.layoutMultiSelectBar.visibility = View.GONE
    }

    private fun handleOpenFile(doc: Document) {
        if (doc.filePath.isEmpty()) {
            Toast.makeText(requireContext(), "Document: ${doc.title} (No file attached)", Toast.LENGTH_SHORT).show()
            return
        }
        val opened = FileStorageHelper.openFile(requireContext(), doc.filePath, doc.mimeType)
        if (!opened) {
            Toast.makeText(requireContext(), "Preview unavailable. You can Share or Export this file format.", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleShareFile(doc: Document) {
        if (doc.filePath.isNotEmpty()) {
            FileStorageHelper.shareFile(requireContext(), doc.filePath, doc.mimeType, doc.title)
        } else {
            Toast.makeText(requireContext(), "No file attachment to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleExportFile(doc: Document) {
        if (doc.filePath.isNotEmpty()) {
            pendingExportDocument = doc
            val defaultFileName = if (doc.originalExtension.isNotEmpty()) {
                "${doc.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")}.${doc.originalExtension}"
            } else {
                doc.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            }
            exportLauncher.launch(defaultFileName)
        } else {
            Toast.makeText(requireContext(), "No file attachment to export", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDeleteFile(doc: Document) {
        documentViewModel.deleteDocument(doc)
        Snackbar.make(binding.root, "Document '${doc.title}' deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                documentViewModel.addDocument(doc)
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


