package com.abyxcz.colormatcher.core.palette

/**
 * Writes [swatches] as `name,#RRGGBB` or `name,#RRGGBB,code` lines, one per swatch, joined by `\n`
 * — the inverse of [parseSwatchLines], so `parseSwatchLines(formatSwatchLines(s)).swatches == s`.
 * Used to save palettes (on device, and across a configuration change) and to share them.
 *
 * Throws [IllegalArgumentException] for a name or code [parseSwatchLines] could not read back: one
 * that contains a comma or a line break, is blank (a name) or empty (a code), or is a name starting
 * with `# ` (read back as a comment).
 */
fun formatSwatchLines(swatches: List<Swatch>): String =
    swatches.joinToString("\n") { swatch ->
        require(isWritable(swatch.name) && !swatch.name.startsWith("# ")) {
            "swatch name cannot be written as a line: \"${swatch.name}\""
        }
        val code = swatch.code
        require(code == null || isWritable(code)) { "swatch code cannot be written: \"$code\"" }
        listOfNotNull(swatch.name, swatch.color.toHex(), code).joinToString(",")
    }

/** Survives the trim and the comma split of [parseSwatchLines] unchanged. */
private fun isWritable(field: String): Boolean =
    field.isNotBlank() &&
        field == field.trim() &&
        field.none { it == ',' || it == '\n' || it == '\r' }
