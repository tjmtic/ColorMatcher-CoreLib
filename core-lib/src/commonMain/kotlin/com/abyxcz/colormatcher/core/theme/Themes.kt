package com.abyxcz.colormatcher.core.theme

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.color.deltaE2000
import com.abyxcz.colormatcher.core.color.toLab

/**
 * A color theme: a name a maker would search for, and the anchor colors that define it. The catalog
 * is our own picks, not any brand's or Pantone's; [id] is what the on-device model answers with, so
 * it is a plain slug.
 */
class Theme(val id: String, val name: String, anchorHexes: List<String>) {
    val anchors: List<Srgb> = anchorHexes.map {
        requireNotNull(Srgb.fromHex(it)) { "not a hex: $it" }
    }

    override fun toString(): String = "Theme($id)"
}

/** The themes the app knows. Order is display order when two fit equally well. */
val THEMES: List<Theme> =
    listOf(
        Theme(
            "autumn-harvest",
            "Autumn harvest",
            listOf("#B5651D", "#C1440E", "#E3A857", "#6B4226", "#8A9A5B"),
        ),
        Theme("ocean", "Ocean", listOf("#1F4E79", "#2E86AB", "#A3D5E0", "#F2F4F3", "#0B3C49")),
        Theme(
            "pastel-kawaii",
            "Pastel kawaii",
            listOf("#F7C6D9", "#C9E4F5", "#FFF1B5", "#D7C4F0", "#C8EBD3"),
        ),
        Theme(
            "earthy-boho",
            "Earthy boho",
            listOf("#C67B5C", "#D9B48F", "#8C6A4F", "#A3A380", "#EFE6DD"),
        ),
        Theme(
            "christmas",
            "Christmas",
            listOf("#B3001B", "#0B6623", "#F5F5F5", "#D4AF37", "#5A0B0B"),
        ),
        Theme(
            "halloween",
            "Halloween",
            listOf("#FF7518", "#1A1A1A", "#6A0DAD", "#F2E8CF", "#3B7A57"),
        ),
        Theme(
            "spring-garden",
            "Spring garden",
            listOf("#F4A7B9", "#FFD166", "#8BC34A", "#FFFFFF", "#B39DDB"),
        ),
        Theme("desert", "Desert", listOf("#E2C290", "#C2703D", "#8B5A2B", "#EED9C4", "#6E8B74")),
        Theme("berry", "Berry", listOf("#8E1B4A", "#C2185B", "#5D1049", "#F8BBD0", "#3E2723")),
        Theme(
            "mid-century",
            "Mid-century",
            listOf("#E07A5F", "#3D405B", "#81B29A", "#F2CC8F", "#F4F1DE"),
        ),
        Theme(
            "neutrals",
            "Neutrals",
            listOf("#FFFFFF", "#D9D9D9", "#8C8C8C", "#3A3A3A", "#000000"),
        ),
    )

/**
 * How well [theme] fits a set of colors: the mean, over the colors, of ΔE00 to its nearest anchor.
 */
data class ThemeFit(val theme: Theme, val distance: Double)

/**
 * The [size] themes of [themes] that fit [colors] best, best first: computed, so it works with no
 * model at all, and it is the only list the model may choose from. Empty when [colors] is.
 */
fun shortlistThemes(
    colors: List<Srgb>,
    themes: List<Theme> = THEMES,
    size: Int = SHORTLIST,
): List<ThemeFit> {
    require(size > 0) { "size must be positive: $size" }
    if (colors.isEmpty()) return emptyList()
    val labs = colors.map { it.toLab() }
    return themes
        .map { theme ->
            val anchors = theme.anchors.map { it.toLab() }
            ThemeFit(theme, labs.sumOf { lab -> anchors.minOf { deltaE2000(lab, it) } } / labs.size)
        }
        .sortedBy { it.distance }
        .take(size)
}

private const val SHORTLIST = 3
