package com.abyxcz.colormatcher.core.sample

import com.abyxcz.colormatcher.core.color.Srgb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PatchSamplerTest {
    private fun argb(c: Srgb, alpha: Int = 0xFF): Int =
        (alpha shl 24) or (c.r shl 16) or (c.g shl 8) or c.b

    private fun filled(width: Int, height: Int, color: (Int, Int) -> Srgb) =
        PixelBuffer(width, height, IntArray(width * height) { argb(color(it % width, it / width)) })

    @Test
    fun aFlatPatchIsItsColorWithNoSpread() {
        val clay = Srgb(181, 101, 29)
        val sample = assertNotNull(samplePatch(filled(9, 9) { _, _ -> clay }, Patch(4, 4, 2)))
        assertEquals(clay, sample.color)
        assertEquals(13, sample.pixelCount) // a radius-2 circle
        assertEquals(0.0, sample.spread, 1e-9)
    }

    @Test
    fun blackAndWhiteAverageInLinearLight() {
        // Five black and five white pixels: the linear mean is 50% light, sRGB 188, not 128.
        val buffer = filled(10, 1) { x, _ -> if (x < 5) Srgb(0, 0, 0) else Srgb(255, 255, 255) }
        val sample = assertNotNull(samplePatch(buffer, Patch(5, 0, 5)))
        assertEquals(10, sample.pixelCount)
        assertEquals(Srgb(188, 188, 188), sample.color)
        assertTrue(sample.spread > 30.0, "black and white is not one color: ${sample.spread}")
    }

    @Test
    fun pixelsOutsideTheBufferAndNotOpaqueAreSkipped() {
        val clay = Srgb(50, 100, 150)
        val pixels = IntArray(4) { argb(clay) }
        pixels[3] = argb(Srgb(255, 0, 0), alpha = 0x80)
        val sample = assertNotNull(samplePatch(PixelBuffer(2, 2, pixels), Patch(0, 0, 5)))
        assertEquals(clay, sample.color)
        assertEquals(3, sample.pixelCount)
    }

    @Test
    fun nothingToSampleIsNull() {
        assertNull(samplePatch(filled(4, 4) { _, _ -> Srgb(0, 0, 0) }, Patch(40, 40, 3)))
    }

    @Test
    fun aBufferMustMatchItsSize() {
        assertFailsWith<IllegalArgumentException> { PixelBuffer(2, 2, IntArray(3)) }
        assertFailsWith<IllegalArgumentException> { Patch(0, 0, -1) }
    }
}
