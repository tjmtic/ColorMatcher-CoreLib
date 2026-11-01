package com.abyxcz.colormatcher.core.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SrgbTest {
    @Test
    fun parsesLongShortAndBareHex() {
        assertEquals(Srgb(255, 136, 0), Srgb.fromHex("#FF8800"))
        assertEquals(Srgb(255, 136, 0), Srgb.fromHex("ff8800"))
        assertEquals(Srgb(255, 136, 0), Srgb.fromHex(" #f80 "))
    }

    @Test
    fun rejectsMalformedHex() {
        assertNull(Srgb.fromHex("#FF88"))
        assertNull(Srgb.fromHex("#GG8800"))
        assertNull(Srgb.fromHex(""))
    }

    @Test
    fun formatsAsUpperCaseHex() {
        assertEquals("#0A0B0C", Srgb(10, 11, 12).toHex())
    }

    @Test
    fun rejectsOutOfRangeChannels() {
        assertFailsWith<IllegalArgumentException> { Srgb(256, 0, 0) }
    }
}

class LabTest {
    private fun assertLab(expected: Lab, actual: Lab) {
        assertEquals(expected.l, actual.l, 0.01, "L of $actual")
        assertEquals(expected.a, actual.a, 0.01, "a of $actual")
        assertEquals(expected.b, actual.b, 0.01, "b of $actual")
    }

    @Test
    fun whiteAndBlackAreTheEndsOfLightness() {
        assertLab(Lab(100.0, 0.0, 0.0), Srgb(255, 255, 255).toLab())
        assertLab(Lab(0.0, 0.0, 0.0), Srgb(0, 0, 0).toLab())
    }

    @Test
    fun primariesMatchReferenceValues() {
        assertLab(Lab(53.24, 80.09, 67.20), Srgb(255, 0, 0).toLab())
        assertLab(Lab(87.73, -86.18, 83.18), Srgb(0, 255, 0).toLab())
        assertLab(Lab(32.30, 79.19, -107.86), Srgb(0, 0, 255).toLab())
    }
}

/** Pairs from Sharma, Wu & Dalal (2005), Table 1, with their published ΔE00. */
class DeltaE2000Test {
    private fun check(x: Lab, y: Lab, expected: Double) {
        assertEquals(expected, deltaE2000(x, y), 1e-4, "ΔE00 of $x and $y")
        assertEquals(expected, deltaE2000(y, x), 1e-4, "ΔE00 is symmetric")
    }

    @Test fun pair1() = check(Lab(50.0, 2.6772, -79.7751), Lab(50.0, 0.0, -82.7485), 2.0425)

    @Test fun pair7() = check(Lab(50.0, 0.0, 0.0), Lab(50.0, -1.0, 2.0), 2.3669)

    @Test fun pair13() = check(Lab(50.0, 2.49, -0.001), Lab(50.0, -2.49, 0.0009), 7.1792)

    @Test fun pair17() = check(Lab(50.0, 2.5, 0.0), Lab(73.0, 25.0, -18.0), 27.1492)

    @Test
    fun pair25() = check(Lab(60.2574, -34.0099, 36.2677), Lab(60.4626, -34.1751, 39.4387), 1.2644)

    @Test fun pair34() = check(Lab(2.0776, 0.0795, -1.1350), Lab(0.9033, -0.0636, -0.5514), 0.9082)

    @Test
    fun aColorIsZeroFromItself() {
        val c = Srgb(120, 80, 200).toLab()
        assertEquals(0.0, deltaE2000(c, c), 1e-12)
    }
}
