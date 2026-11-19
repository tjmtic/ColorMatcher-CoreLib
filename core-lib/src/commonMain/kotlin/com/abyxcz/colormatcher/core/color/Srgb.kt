package com.abyxcz.colormatcher.core.color

/** An 8-bit sRGB color, the form colors arrive in: hex codes, camera pixels, palette files. */
data class Srgb(val r: Int, val g: Int, val b: Int) {
    init {
        require(r in 0..MAX && g in 0..MAX && b in 0..MAX) {
            "sRGB channels are 0..255: ($r, $g, $b)"
        }
    }

    /** `#RRGGBB`, upper case. */
    fun toHex(): String =
        "#" + listOf(r, g, b).joinToString("") { it.toString(16).padStart(2, '0') }.uppercase()

    companion object {
        private const val MAX = 255

        /**
         * The color of a packed ARGB pixel (alpha in the top byte, then red, green, blue); alpha is
         * ignored.
         */
        fun fromArgb(argb: Int): Srgb =
            Srgb((argb shr 16) and MAX, (argb shr 8) and MAX, argb and MAX)

        /** Parses `#RRGGBB`, `RRGGBB`, `#RGB` or `RGB`; null for anything else. */
        fun fromHex(hex: String): Srgb? {
            val digits = hex.trim().removePrefix("#")
            val full =
                when (digits.length) {
                    3 -> digits.map { "$it$it" }.joinToString("")
                    6 -> digits
                    else -> null
                }
            val value = full?.toIntOrNull(16)
            return value?.let { Srgb((it shr 16) and MAX, (it shr 8) and MAX, it and MAX) }
        }
    }
}
