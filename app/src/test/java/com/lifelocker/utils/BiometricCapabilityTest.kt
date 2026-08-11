package com.lifelocker.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BiometricCapabilityTest {

    @Test
    fun getCapability_returnsKnownStatesWithoutCrash() {
        val capability = BiometricHelper.getCapability(
            org.robolectric.RuntimeEnvironment.getApplication()
        )
        assert(capability in BiometricCapability.entries)
    }
}
