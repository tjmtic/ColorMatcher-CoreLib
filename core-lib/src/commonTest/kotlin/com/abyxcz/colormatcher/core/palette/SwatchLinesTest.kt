package com.abyxcz.colormatcher.core.palette

import com.abyxcz.colormatcher.core.color.Srgb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SwatchLinesTest {
    private val swatches =
        listOf(
            Swatch("Cadmium Red", Srgb(227, 0, 34), "Premo 5382"),
            Swatch("White", Srgb(255, 255, 255)),
        )

    @Test
    fun writesOneLinePerSwatchWithTheCodeOnlyWhenThereIsOne() {
        assertEquals("Cadmium Red,#E30022,Premo 5382\nWhite,#FFFFFF", formatSwatchLines(swatches))
    }

    @Test
    fun parsingTheOutputGivesTheSwatchesBack() {
        val parsed = parseSwatchLines(formatSwatchLines(swatches))
        assertEquals(swatches, parsed.swatches)
        assertEquals(emptyList(), parsed.rejectedLines)
    }

    @Test
    fun noSwatchesIsAnEmptyString() {
        assertEquals("", formatSwatchLines(emptyList()))
    }

    @Test
    fun refusesANameItCouldNotReadBack() {
        val black = Srgb(0, 0, 0)
        assertFailsWith<IllegalArgumentException> {
            formatSwatchLines(listOf(Swatch("Red, deep", black)))
        }
        assertFailsWith<IllegalArgumentException> {
            formatSwatchLines(listOf(Swatch("Two\nlines", black)))
        }
        assertFailsWith<IllegalArgumentException> {
            formatSwatchLines(listOf(Swatch(" Padded", black)))
        }
        assertFailsWith<IllegalArgumentException> {
            formatSwatchLines(listOf(Swatch("# Note", black)))
        }
    }

    @Test
    fun refusesACodeItCouldNotReadBack() {
        val black = Srgb(0, 0, 0)
        assertFailsWith<IllegalArgumentException> {
            formatSwatchLines(listOf(Swatch("Ink", black, "a,b")))
        }
        assertFailsWith<IllegalArgumentException> {
            formatSwatchLines(listOf(Swatch("Ink", black, "")))
        }
    }
}
