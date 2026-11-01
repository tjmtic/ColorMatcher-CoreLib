package com.abyxcz.colormatcher.core.match

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.color.deltaE2000
import com.abyxcz.colormatcher.core.color.toLab
import com.abyxcz.colormatcher.core.palette.Palette
import com.abyxcz.colormatcher.core.palette.Swatch

/**
 * How close a match reads to the eye, from its ΔE2000. The bands are the usual rule of thumb, not a
 * standard: a ΔE under 1 is not perceptible, 1–2 only side by side, 2–5 at a glance, and past 10 it
 * is a different color.
 */
enum class Closeness(val maxDeltaE: Double) {
    IDENTICAL(1.0),
    CLOSE(2.0),
    NOTICEABLE(5.0),
    DISTINCT(10.0),
    DIFFERENT(Double.POSITIVE_INFINITY);

    companion object {
        fun of(deltaE: Double): Closeness = entries.first { deltaE < it.maxDeltaE }
    }
}

data class Match(val swatch: Swatch, val deltaE: Double) {
    val closeness: Closeness
        get() = Closeness.of(deltaE)
}

/**
 * The [limit] swatches in [palette] nearest to [target] by CIEDE2000, nearest first. Ties keep
 * palette order, so the result is stable for the same inputs.
 *
 * The same call serves both directions of the app: a sampled clay against a Pantone or hex palette
 * ("what color is this clay?"), and a target hex against a clay palette ("which clay is this?").
 */
fun nearest(target: Srgb, palette: Palette, limit: Int = DEFAULT_LIMIT): List<Match> {
    require(limit > 0) { "limit must be positive: $limit" }
    val lab = target.toLab()
    return palette.swatches
        .map { Match(it, deltaE2000(lab, it.lab)) }
        .sortedBy { it.deltaE }
        .take(limit)
}

private const val DEFAULT_LIMIT = 5
