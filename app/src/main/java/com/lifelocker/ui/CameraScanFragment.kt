package com.lifelocker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.lifelocker.databinding.FragmentCameraScanBinding
import com.lifelocker.utils.FileStorageHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * CameraX-based document scanner fragment.
 * Capture → Preview → Retake/Accept → navigates back with file path argument.
 * OCR is intentionally NOT implemented - left as a clean future extension point.
 */
class CameraScanFragment : Fragment() {

    private var _binding: FragmentCameraScanBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var capturedFile: File? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to scan documents.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCameraBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCapture.setOnClickListener {
            capturePhoto()
        }

        binding.btnRetake.setOnClickListener {
            capturedFile?.delete()
            capturedFile = null
            showCameraMode()
            startCamera()
        }

        binding.btnAcceptCapture.setOnClickListener {
            val path = capturedFile?.absolutePath
            if (!path.isNullOrEmpty()) {
                // Navigate back to AddEditDocument with captured file path
                val bundle = Bundle().apply {
                    putString("capturedFilePath", path)
                }
                // Pop back and let the caller handle the result
                requireActivity().supportFragmentManager.setFragmentResult("camera_capture_result", bundle)
                findNavController().navigateUp()
            }
        }

        checkAndRequestCameraPermission()
    }

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = FileStorageHelper.getDocsDir(requireContext())
        val photoFile = File(storageDir, "SCAN_${timeStamp}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    if (_binding == null) return
                    capturedFile = photoFile
                    showPostCaptureMode(photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showCameraMode() {
        binding.btnCapture.visibility = View.VISIBLE
        binding.layoutPostCapture.visibility = View.GONE
        binding.ivCapturePreview.visibility = View.GONE
        binding.tvCameraHint.text = "Position document in frame and capture"
    }

    private fun showPostCaptureMode(file: File) {
        binding.btnCapture.visibility = View.GONE
        binding.layoutPostCapture.visibility = View.VISIBLE
        binding.ivCapturePreview.visibility = View.VISIBLE
        binding.tvCameraHint.text = "Review capture. Retake or accept."
        binding.ivCapturePreview.load(Uri.fromFile(file))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
