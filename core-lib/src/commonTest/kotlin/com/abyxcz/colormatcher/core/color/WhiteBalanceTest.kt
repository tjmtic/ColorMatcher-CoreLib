package com.abyxcz.colormatcher.core.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WhiteBalanceTest {
    private val grayCard = Srgb(119, 119, 119)

    @Test
    fun theReferenceIsCorrectedToItsKnownColor() {
        val warmCard = Srgb(140, 118, 92) // a gray card under warm light
        val balance = assertNotNull(WhiteBalance.fromReference(warmCard, grayCard))
        assertEquals(grayCard, balance.correct(warmCard))
    }

    @Test
    fun theSameGainsMoveOtherColorsTheSameWay() {
        val warmCard = Srgb(140, 118, 92)
        val balance = assertNotNull(WhiteBalance.fromReference(warmCard, grayCard))
        val clay = balance.correct(Srgb(200, 120, 60))
        // Warm light pushed red up and blue down; the correction pulls them back.
        assert(clay.r < 200) { "red should drop: $clay" }
        assert(clay.b > 60) { "blue should rise: $clay" }
    }

    @Test
    fun aNeutralObservationChangesNothing() {
        val balance = assertNotNull(WhiteBalance.fromReference(grayCard, grayCard))
        assertEquals(Srgb(200, 120, 60), balance.correct(Srgb(200, 120, 60)))
    }

    @Test
    fun refusesADarkOrClippedCard() {
        assertNull(WhiteBalance.fromReference(Srgb(10, 60, 60), grayCard))
        assertNull(WhiteBalance.fromReference(Srgb(255, 240, 230), grayCard))
    }

    @Test
    fun noneChangesNothing() {
        assertEquals(Srgb(12, 34, 56), WhiteBalance.NONE.correct(Srgb(12, 34, 56)))
    }
}
