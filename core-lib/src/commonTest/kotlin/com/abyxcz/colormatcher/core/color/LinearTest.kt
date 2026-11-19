package com.abyxcz.colormatcher.core.color

import kotlin.test.Test
import kotlin.test.assertEquals

class LinearTest {
    @Test
    fun endsAndMidGrayMatchTheStandard() {
        assertEquals(0.0, srgbToLinear(0), 1e-12)
        assertEquals(1.0, srgbToLinear(255), 1e-12)
        // sRGB 128 is about 21.6% linear light, not 50%.
        assertEquals(0.2158605, srgbToLinear(128), 1e-6)
    }

    @Test
    fun everyChannelRoundTrips() {
        for (c in 0..255) assertEquals(c, linearToSrgb(srgbToLinear(c)), "channel $c")
    }

    @Test
    fun outOfRangeLightIsClamped() {
        assertEquals(255, linearToSrgb(1.7))
        assertEquals(0, linearToSrgb(-0.2))
    }
}
