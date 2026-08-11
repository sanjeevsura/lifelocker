package com.lifelocker.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.Document
import com.lifelocker.data.DocumentRepository
import com.lifelocker.databinding.FragmentDocumentDetailBinding
import com.lifelocker.utils.ExpiryHelper
import com.lifelocker.utils.FileStorageHelper
import com.lifelocker.viewmodel.DocumentViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class DocumentDetailFragment : Fragment() {

    private var _binding: FragmentDocumentDetailBinding? = null
    private val binding get() = _binding!!

    private val documentViewModel: DocumentViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(documentRepository = DocumentRepository(app.database.documentDao()))
    }

    private var documentId: Int = 0
    private var currentDocument: Document? = null

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { targetUri: Uri? ->
        val doc = currentDocument
        if (targetUri != null && doc != null && doc.filePath.isNotEmpty()) {
            val success = FileStorageHelper.exportFileToUri(requireContext(), doc.filePath, targetUri)
            if (success) {
                Snackbar.make(binding.root, "File exported successfully.", Snackbar.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Failed to export file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            documentId = it.getInt("documentId", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val doc = documentViewModel.getDocumentById(documentId)
            if (doc != null) {
                currentDocument = doc
                bindDocument(doc)
            } else {
                Toast.makeText(requireContext(), "Document not found", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        binding.btnDetailFavorite.setOnClickListener {
            currentDocument?.let { doc ->
                documentViewModel.toggleFavorite(doc)
                val newFavState = !doc.isFavorite
                currentDocument = doc.copy(isFavorite = newFavState)
                binding.btnDetailFavorite.setImageResource(
                    if (newFavState) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
                )
                Toast.makeText(
                    requireContext(),
                    if (newFavState) "Added to Favorites" else "Removed from Favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnDetailEdit.setOnClickListener {
            currentDocument?.let { doc ->
                val bundle = Bundle().apply {
                    putInt("documentId", doc.id)
                }
                findNavController().navigate(R.id.action_detail_to_edit_document, bundle)
            }
        }

        binding.btnDetailOpen.setOnClickListener {
            currentDocument?.let { doc ->
                if (doc.isEncrypted) {
                    promptDocumentAuth(doc) {
                        performOpenFile(doc)
                    }
                } else {
                    performOpenFile(doc)
                }
            }
        }

        binding.btnDetailShare.setOnClickListener {
            currentDocument?.let { doc ->
                if (doc.isEncrypted) {
                    promptDocumentAuth(doc) {
                        FileStorageHelper.shareFile(requireContext(), doc.filePath, doc.mimeType, doc.title)
                    }
                } else {
                    if (doc.filePath.isNotEmpty()) {
                        FileStorageHelper.shareFile(requireContext(), doc.filePath, doc.mimeType, doc.title)
                    } else {
                        Toast.makeText(requireContext(), "No file attachment to share", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnDetailExport.setOnClickListener {
            currentDocument?.let { doc ->
                if (doc.isEncrypted) {
                    promptDocumentAuth(doc) {
                        performExportFile(doc)
                    }
                } else {
                    performExportFile(doc)
                }
            }
        }

        binding.btnDetailAddReminder.setOnClickListener {
            currentDocument?.let { doc ->
                val bundle = Bundle().apply {
                    putInt("reminderId", 0)
                    putString("title", "Expiry: ${doc.title}")
                    putString("category", doc.category)
                    putString("dueDate", doc.expiryDate ?: "")
                }
                findNavController().navigate(R.id.action_detail_to_add_reminder, bundle)
            }
        }

        binding.btnDetailDelete.setOnClickListener {
            currentDocument?.let { doc ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Move to Trash?")
                    .setMessage("Are you sure you want to move \"${doc.title}\" to trash?")
                    .setPositiveButton("Move to Trash") { _, _ ->
                        documentViewModel.moveToTrash(doc)
                        Toast.makeText(requireContext(), "Moved to Trash", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun bindDocument(doc: Document) {
        binding.tvDetailTitle.text = doc.title
        binding.tvDetailCategory.text = doc.category
        binding.btnDetailFavorite.setImageResource(
            if (doc.isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        )

        // Single Source of Truth Expiry Status
        val expiryStatus = ExpiryHelper.calculateExpiryStatus(doc.expiryDate)
        binding.tvDetailStatusChip.text = expiryStatus.statusText
        binding.tvDetailStatusChip.setTextColor(requireContext().getColor(expiryStatus.colorResId))

        if (!doc.expiryDate.isNullOrEmpty()) {
            binding.tvDetailExpiryDate.text = "Expiry Date: ${doc.expiryDate}"
        } else {
            binding.tvDetailExpiryDate.text = "Expiry Date: None"
        }

        val extStr = if (doc.originalExtension.isNotEmpty()) doc.originalExtension.uppercase(Locale.getDefault()) else "FILE"
        val sizeStr = if (doc.fileSize > 0) FileStorageHelper.formatFileSize(doc.fileSize) else "0 B"
        binding.tvDetailFileExt.text = "Format: $extStr (${doc.mimeType})"
        binding.tvDetailFileSize.text = "Size: $sizeStr"
        binding.tvDetailFilePath.text = "Storage: ${doc.filePath.ifEmpty { "No file attached" }}"

        val notesText = StringBuilder()
        if (doc.fileType.isNotEmpty()) {
            notesText.append("Type: ").append(doc.fileType).append("\n")
        }
        if (!doc.tags.isNullOrEmpty()) {
            notesText.append("Tags: ").append(doc.tags).append("\n")
        }
        if (doc.notes.isNotEmpty()) {
            notesText.append("Notes: ").append(doc.notes)
        }
        binding.tvDetailNotes.text = if (notesText.isNotEmpty()) notesText.toString() else "No additional notes or metadata recorded."

        // Thumbnail Preview
        loadAttachmentPreview(doc)
    }

    private fun loadAttachmentPreview(doc: Document) {
        if (doc.filePath.isEmpty()) {
            binding.ivDocumentPreview.visibility = View.GONE
            binding.layoutPreviewUnavailable.visibility = View.VISIBLE
            binding.tvPreviewStatus.text = "No file attached"
            return
        }

        val mime = doc.mimeType.lowercase(Locale.getDefault())
        val ext = doc.originalExtension.lowercase(Locale.getDefault())
        val isImage = mime.startsWith("image/") || ext in listOf("jpg", "jpeg", "png", "webp")

        if (isImage) {
            val file = File(doc.filePath)
            if (file.exists()) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeFile(file.absolutePath, options)
                        options.inSampleSize = calculateInSampleSize(options, 400, 300)
                        options.inJustDecodeBounds = false

                        val bmp = BitmapFactory.decodeFile(file.absolutePath, options)
                        withContext(Dispatchers.Main) {
                            if (_binding != null && bmp != null) {
                                binding.ivDocumentPreview.setImageBitmap(bmp)
                                binding.ivDocumentPreview.visibility = View.VISIBLE
                                binding.layoutPreviewUnavailable.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            if (_binding != null) {
                                binding.ivDocumentPreview.visibility = View.GONE
                                binding.layoutPreviewUnavailable.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            } else {
                binding.ivDocumentPreview.visibility = View.GONE
                binding.layoutPreviewUnavailable.visibility = View.VISIBLE
                binding.tvPreviewStatus.text = "File not found on local storage"
            }
        } else {
            binding.ivDocumentPreview.visibility = View.GONE
            binding.layoutPreviewUnavailable.visibility = View.VISIBLE
            binding.tvPreviewStatus.text = "Preview unavailable for ${ext.uppercase(Locale.getDefault())} format"
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun performOpenFile(doc: Document) {
        if (doc.filePath.isNotEmpty()) {
            val opened = FileStorageHelper.openFile(requireContext(), doc.filePath, doc.mimeType)
            if (!opened) {
                Toast.makeText(requireContext(), "No application found to open this file. Use Share or Export.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "No file attachment to open", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performExportFile(doc: Document) {
        if (doc.filePath.isNotEmpty()) {
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

    private fun promptDocumentAuth(doc: Document, onSuccess: () -> Unit) {
        val app = requireActivity().application as LifeLockerApp
        val sensitiveAuth = com.lifelocker.utils.SensitiveActionAuthenticator(
            requireActivity(),
            app.securityManager,
            com.lifelocker.utils.BiometricHelper(requireActivity())
        )
        sensitiveAuth.authenticate(
            itemTitle = doc.title,
            subtitle = "Verify your identity to access this protected document.",
            onAuthenticated = onSuccess
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

