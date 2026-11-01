package com.abyxcz.colormatcher.core.color

import kotlin.math.cbrt
import kotlin.math.pow

/**
 * A CIELAB color (D65 white point, 2° observer). Matching happens here, not in sRGB: equal
 * distances in Lab are close to equal differences to the eye, which is what ΔE measures.
 */
data class Lab(val l: Double, val a: Double, val b: Double)

/** sRGB (IEC 61966-2-1) → linear RGB → CIE XYZ (D65) → CIELAB (CIE 15:2004). */
fun Srgb.toLab(): Lab {
    val rl = linearize(r)
    val gl = linearize(g)
    val bl = linearize(b)

    val x = 0.4124564 * rl + 0.3575761 * gl + 0.1804375 * bl
    val y = 0.2126729 * rl + 0.7151522 * gl + 0.0721750 * bl
    val z = 0.0193339 * rl + 0.1191920 * gl + 0.9503041 * bl

    val fx = labF(x / WHITE_X)
    val fy = labF(y / WHITE_Y)
    val fz = labF(z / WHITE_Z)
    return Lab(l = 116.0 * fy - 16.0, a = 500.0 * (fx - fy), b = 200.0 * (fy - fz))
}

private const val WHITE_X = 0.95047
private const val WHITE_Y = 1.0
private const val WHITE_Z = 1.08883

private fun linearize(channel: Int): Double {
    val c = channel / 255.0
    return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}

private fun labF(t: Double): Double {
    val epsilon = 216.0 / 24389.0
    val kappa = 24389.0 / 27.0
    return if (t > epsilon) cbrt(t) else (kappa * t + 16.0) / 116.0
}
