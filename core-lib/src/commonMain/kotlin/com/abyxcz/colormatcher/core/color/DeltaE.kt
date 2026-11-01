package com.abyxcz.colormatcher.core.color

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * CIEDE2000 color difference (CIE 142-2001), with kL = kC = kH = 1. Implemented and tested against
 * Sharma, Wu & Dalal (2005), "The CIEDE2000 Color-Difference Formula: Implementation Notes,
 * Supplementary Test Data, and Mathematical Observations".
 */
fun deltaE2000(x: Lab, y: Lab): Double {
    val c1 = hypot(x.a, x.b)
    val c2 = hypot(y.a, y.b)
    val cBar7 = ((c1 + c2) / 2.0).pow(7)
    val g = 0.5 * (1.0 - sqrt(cBar7 / (cBar7 + POW25_7)))

    val a1 = (1.0 + g) * x.a
    val a2 = (1.0 + g) * y.a
    val cp1 = hypot(a1, x.b)
    val cp2 = hypot(a2, y.b)
    val hp1 = hueDegrees(x.b, a1)
    val hp2 = hueDegrees(y.b, a2)

    val dL = y.l - x.l
    val dC = cp2 - cp1
    val dh =
        when {
            cp1 * cp2 == 0.0 -> 0.0
            abs(hp2 - hp1) <= 180.0 -> hp2 - hp1
            hp2 - hp1 > 180.0 -> hp2 - hp1 - 360.0
            else -> hp2 - hp1 + 360.0
        }
    val dH = 2.0 * sqrt(cp1 * cp2) * sin(rad(dh / 2.0))

    val lBar = (x.l + y.l) / 2.0
    val cpBar = (cp1 + cp2) / 2.0
    val hpBar =
        when {
            cp1 * cp2 == 0.0 -> hp1 + hp2
            abs(hp1 - hp2) <= 180.0 -> (hp1 + hp2) / 2.0
            hp1 + hp2 < 360.0 -> (hp1 + hp2 + 360.0) / 2.0
            else -> (hp1 + hp2 - 360.0) / 2.0
        }

    val t =
        1.0 - 0.17 * cos(rad(hpBar - 30.0)) +
            0.24 * cos(rad(2.0 * hpBar)) +
            0.32 * cos(rad(3.0 * hpBar + 6.0)) - 0.20 * cos(rad(4.0 * hpBar - 63.0))
    val dTheta = 30.0 * exp(-((hpBar - 275.0) / 25.0).pow(2))
    val cpBar7 = cpBar.pow(7)
    val rc = 2.0 * sqrt(cpBar7 / (cpBar7 + POW25_7))
    val lBar50 = (lBar - 50.0).pow(2)
    val sl = 1.0 + 0.015 * lBar50 / sqrt(20.0 + lBar50)
    val sc = 1.0 + 0.045 * cpBar
    val sh = 1.0 + 0.015 * cpBar * t
    val rt = -sin(rad(2.0 * dTheta)) * rc

    val tl = dL / sl
    val tc = dC / sc
    val th = dH / sh
    return sqrt(tl * tl + tc * tc + th * th + rt * tc * th)
}

private const val POW25_7 = 6103515625.0 // 25^7

private fun rad(degrees: Double): Double = degrees * PI / 180.0

private fun hueDegrees(b: Double, a: Double): Double {
    if (a == 0.0 && b == 0.0) return 0.0
    val h = atan2(b, a) * 180.0 / PI
    return if (h < 0) h + 360.0 else h
}
