package com.abyxcz.colormatcher.core.sample

import com.abyxcz.colormatcher.core.color.Srgb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClayMeasurementTest {
    private val grayCard = Srgb(119, 119, 119)
    private val warmCard = Srgb(140, 118, 92) // the gray card under warm light
    private val warmClay = Srgb(200, 120, 60)

    private fun argb(c: Srgb): Int = (0xFF shl 24) or (c.r shl 16) or (c.g shl 8) or c.b

    /** Left half the card, right half the clay, both under the same warm light. */
    private val photo =
        PixelBuffer(20, 10, IntArray(200) { i -> argb(if (i % 20 < 10) warmCard else warmClay) })

    private val clay = Patch(15, 5, 3)
    private val card = Patch(4, 5, 3)

    @Test
    fun aUsableCardBalancesTheClay() {
        val m = assertNotNull(measureClay(photo, clay, card, grayCard))
        assertEquals(BalanceStatus.BALANCED, m.balance)
        assertTrue(
            m.color.r < warmClay.r && m.color.b > warmClay.b,
            "warm cast removed: ${m.color}",
        )
        assertFalse(m.uneven)
    }

    @Test
    fun noCardLeavesTheClayAsSampled() {
        val m = assertNotNull(measureClay(photo, clay, null, grayCard))
        assertEquals(BalanceStatus.NO_CARD, m.balance)
        assertEquals(warmClay, m.color)
    }

    @Test
    fun aCardOffTheImageIsUnusable() {
        val m = assertNotNull(measureClay(photo, clay, Patch(100, 100, 2), grayCard))
        assertEquals(BalanceStatus.CARD_UNUSABLE, m.balance)
        assertEquals(warmClay, m.color)
    }

    @Test
    fun aPatchAcrossTwoColorsIsUneven() {
        val m = assertNotNull(measureClay(photo, Patch(10, 5, 4), null, grayCard))
        assertTrue(m.uneven, "spread ${m.spread}")
    }

    @Test
    fun noClayPixelsIsNull() {
        assertNull(measureClay(photo, Patch(-50, -50, 2), card, grayCard))
    }
}
