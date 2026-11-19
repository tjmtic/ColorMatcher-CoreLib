package com.abyxcz.colormatcher.core.color

import kotlin.test.Test
import kotlin.test.assertEquals

class SrgbArgbTest {
    @Test
    fun readsRedGreenBlueAndIgnoresAlpha() {
        assertEquals(Srgb(0x12, 0x34, 0x56), Srgb.fromArgb(0xFF123456.toInt()))
        assertEquals(Srgb(0x12, 0x34, 0x56), Srgb.fromArgb(0x00123456))
        assertEquals(Srgb(255, 255, 255), Srgb.fromArgb(-1))
    }
}
