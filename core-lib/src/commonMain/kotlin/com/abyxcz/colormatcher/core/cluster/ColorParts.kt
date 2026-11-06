package com.abyxcz.colormatcher.core.cluster

import com.abyxcz.colormatcher.core.color.Lab
import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.color.deltaE2000
import com.abyxcz.colormatcher.core.color.linearToSrgb
import com.abyxcz.colormatcher.core.color.srgbToLinear
import com.abyxcz.colormatcher.core.color.toLab
import com.abyxcz.colormatcher.core.sample.PixelBuffer

/**
 * One distinct color in a photo: its average (in linear light), its share of the pixels, and its
 * [spread], measured as a patch's is ([com.abyxcz.colormatcher.core.sample.PatchSample]): the ΔE00
 * from the average within which 90% of the part's samples fall. A part is what clustering grouped,
 * not one flat color: a shaded or marbled clay gives a part with a large spread.
 */
data class ColorPart(val color: Srgb, val share: Double, val spread: Double)

/**
 * The distinct colors of a photo, biggest share first — the parts a user can pick: each clay of a
 * multi-color piece, the card, the table. [partOf] says which part a pixel belongs to, so a tap
 * selects the whole part, not a patch.
 */
class ColorParts internal constructor(val parts: List<ColorPart>, private val centers: List<Lab>) {
    /** The index in [parts] of the part whose color [pixel] is nearest to (ΔE00). */
    fun partOf(pixel: Srgb): Int {
        val lab = pixel.toLab()
        return centers.indices.minBy { deltaE2000(lab, centers[it]) }
    }
}

/**
 * How [findColorParts] works: at most [maxParts] parts, every [step]th pixel across and down
 * sampled, parts closer than [mergeBelow] ΔE00 merged into one, at most [iterations] rounds.
 */
data class PartOptions(
    val maxParts: Int = 6,
    val step: Int = 4,
    val mergeBelow: Double = 3.0,
    val iterations: Int = 12,
) {
    init {
        require(maxParts > 0 && step > 0 && iterations > 0) { "options must be positive: $this" }
    }
}

/**
 * Finds the distinct colors of [buffer] by k-means in CIELAB, started deterministically (the sample
 * nearest the mean, then each next the sample farthest from those chosen), so the same photo always
 * gives the same parts. Only opaque pixels count, and only where [mask] is true when given (a
 * foreground mask from the platform's segmentation, `width * height` long). Parts closer than
 * [PartOptions.mergeBelow] are merged, so a plain photo gives few parts, not always
 * [PartOptions.maxParts]. Null when no pixel is left to sample.
 */
fun findColorParts(
    buffer: PixelBuffer,
    mask: BooleanArray? = null,
    options: PartOptions = PartOptions(),
): ColorParts? {
    require(mask == null || mask.size == buffer.width * buffer.height) {
        "mask has ${mask?.size} entries for ${buffer.width} x ${buffer.height} pixels"
    }
    val samples = sample(buffer, mask, options.step)
    if (samples.isEmpty()) return null
    val labs = samples.map { it.toLab() }
    val assignment = kMeans(labs, minOf(options.maxParts, labs.size), options.iterations)
    val groups = merge(assignment.groupBy({ it.second }, { it.first }), labs, options.mergeBelow)
    val parts =
        groups
            .map { members -> members to averageColor(members.map { samples[it] }) }
            .sortedByDescending { it.first.size }
    return ColorParts(
        parts =
            parts.map { (members, color) ->
                ColorPart(color, members.size.toDouble() / labs.size, spread(color, members, labs))
            },
        centers = parts.map { it.second.toLab() },
    )
}

private fun sample(buffer: PixelBuffer, mask: BooleanArray?, step: Int): List<Srgb> {
    val out = mutableListOf<Srgb>()
    for (y in 0 until buffer.height step step) {
        for (x in 0 until buffer.width step step) {
            val p = buffer.pixel(x, y)
            val included = mask == null || mask[y * buffer.width + x]
            if (included && p ushr ALPHA_SHIFT == OPAQUE) out += Srgb.fromArgb(p)
        }
    }
    return out
}

/** (sample index, cluster index) for every sample. */
private fun kMeans(labs: List<Lab>, k: Int, iterations: Int): List<Pair<Int, Int>> {
    var centers = initialCenters(labs, k)
    var labels = IntArray(labs.size) { nearest(labs[it], centers) }
    repeat(iterations) {
        centers =
            centers.indices.map { c ->
                meanLab(labs.filterIndexed { i, _ -> labels[i] == c }) ?: centers[c]
            }
        val next = IntArray(labs.size) { nearest(labs[it], centers) }
        if (next.contentEquals(labels)) return labels.withIndex().map { it.index to it.value }
        labels = next
    }
    return labels.withIndex().map { it.index to it.value }
}

private fun initialCenters(labs: List<Lab>, k: Int): List<Lab> {
    val mean = meanLab(labs) ?: return emptyList()
    val centers = mutableListOf(labs.minBy { distance(it, mean) })
    while (centers.size < k) {
        val farthest = labs.maxBy { lab -> centers.minOf { distance(lab, it) } }
        if (centers.minOf { distance(farthest, it) } == 0.0) break // fewer distinct colors than k
        centers += farthest
    }
    return centers
}

/** Groups of sample indices, folding each group into a bigger one closer than [below] ΔE00. */
private fun merge(groups: Map<Int, List<Int>>, labs: List<Lab>, below: Double): List<List<Int>> {
    val kept = mutableListOf<MutableList<Int>>()
    for (members in groups.values.sortedByDescending { it.size }) {
        val center = meanLab(members.map { labs[it] }) ?: continue
        val into = kept.firstOrNull { other ->
            val otherCenter = meanLab(other.map { labs[it] })
            otherCenter != null && deltaE2000(center, otherCenter) < below
        }
        if (into != null) into += members else kept += members.toMutableList()
    }
    return kept
}

/** The ΔE00 from [color] within which [SPREAD_QUANTILE] of the [members] fall. */
private fun spread(color: Srgb, members: List<Int>, labs: List<Lab>): Double {
    val center = color.toLab()
    val distances = members.map { deltaE2000(center, labs[it]) }.sorted()
    return distances[((distances.size - 1) * SPREAD_QUANTILE).toInt()]
}

private fun nearest(lab: Lab, centers: List<Lab>): Int =
    centers.indices.minBy { distance(lab, centers[it]) }

/** CIE76 ΔE: cheap, and good enough to cluster with; ΔE00 decides the merges. */
private fun distance(x: Lab, y: Lab): Double {
    val dl = x.l - y.l
    val da = x.a - y.a
    val db = x.b - y.b
    return dl * dl + da * da + db * db
}

private fun meanLab(labs: List<Lab>): Lab? =
    if (labs.isEmpty()) null
    else
        Lab(
            labs.sumOf { it.l } / labs.size,
            labs.sumOf { it.a } / labs.size,
            labs.sumOf { it.b } / labs.size,
        )

private fun averageColor(colors: List<Srgb>): Srgb =
    Srgb(
        linearToSrgb(colors.sumOf { srgbToLinear(it.r) } / colors.size),
        linearToSrgb(colors.sumOf { srgbToLinear(it.g) } / colors.size),
        linearToSrgb(colors.sumOf { srgbToLinear(it.b) } / colors.size),
    )

private const val SPREAD_QUANTILE = 0.9
private const val ALPHA_SHIFT = 24
private const val OPAQUE = 0xFF
