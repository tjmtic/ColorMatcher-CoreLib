package com.abyxcz.colormatcher.core.color

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The sRGB transfer function (IEC 61966-2-1): an 8-bit channel to linear light, 0..1. Light adds up
 * linearly, gamma-encoded channel values do not, so averaging pixels and white-balancing both
 * happen here.
 */
internal fun srgbToLinear(channel: Int): Double {
    val c = channel / 255.0
    return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}

/** The inverse of [srgbToLinear]: linear light, clamped to 0..1, to an 8-bit channel, rounded. */
internal fun linearToSrgb(value: Double): Int {
    val v = value.coerceIn(0.0, 1.0)
    val c = if (v <= 0.0031308) v * 12.92 else 1.055 * v.pow(1.0 / 2.4) - 0.055
    return (c * 255.0).roundToInt().coerceIn(0, 255)
}
