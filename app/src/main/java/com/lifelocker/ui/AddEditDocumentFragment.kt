package com.lifelocker.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.Document
import com.lifelocker.data.DocumentRepository
import com.lifelocker.databinding.FragmentAddEditDocumentBinding
import com.lifelocker.utils.FileStorageHelper
import com.lifelocker.utils.StoredFileInfo
import com.lifelocker.viewmodel.DocumentViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditDocumentFragment : Fragment() {

    private var _binding: FragmentAddEditDocumentBinding? = null
    private val binding get() = _binding!!

    private val documentViewModel: DocumentViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(documentRepository = DocumentRepository(app.database.documentDao()))
    }

    private var documentId: Int = 0
    private var existingDocument: Document? = null
    private var currentStoredFileInfo: StoredFileInfo? = null
    private var tempCameraFile: File? = null
    private var tempCameraUri: Uri? = null

    private var issueDateStr: String? = null
    private var expiryDateStr: String? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { sourceUri ->
            val info = FileStorageHelper.saveUriToInternalStorage(requireContext(), sourceUri)
            if (info != null) {
                currentStoredFileInfo = info
                binding.tvSelectedFilePath.text = "Attached: ${info.fileName} (${FileStorageHelper.formatFileSize(info.fileSize)})"
                if (binding.etDocTitle.text.toString().isBlank()) {
                    binding.etDocTitle.setText(info.fileName.substringBeforeLast("."))
                }
                Toast.makeText(requireContext(), "File attached securely (${info.extension.uppercase(Locale.getDefault())})", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val cameraUri = Uri.fromFile(tempCameraFile)
            val info = FileStorageHelper.saveUriToInternalStorage(requireContext(), cameraUri, preferredName = "photo_${tempCameraFile!!.name}")
            if (info != null) {
                currentStoredFileInfo = info
                binding.tvSelectedFilePath.text = "Captured Photo: ${info.fileName}"
                if (binding.etDocTitle.text.toString().isBlank()) {
                    binding.etDocTitle.setText("Camera Document ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}")
                }
                Toast.makeText(requireContext(), "Photo saved to vault", Toast.LENGTH_SHORT).show()
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
        _binding = FragmentAddEditDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupDatePickers()

        if (documentId > 0) {
            binding.tvDocFormTitle.text = "Edit Document"
            binding.btnSaveDocument.text = "Update Document"
            loadExistingDocument()
        }

        binding.btnPickFile.setOnClickListener {
            filePickerLauncher.launch(arrayOf("*/*"))
        }

        binding.btnTakePhoto.setOnClickListener {
            // Navigate to CameraX scanner for higher quality document capture
            try {
                findNavController().navigate(R.id.action_add_edit_document_to_camera)
            } catch (e: Exception) {
                // Fallback: use old TakePicture launcher
                try {
                    val pair = FileStorageHelper.createTemporaryCameraFile(requireContext())
                    tempCameraFile = pair.first
                    tempCameraUri = pair.second
                    cameraLauncher.launch(tempCameraUri)
                } catch (ex: Exception) {
                    Toast.makeText(requireContext(), "Camera launch failed: ${ex.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Listen for result from CameraScanFragment
        parentFragmentManager.setFragmentResultListener("camera_capture_result", viewLifecycleOwner) { _, bundle ->
            val capturedPath = bundle.getString("capturedFilePath")
            if (!capturedPath.isNullOrEmpty()) {
                val file = File(capturedPath)
                if (file.exists()) {
                    val uri = Uri.fromFile(file)
                    val info = FileStorageHelper.saveUriToInternalStorage(requireContext(), uri, preferredName = file.name)
                    if (info != null) {
                        currentStoredFileInfo = info
                        binding.tvSelectedFilePath.text = "Scanned: ${info.fileName} (${FileStorageHelper.formatFileSize(info.fileSize)})"
                        if (binding.etDocTitle.text.toString().isBlank()) {
                            binding.etDocTitle.setText("Scanned Document ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}")
                        }
                        Toast.makeText(requireContext(), "Scanned photo attached to document", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnSaveDocument.setOnClickListener {
            saveDocument()
        }
    }

    private fun setupDropdowns() {
        val types = arrayOf(
            "Passport", "Driving License", "Aadhaar", "PAN", "ID Card",
            "Certificate", "Warranty", "Invoice", "Insurance", "Medical",
            "Travel", "Note", "Password Hint", "Other"
        )
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.etDocType.setAdapter(typeAdapter)

        val categories = arrayOf(
            "Personal", "Education", "Finance", "Medical",
            "Travel", "Vehicle", "Work", "Family", "Other"
        )
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.etDocCategory.setAdapter(catAdapter)
    }

    private fun setupDatePickers() {
        binding.etDocIssueDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Issue Date")
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                issueDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selection))
                binding.etDocIssueDate.setText(issueDateStr)
                validateDates()
            }
            datePicker.show(parentFragmentManager, "ISSUE_DATE_PICKER")
        }

        binding.etDocExpiry.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Expiry Date")
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                expiryDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selection))
                binding.etDocExpiry.setText(expiryDateStr)
                validateDates()
            }
            datePicker.show(parentFragmentManager, "EXPIRY_DATE_PICKER")
        }
    }

    private fun validateDates(): Boolean {
        if (!issueDateStr.isNullOrEmpty() && !expiryDateStr.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val issue = sdf.parse(issueDateStr!!)
                val expiry = sdf.parse(expiryDateStr!!)
                if (expiry != null && issue != null && expiry.before(issue)) {
                    binding.tvDateValidationError.visibility = View.VISIBLE
                    return false
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        binding.tvDateValidationError.visibility = View.GONE
        return true
    }

    private fun loadExistingDocument() {
        viewLifecycleOwner.lifecycleScope.launch {
            val doc = documentViewModel.getDocumentById(documentId)
            if (doc != null) {
                existingDocument = doc
                binding.etDocTitle.setText(doc.title)
                binding.etDocType.setText(doc.fileType, false)
                binding.etDocCategory.setText(doc.category, false)
                binding.etDocExpiry.setText(doc.expiryDate ?: "")
                expiryDateStr = doc.expiryDate
                binding.etDocTags.setText(doc.tags)
                binding.etDocNotes.setText(doc.notes)
                if (doc.filePath.isNotEmpty()) {
                    binding.tvSelectedFilePath.text = "Attached: ${doc.filePath.substringAfterLast('/')} (${FileStorageHelper.formatFileSize(doc.fileSize)})"
                }
            }
        }
    }

    private fun saveDocument() {
        val title = binding.etDocTitle.text.toString().trim()
        val docType = binding.etDocType.text.toString().trim().ifEmpty { "Document" }
        val category = binding.etDocCategory.text.toString().trim().ifEmpty { "General" }
        val expiry = binding.etDocExpiry.text.toString().trim()
        val tags = binding.etDocTags.text.toString().trim()
        val notes = binding.etDocNotes.text.toString().trim()
        val info = currentStoredFileInfo

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a document title", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateDates()) {
            Toast.makeText(requireContext(), "Expiry date cannot be before issue date", Toast.LENGTH_SHORT).show()
            return
        }

        val filePath = info?.absolutePath ?: existingDocument?.filePath ?: ""
        val mimeType = info?.mimeType ?: existingDocument?.mimeType ?: "*/*"
        val fileSize = info?.fileSize ?: existingDocument?.fileSize ?: 0L
        val originalExtension = info?.extension ?: existingDocument?.originalExtension ?: ""

        val doc = Document(
            id = documentId,
            title = title,
            category = category,
            filePath = filePath,
            expiryDate = expiry.ifEmpty { null },
            notes = notes,
            fileType = docType,
            mimeType = mimeType,
            fileSize = fileSize,
            originalExtension = originalExtension,
            isFavorite = existingDocument?.isFavorite ?: false,
            tags = tags,
            createdAt = existingDocument?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        if (documentId > 0) {
            documentViewModel.updateDocument(doc)
            Toast.makeText(requireContext(), "Document updated", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        } else {
            documentViewModel.addDocument(doc) {
                Toast.makeText(requireContext(), "Document stored in Vault", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


