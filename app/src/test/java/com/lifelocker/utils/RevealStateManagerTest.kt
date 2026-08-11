package com.lifelocker.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RevealStateManagerTest {

    @Before
    fun setup() {
        RevealStateManager.maskAll()
    }

    @After
    fun teardown() {
        RevealStateManager.maskAll()
    }

    @Test
    fun maskedDisplay_isNeverPlaintext() {
        assertEquals("••••••••••", RevealStateManager.getMaskedDisplay())
    }

    @Test
    fun reveal_marksItemAsRevealed() {
        RevealStateManager.reveal(1)
        assertTrue(RevealStateManager.isRevealed(1))
    }

    @Test
    fun maskAll_clearsAllRevealedItems() {
        RevealStateManager.reveal(1)
        RevealStateManager.reveal(2)
        RevealStateManager.maskAll()
        assertFalse(RevealStateManager.isRevealed(1))
        assertFalse(RevealStateManager.isRevealed(2))
    }
}
