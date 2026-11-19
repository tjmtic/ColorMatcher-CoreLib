package com.abyxcz.colormatcher.core.color

/**
 * A white balance: per-channel gains in linear light, von Kries style in linear sRGB. Made from a
 * reference card in the frame: [fromReference] takes how the camera saw the card and the card's
 * known color, and [correct] applies the same gains to anything else in that frame.
 */
data class WhiteBalance(val red: Double, val green: Double, val blue: Double) {
    /** [color] as it would look under the light the reference was measured for. */
    fun correct(color: Srgb): Srgb =
        Srgb(
            linearToSrgb(srgbToLinear(color.r) * red),
            linearToSrgb(srgbToLinear(color.g) * green),
            linearToSrgb(srgbToLinear(color.b) * blue),
        )

    companion object {
        /** Changes nothing: for a frame with no reference card. */
        val NONE = WhiteBalance(1.0, 1.0, 1.0)

        /**
         * The balance that turns [observed] (the card as the camera saw it) into [reference] (the
         * card's known color). Null when the observation cannot be trusted: a channel too dark to
         * divide by (under 1% linear light — the card is shadowed or not in frame), or clipped at
         * 255 (overexposed, so its true value is unknown).
         */
        fun fromReference(observed: Srgb, reference: Srgb): WhiteBalance? {
            val channels = listOf(observed.r, observed.g, observed.b)
            val usable = channels.all { srgbToLinear(it) >= MIN_LINEAR && it < CLIPPED }
            return if (!usable) null
            else
                WhiteBalance(
                    red = srgbToLinear(reference.r) / srgbToLinear(observed.r),
                    green = srgbToLinear(reference.g) / srgbToLinear(observed.g),
                    blue = srgbToLinear(reference.b) / srgbToLinear(observed.b),
                )
        }

        private const val MIN_LINEAR = 0.01
        private const val CLIPPED = 255
    }
}
