package com.abyxcz.colormatcher.core.cluster

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.sample.PixelBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ColorPartsTest {
    private val red = Srgb(190, 60, 50)
    private val teal = Srgb(30, 130, 140)
    private val cream = Srgb(240, 225, 200)

    private fun argb(c: Srgb): Int = (0xFF shl 24) or (c.r shl 16) or (c.g shl 8) or c.b

    private fun photo(width: Int, height: Int, color: (Int, Int) -> Srgb) =
        PixelBuffer(width, height, IntArray(width * height) { argb(color(it % width, it / width)) })

    /** Left third red, middle third teal, right third cream. */
    private val stripes = photo(30, 12) { x, _ -> if (x < 10) red else if (x < 20) teal else cream }

    @Test
    fun findsEachDistinctColorWithItsShare() {
        val parts = assertNotNull(findColorParts(stripes, options = PartOptions(step = 1)))
        assertEquals(setOf(red, teal, cream), parts.parts.map { it.color }.toSet())
        parts.parts.forEach { assertEquals(1.0 / 3, it.share, 1e-9) }
    }

    @Test
    fun aTapSelectsThePartUnderIt() {
        val parts = assertNotNull(findColorParts(stripes, options = PartOptions(step = 1)))
        assertEquals(teal, parts.parts[parts.partOf(Srgb(35, 128, 138))].color)
        assertEquals(red, parts.parts[parts.partOf(red)].color)
    }

    @Test
    fun shadesCloserThanTheMergeLimitAreOnePart() {
        val nearlyRed = Srgb(191, 61, 51)
        val parts =
            assertNotNull(findColorParts(photo(20, 10) { x, _ -> if (x < 10) red else nearlyRed }))
        assertEquals(1, parts.parts.size)
        assertEquals(1.0, parts.parts[0].share, 1e-9)
    }

    @Test
    fun biggestPartComesFirst() {
        val mostlyCream = photo(20, 10) { x, _ -> if (x < 5) red else cream }
        val parts = assertNotNull(findColorParts(mostlyCream, options = PartOptions(step = 1)))
        assertEquals(listOf(cream, red), parts.parts.map { it.color })
        assertEquals(0.75, parts.parts[0].share, 1e-9)
    }

    @Test
    fun aMaskKeepsOnlyTheForeground() {
        val mask = BooleanArray(30 * 12) { it % 30 >= 20 } // only the cream third
        val parts = assertNotNull(findColorParts(stripes, mask, PartOptions(step = 1)))
        assertEquals(listOf(cream), parts.parts.map { it.color })
    }

    @Test
    fun nothingToSampleIsNull() {
        assertNull(findColorParts(stripes, BooleanArray(30 * 12)))
    }

    @Test
    fun theSamePhotoAlwaysGivesTheSameParts() {
        val noisy =
            photo(40, 20) { x, y -> if ((x + y) % 7 == 0) teal else if (x < 20) red else cream }
        assertEquals(
            findColorParts(noisy)?.parts,
            findColorParts(noisy)?.parts,
        )
    }

    @Test
    fun refusesAMaskOfTheWrongSizeAndBadOptions() {
        assertFailsWith<IllegalArgumentException> { findColorParts(stripes, BooleanArray(3)) }
        assertFailsWith<IllegalArgumentException> { PartOptions(maxParts = 0) }
    }
}
