package com.abyxcz.colormatcher.core.match

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.palette.Palette
import com.abyxcz.colormatcher.core.palette.PaletteKind
import com.abyxcz.colormatcher.core.palette.Swatch
import com.abyxcz.colormatcher.core.palette.parseSwatchLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MatcherTest {
    private val scheme =
        Palette(
            id = "test",
            name = "Test scheme",
            kind = PaletteKind.SCHEME,
            swatches =
                listOf(
                    Swatch("Brick", Srgb(178, 34, 34)),
                    Swatch("Tomato", Srgb(255, 99, 71)),
                    Swatch("Navy", Srgb(0, 0, 128)),
                    Swatch("Sky", Srgb(135, 206, 235)),
                ),
        )

    @Test
    fun nearestComesFirstAndIsExactForAPaletteColor() {
        val result = nearest(Srgb(255, 99, 71), scheme)
        assertEquals("Tomato", result.first().swatch.name)
        assertEquals(0.0, result.first().deltaE, 1e-9)
        assertEquals(Closeness.IDENTICAL, result.first().closeness)
    }

    @Test
    fun aReddishClayLandsOnTheRedsBeforeTheBlues() {
        val names = nearest(Srgb(190, 45, 40), scheme).map { it.swatch.name }
        assertEquals(listOf("Brick", "Tomato"), names.take(2))
    }

    @Test
    fun resultsAreSortedAndLimited() {
        val result = nearest(Srgb(100, 100, 100), scheme, limit = 3)
        assertEquals(3, result.size)
        assertEquals(result.sortedBy { it.deltaE }, result)
    }

    @Test
    fun limitMustBePositive() {
        assertFailsWith<IllegalArgumentException> { nearest(Srgb(0, 0, 0), scheme, limit = 0) }
    }

    @Test
    fun closenessBands() {
        assertEquals(Closeness.IDENTICAL, Closeness.of(0.5))
        assertEquals(Closeness.CLOSE, Closeness.of(1.5))
        assertEquals(Closeness.NOTICEABLE, Closeness.of(3.0))
        assertEquals(Closeness.DISTINCT, Closeness.of(7.0))
        assertEquals(Closeness.DIFFERENT, Closeness.of(40.0))
    }
}

class ParseSwatchLinesTest {
    @Test
    fun readsNamesHexAndOptionalCodes() {
        val parsed =
            parseSwatchLines("# a comment\n\nCadmium Red,#E30022,Premo 5382\nWhite, #fff \n")
        assertEquals(
            listOf(
                Swatch("Cadmium Red", Srgb(227, 0, 34), "Premo 5382"),
                Swatch("White", Srgb(255, 255, 255)),
            ),
            parsed.swatches,
        )
        assertEquals(emptyList(), parsed.rejectedLines)
        assertNull(parsed.swatches[1].code)
    }

    @Test
    fun reportsLinesItCannotRead() {
        val parsed =
            parseSwatchLines(
                "Good,#000000\nno hex here\n,#123456\nBad,#12\nToo,#000000,many,fields"
            )
        assertEquals(1, parsed.swatches.size)
        assertEquals(listOf(2, 3, 4, 5), parsed.rejectedLines)
    }
}
