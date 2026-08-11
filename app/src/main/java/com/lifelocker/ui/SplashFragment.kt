package com.lifelocker.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lifelocker.R
import com.lifelocker.databinding.FragmentSplashBinding
import com.lifelocker.utils.SessionManager

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Logo scale & pulse 3D entrance animation
        binding.ivSplashLogo.scaleX = 0.3f
        binding.ivSplashLogo.scaleY = 0.3f
        binding.ivSplashLogo.alpha = 0f

        binding.ivSplashLogo.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .alpha(1f)
            .setDuration(1200)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                // Continuous 3D glowing pulse
                val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    binding.ivSplashGlow,
                    PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.25f, 1.0f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.25f, 1.0f),
                    PropertyValuesHolder.ofFloat("alpha", 0.2f, 0.45f, 0.2f)
                ).apply {
                    duration = 2000
                    repeatCount = ObjectAnimator.INFINITE
                    start()
                }
            }

        // 5-Second Countdown Timer with progress bar
        val totalMs = 5000L
        val intervalMs = 50L
        timer = object : CountDownTimer(totalMs, intervalMs) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = (((totalMs - millisUntilFinished).toFloat() / totalMs) * 100).toInt()
                _binding?.pbSplashLoading?.progress = progress
                
                when (progress) {
                    20 -> _binding?.tvSplashStatus?.text = "Initializing Encrypted Keyring..."
                    50 -> _binding?.tvSplashStatus?.text = "Loading Secure Storage Engine..."
                    80 -> _binding?.tvSplashStatus?.text = "Verifying Vault Integrity..."
                    95 -> _binding?.tvSplashStatus?.text = "Ready!"
                }
            }

            override fun onFinish() {
                _binding?.pbSplashLoading?.progress = 100
                if (isAdded) {
                    val targetDest = if (SessionManager.isActive.value) {
                        R.id.nav_dashboard
                    } else {
                        R.id.nav_lock
                    }
                    findNavController().navigate(
                        targetDest,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.nav_splash, true)
                            .build()
                    )
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        timer = null
        _binding = null
    }
}
