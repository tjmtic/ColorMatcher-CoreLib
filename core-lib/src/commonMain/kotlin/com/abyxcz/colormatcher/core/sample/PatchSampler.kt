package com.abyxcz.colormatcher.core.sample

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.color.deltaE2000
import com.abyxcz.colormatcher.core.color.linearToSrgb
import com.abyxcz.colormatcher.core.color.srgbToLinear
import com.abyxcz.colormatcher.core.color.toLab

/**
 * A camera frame (or any image) as packed ARGB pixels, row by row: `argb[y * width + x]`, alpha in
 * the top byte. The app's camera adapter fills it; nothing here knows a platform image type.
 */
class PixelBuffer(val width: Int, val height: Int, val argb: IntArray) {
    init {
        require(width > 0 && height > 0) { "a buffer has pixels: $width x $height" }
        require(argb.size == width * height) { "${argb.size} pixels for $width x $height" }
    }

    fun pixel(x: Int, y: Int): Int = argb[y * width + x]
}

/** A circle of pixels around ([centerX], [centerY]); [radius] 0 is the one pixel. */
data class Patch(val centerX: Int, val centerY: Int, val radius: Int) {
    init {
        require(radius >= 0) { "radius must not be negative: $radius" }
    }
}

/**
 * The averaged color of a patch, how many pixels went into it, and its [spread]: the ΔE00 from the
 * average within which 90% of the patch's pixels fall. A small spread is one flat color; a large
 * one is marbled clay, a shadow edge or a highlight — the app should say the match is uncertain.
 */
data class PatchSample(val color: Srgb, val pixelCount: Int, val spread: Double)

/**
 * Samples [patch] of [buffer]: averages its opaque pixels in linear light (light adds up linearly,
 * gamma-encoded values do not), then measures the spread. Pixels outside the buffer are skipped,
 * and so are any not fully opaque. Null when no pixel is left.
 */
fun samplePatch(buffer: PixelBuffer, patch: Patch): PatchSample? {
    val pixels = patchPixels(buffer, patch)
    if (pixels.isEmpty()) return null
    val mean =
        Srgb(
            linearMean(pixels) { it.r },
            linearMean(pixels) { it.g },
            linearMean(pixels) { it.b },
        )
    val meanLab = mean.toLab()
    val distances = pixels.map { deltaE2000(meanLab, it.toLab()) }.sorted()
    val index = ((distances.size - 1) * SPREAD_QUANTILE).toInt()
    return PatchSample(mean, pixels.size, distances[index])
}

private fun patchPixels(buffer: PixelBuffer, patch: Patch): List<Srgb> {
    val r = patch.radius
    val ys = maxOf(0, patch.centerY - r)..minOf(buffer.height - 1, patch.centerY + r)
    val xs = maxOf(0, patch.centerX - r)..minOf(buffer.width - 1, patch.centerX + r)
    return ys.flatMap { y -> xs.map { x -> x to y } }
        .filter { (x, y) -> square(x - patch.centerX) + square(y - patch.centerY) <= r * r }
        .map { (x, y) -> buffer.pixel(x, y) }
        .filter { it ushr ALPHA_SHIFT == OPAQUE }
        .map(Srgb::fromArgb)
}

private fun square(n: Int): Int = n * n

private inline fun linearMean(pixels: List<Srgb>, channel: (Srgb) -> Int): Int =
    linearToSrgb(pixels.sumOf { srgbToLinear(channel(it)) } / pixels.size)

private const val SPREAD_QUANTILE = 0.9
private const val ALPHA_SHIFT = 24
private const val OPAQUE = 0xFF
