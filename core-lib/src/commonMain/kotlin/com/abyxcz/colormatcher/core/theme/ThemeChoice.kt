package com.abyxcz.colormatcher.core.theme

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.color.deltaE2000
import com.abyxcz.colormatcher.core.color.toLab
import com.abyxcz.colormatcher.core.palette.Swatch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * What the on-device model is asked: which of the [shortlist] themes fits the measured [colors],
 * and which of the [candidates] (swatches from the user's palette, numbered from 0) complete it.
 * Everything here is computed; the model only chooses among it and writes one line.
 */
class ThemeRequest(
    val colors: List<Srgb>,
    val shortlist: List<Theme>,
    val candidates: List<Swatch>,
) {
    init {
        require(colors.isNotEmpty() && shortlist.isNotEmpty()) {
            "a request needs colors and a shortlist"
        }
        require(candidates.size <= MAX_CANDIDATES) { "at most $MAX_CANDIDATES candidates" }
    }
}

/** The answer: a theme from the shortlist, swatches from the candidates, and a short line. */
data class ThemeChoice(val theme: Theme, val swatches: List<Swatch>, val line: String)

/**
 * The request for [colors] against the swatches of the user's palette: the computed shortlist, and
 * as candidates each shortlisted theme's anchors' nearest swatches, at most [MAX_CANDIDATES],
 * nearest first, no swatch twice.
 */
fun themeRequest(
    colors: List<Srgb>,
    palette: List<Swatch>,
    themes: List<Theme> = THEMES,
): ThemeRequest {
    val shortlist = shortlistThemes(colors, themes).map { it.theme }
    val candidates =
        shortlist
            .flatMap { theme -> theme.anchors.map { anchor -> nearestSwatch(anchor, palette) } }
            .filterNotNull()
            .sortedBy { it.second }
            .map { it.first }
            .distinct()
            .take(MAX_CANDIDATES)
    return ThemeRequest(colors, shortlist, candidates)
}

/** The user message for the model: the computed facts and the choices, nothing to invent. */
fun themePrompt(request: ThemeRequest): String = buildString {
    appendLine("A maker measured these clay colors: ${request.colors.joinToString { it.toHex() }}.")
    appendLine("They read as ${describeColors(request.colors)}.")
    appendLine("Pick the theme they fit best, from these only:")
    request.shortlist.forEach { appendLine("- ${it.id}: ${it.name}") }
    appendLine("Then pick up to $MAX_PICKS swatches that complete the theme, by number:")
    request.candidates.forEachIndexed { i, s -> appendLine("$i: ${s.name} ${s.color.toHex()}") }
    append("Answer in JSON with theme, swatches and one short friendly line about the set.")
}

/**
 * A GBNF grammar for the answer, so it always parses: `theme` can only be a shortlisted id,
 * `swatches` only candidate numbers, `line` only plain characters, 1 to [MAX_LINE] of them.
 */
fun themeGrammar(request: ThemeRequest): String {
    val ids = request.shortlist.joinToString(" | ") { "\"${it.id}\"" }
    val numbers = request.candidates.indices.joinToString(" | ") { "\"$it\"" }
    val swatches =
        if (request.candidates.isEmpty()) "\"\"" else "idx (\",\" idx){0,${MAX_PICKS - 1}}"
    return buildString {
        appendLine(
            "root ::= \"{\\\"theme\\\":\\\"\" theme \"\\\",\\\"swatches\\\":[\" swatches " +
                "\"],\\\"line\\\":\\\"\" line \"\\\"}\""
        )
        appendLine("theme ::= $ids")
        appendLine("swatches ::= $swatches")
        if (request.candidates.isNotEmpty()) appendLine("idx ::= $numbers")
        append("line ::= [a-zA-Z0-9 ,.'!-]{1,$MAX_LINE}")
    }
}

/**
 * The model's [answer] for [request], or null when it is not the grammar's shape or names a theme
 * or swatch the request did not offer — then the app shows [fallbackChoice] instead.
 */
fun parseThemeChoice(answer: String, request: ThemeRequest): ThemeChoice? {
    val match = ANSWER.matchEntire(answer.trim()) ?: return null
    val (id, numbers, line) = match.destructured
    val picks = numbers.split(',').filter { it.isNotEmpty() }.map { it.toIntOrNull() }
    val offered = picks.all { it != null && it in request.candidates.indices }
    return request.shortlist
        .firstOrNull { it.id == id }
        ?.takeIf { offered }
        ?.let { theme ->
            val swatches = picks.filterNotNull().distinct().map { request.candidates[it] }
            ThemeChoice(theme, swatches, line)
        }
}

/** With no model (its pack not installed yet) or no usable answer: the best fit, no line. */
fun fallbackChoice(request: ThemeRequest): ThemeChoice =
    ThemeChoice(request.shortlist.first(), request.candidates.take(MAX_PICKS), "")

/** "warm, mid-tone and vivid": temperature, lightness and colorfulness, from CIELAB (LCh). */
fun describeColors(colors: List<Srgb>): String {
    val labs = colors.map { it.toLab() }
    val chroma = labs.map { hypot(it.a, it.b) }
    val warm = labs.count { hypot(it.a, it.b) >= NEUTRAL_CHROMA && isWarm(atan2(it.b, it.a)) }
    val cool = labs.count { hypot(it.a, it.b) >= NEUTRAL_CHROMA && !isWarm(atan2(it.b, it.a)) }
    val temperature =
        when {
            warm == 0 && cool == 0 -> "neutral"
            warm > cool -> "warm"
            cool > warm -> "cool"
            else -> "mixed warm and cool"
        }
    val lightness = labs.sumOf { it.l } / labs.size
    val tone =
        when {
            lightness >= LIGHT -> "light"
            lightness <= DARK -> "dark"
            else -> "mid-tone"
        }
    val vividness = if (chroma.average() >= VIVID_CHROMA) "vivid" else "muted"
    return "$temperature, $tone and $vividness"
}

/** Reds through yellows (hue -60° to 100°) read warm; greens through purples, cool. */
private fun isWarm(hueRadians: Double): Boolean = hueRadians * HALF_TURN / PI in WARM_FROM..WARM_TO

private fun nearestSwatch(color: Srgb, palette: List<Swatch>): Pair<Swatch, Double>? {
    val lab = color.toLab()
    return palette.map { it to deltaE2000(lab, it.lab) }.minByOrNull { it.second }
}

private val ANSWER =
    Regex("""\{"theme":"([a-z0-9-]+)","swatches":\[([0-9,]*)\],"line":"([a-zA-Z0-9 ,.'!-]*)"}""")

/** Candidates offered to the model: few enough to number in a short prompt. */
const val MAX_CANDIDATES = 8
private const val MAX_PICKS = 4
private const val MAX_LINE = 80
private const val NEUTRAL_CHROMA = 10.0
private const val VIVID_CHROMA = 40.0
private const val LIGHT = 70.0
private const val DARK = 35.0
private const val WARM_FROM = -60.0
private const val WARM_TO = 100.0
private const val HALF_TURN = 180.0
