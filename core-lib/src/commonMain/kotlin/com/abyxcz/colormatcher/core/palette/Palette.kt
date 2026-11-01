package com.abyxcz.colormatcher.core.palette

import com.abyxcz.colormatcher.core.color.Lab
import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.color.toLab

/**
 * One named color in a palette: a clay (brand + color name), a Pantone code, or a hex scheme entry.
 * [code] is the identifier its owner uses ("PANTONE 186 C", "Premo 5026"); null when it has none.
 */
data class Swatch(val name: String, val color: Srgb, val code: String? = null) {
    val lab: Lab by lazy { color.toLab() }
}

/**
 * What a palette is, which decides how the app may treat it.
 * - [CLAY]: a clay line (a brand's colors, or the user's own mixes).
 * - [SCHEME]: a hex color scheme — a brand guide, a design palette.
 * - [LICENSED]: a palette the user imported under their own license (Pantone and the like). Its
 *   data never ships in this library or the app; it lives only on the user's device.
 */
enum class PaletteKind {
    CLAY,
    SCHEME,
    LICENSED,
}

data class Palette(
    val id: String,
    val name: String,
    val kind: PaletteKind,
    val swatches: List<Swatch>,
)

/**
 * Reads a palette from plain lines of `name,#hex` or `name,#hex,code` — the shape a user can export
 * from a spreadsheet or type by hand. Blank lines and lines starting with `#` followed by a space
 * (comments) are skipped. Returns the swatches and the 1-based numbers of the lines it could not
 * read, so the app can show them instead of silently dropping colors.
 */
fun parseSwatchLines(text: String): ParsedSwatches {
    val swatches = mutableListOf<Swatch>()
    val rejected = mutableListOf<Int>()
    text.lineSequence().forEachIndexed { index, raw ->
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("# ")) return@forEachIndexed
        val parts = line.split(',').map { it.trim() }
        val color = parts.getOrNull(1)?.let(Srgb::fromHex)
        if (parts.size !in FIELDS_MIN..FIELDS_MAX || parts[0].isEmpty() || color == null) {
            rejected += index + 1
        } else {
            swatches +=
                Swatch(name = parts[0], color = color, code = parts.getOrNull(2)?.ifEmpty { null })
        }
    }
    return ParsedSwatches(swatches, rejected)
}

private const val FIELDS_MIN = 2 // name, hex
private const val FIELDS_MAX = 3 // name, hex, code

data class ParsedSwatches(val swatches: List<Swatch>, val rejectedLines: List<Int>)
