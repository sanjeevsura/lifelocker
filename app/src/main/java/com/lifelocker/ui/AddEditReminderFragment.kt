package com.lifelocker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.lifelocker.LifeLockerApp
import com.lifelocker.R
import com.lifelocker.data.ReminderItem
import com.lifelocker.data.ReminderRepository
import com.lifelocker.databinding.FragmentAddEditReminderBinding
import com.lifelocker.viewmodel.ReminderViewModel
import com.lifelocker.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditReminderFragment : Fragment() {

    private var _binding: FragmentAddEditReminderBinding? = null
    private val binding get() = _binding!!

    private val reminderViewModel: ReminderViewModel by viewModels {
        val app = requireActivity().application as LifeLockerApp
        ViewModelFactory(reminderRepository = ReminderRepository(app.database.reminderDao()))
    }

    private var selectedDueDateMillis: Long = System.currentTimeMillis() + (3600 * 1000 * 24)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read pre-filled arguments if launched from Document Detail
        arguments?.let { bundle ->
            val passedTitle = bundle.getString("title", "")
            val passedCategory = bundle.getString("category", "")
            val passedDueDate = bundle.getString("dueDate", "")

            if (!passedTitle.isNullOrEmpty()) {
                binding.etReminderTitle.setText(passedTitle)
            }
            if (!passedCategory.isNullOrEmpty()) {
                binding.etReminderDescription.setText("Category: $passedCategory")
            }
            if (!passedDueDate.isNullOrEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val parsedDate = sdf.parse(passedDueDate)
                    if (parsedDate != null) {
                        selectedDueDateMillis = parsedDate.time
                    }
                } catch (e: Exception) {
                    // Default tomorrow
                }
            }
        }

        updateDateTimeDisplay()

        binding.etReminderDatetime.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(selectedDueDateMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(10)
                    .setMinute(0)
                    .setTitleText("Select Due Time")
                    .build()

                timePicker.addOnPositiveButtonClickListener {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = selectedDateMillis
                    cal.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    cal.set(Calendar.MINUTE, timePicker.minute)
                    selectedDueDateMillis = cal.timeInMillis
                    updateDateTimeDisplay()
                }
                timePicker.show(parentFragmentManager, "TIME_PICKER")
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.btnSaveReminder.setOnClickListener {
            val title = binding.etReminderTitle.text.toString().trim()
            val description = binding.etReminderDescription.text.toString().trim()
            val priority = when (binding.rgPriority.checkedRadioButtonId) {
                R.id.rb_low -> "LOW"
                R.id.rb_high -> "HIGH"
                else -> "MEDIUM"
            }

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = ReminderItem(
                title = title,
                description = description,
                dueDateMillis = selectedDueDateMillis,
                priority = priority
            )

            reminderViewModel.addReminder(item) {
                Toast.makeText(requireContext(), "Reminder scheduled", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun updateDateTimeDisplay() {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        binding.etReminderDatetime.setText(format.format(Date(selectedDueDateMillis)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

