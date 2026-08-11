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
import com.lifelocker.data.EmergencyContact
import com.lifelocker.data.EmergencyRepository
import com.lifelocker.databinding.FragmentAddEditContactBinding
import com.lifelocker.viewmodel.EmergencyViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class AddEditContactFragment : Fragment() {

    private var _binding: FragmentAddEditContactBinding? = null
    private val binding get() = _binding!!

    private val emergencyViewModel: EmergencyViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(emergencyRepository = EmergencyRepository(app.database.emergencyDao()))
    }

    private var contactId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactId = arguments?.getInt("contactId", 0) ?: 0

        if (contactId != 0) {
            binding.tvContactFormTitle.text = "Edit Emergency Contact"
            viewLifecycleOwner.lifecycleScope.launch {
                val contact = emergencyViewModel.getContactById(contactId)
                contact?.let {
                    binding.etContactName.setText(it.name)
                    binding.etContactRelationship.setText(it.relationship)
                    binding.etContactPhone.setText(it.phone)
                    binding.etContactBloodGroup.setText(it.bloodGroup)
                    binding.etContactAllergies.setText(it.allergies)
                    binding.etContactConditions.setText(it.conditions)
                    binding.etContactMedicines.setText(it.medicines)
                    binding.etContactDoctor.setText(it.doctor)
                    binding.etContactHospital.setText(it.hospital)
                    binding.etContactInsurance.setText(it.insurance)
                    binding.etContactMedicalNotes.setText(it.medicalNotes)
                }
            }
        }

        binding.btnSaveContact.setOnClickListener {
            val name = binding.etContactName.text.toString().trim()
            val relationship = binding.etContactRelationship.text.toString().trim().ifEmpty { "Family" }
            val phone = binding.etContactPhone.text.toString().trim()
            val bloodGroup = binding.etContactBloodGroup.text.toString().trim().ifEmpty { "Unknown" }
            val allergies = binding.etContactAllergies.text.toString().trim()
            val conditions = binding.etContactConditions.text.toString().trim()
            val medicines = binding.etContactMedicines.text.toString().trim()
            val doctor = binding.etContactDoctor.text.toString().trim()
            val hospital = binding.etContactHospital.text.toString().trim()
            val insurance = binding.etContactInsurance.text.toString().trim()
            val medicalNotes = binding.etContactMedicalNotes.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Name and Phone number are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val contact = EmergencyContact(
                id = contactId,
                name = name,
                relationship = relationship,
                phone = phone,
                bloodGroup = bloodGroup,
                allergies = allergies,
                conditions = conditions,
                medicines = medicines,
                doctor = doctor,
                hospital = hospital,
                insurance = insurance,
                medicalNotes = medicalNotes,
                isPrimary = contactId == 0 // new contacts default to primary
            )

            if (contactId == 0) {
                emergencyViewModel.addContact(contact) {
                    Toast.makeText(requireContext(), "Emergency contact saved", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } else {
                emergencyViewModel.updateContact(contact)
                Toast.makeText(requireContext(), "Contact updated", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
