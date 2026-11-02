package com.abyxcz.colormatcher.core.theme

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.palette.Swatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThemeTest {
    private fun hex(h: String) = assertNotNull(Srgb.fromHex(h))

    private val autumn = listOf(hex("#B8661F"), hex("#C2460F"), hex("#6A4327"))
    private val palette =
        listOf(
            Swatch("Ochre", hex("#CC7722")),
            Swatch("Rust", hex("#B7410E")),
            Swatch("Umber", hex("#635147")),
            Swatch("Sky", hex("#87CEEB")),
            Swatch("Navy", hex("#1F3A5F")),
        )

    @Test
    fun autumnColorsShortlistAutumnFirst() {
        val fits = shortlistThemes(autumn)
        assertEquals(3, fits.size)
        assertEquals("autumn-harvest", fits.first().theme.id)
        assertEquals(fits.sortedBy { it.distance }, fits)
    }

    @Test
    fun oceanColorsShortlistOceanFirst() {
        assertEquals(
            "ocean",
            shortlistThemes(listOf(hex("#2E86AB"), hex("#1F4E79"))).first().theme.id,
        )
    }

    @Test
    fun noColorsNoShortlist() {
        assertTrue(shortlistThemes(emptyList()).isEmpty())
    }

    @Test
    fun theRequestOffersTheShortlistAndNearbySwatches() {
        val request = themeRequest(autumn, palette)
        assertEquals("autumn-harvest", request.shortlist.first().id)
        assertTrue(request.candidates.size in 1..MAX_CANDIDATES)
        assertEquals(request.candidates.distinct(), request.candidates)
        assertTrue(request.candidates.any { it.name == "Rust" })
    }

    @Test
    fun thePromptCarriesTheFactsAndTheChoices() {
        val request = themeRequest(autumn, palette)
        val prompt = themePrompt(request)
        assertTrue("#B8661F" in prompt)
        assertTrue("warm" in prompt)
        request.shortlist.forEach { assertTrue("- ${it.id}: ${it.name}" in prompt) }
        request.candidates.forEachIndexed { i, s -> assertTrue("$i: ${s.name}" in prompt) }
    }

    @Test
    fun theGrammarAllowsOnlyWhatWasOffered() {
        val request = ThemeRequest(autumn, THEMES.take(2), palette.take(3))
        assertEquals(
            """
            root ::= "{\"theme\":\"" theme "\",\"swatches\":[" swatches "],\"line\":\"" line "\"}"
            theme ::= "autumn-harvest" | "ocean"
            swatches ::= idx ("," idx){0,3}
            idx ::= "0" | "1" | "2"
            line ::= [a-zA-Z0-9 ,.'!-]{1,80}
            """
                .trimIndent(),
            themeGrammar(request),
        )
    }

    @Test
    fun aGrammarShapedAnswerParses() {
        val request = ThemeRequest(autumn, THEMES.take(2), palette.take(3))
        val choice =
            assertNotNull(
                parseThemeChoice(
                    """{"theme":"autumn-harvest","swatches":[1,0,1],"line":"Cozy fall tones!"}""",
                    request,
                )
            )
        assertEquals("autumn-harvest", choice.theme.id)
        assertEquals(listOf("Rust", "Ochre"), choice.swatches.map { it.name })
        assertEquals("Cozy fall tones!", choice.line)
    }

    @Test
    fun anAnswerOutsideTheOfferIsRefused() {
        val request = ThemeRequest(autumn, THEMES.take(2), palette.take(3))
        assertNull(parseThemeChoice("""{"theme":"christmas","swatches":[0],"line":"x"}""", request))
        assertNull(parseThemeChoice("""{"theme":"ocean","swatches":[7],"line":"x"}""", request))
        assertNull(parseThemeChoice("not json at all", request))
    }

    @Test
    fun theFallbackIsTheBestFitWithNoLine() {
        val request = themeRequest(autumn, palette)
        val choice = fallbackChoice(request)
        assertEquals(request.shortlist.first(), choice.theme)
        assertEquals("", choice.line)
        assertTrue(choice.swatches.size <= 4)
    }

    @Test
    fun colorsAreDescribedFromLab() {
        assertEquals(
            "warm, mid-tone and vivid",
            describeColors(listOf(hex("#C1440E"), hex("#E3A857"))),
        )
        assertEquals("cool, dark and muted", describeColors(listOf(hex("#1F3A5F"), hex("#2B3A42"))))
        assertEquals(
            "neutral, light and muted",
            describeColors(listOf(hex("#F0F0F0"), hex("#D9D9D9"))),
        )
    }
}
