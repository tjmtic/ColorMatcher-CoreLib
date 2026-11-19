package com.abyxcz.colormatcher.core.sample

import com.abyxcz.colormatcher.core.color.Srgb
import com.abyxcz.colormatcher.core.color.WhiteBalance

/** Whether a measurement was white-balanced against a reference card, and if not, why. */
enum class BalanceStatus {
    /** Corrected against the card: the color is trustworthy. */
    BALANCED,

    /** No card was marked: the color is as the camera saw it, under unknown light. */
    NO_CARD,

    /** A card was marked but could not be used (too dark, overexposed, or off the image). */
    CARD_UNUSABLE,
}

/**
 * A clay's measured color, ready to match: white-balanced when [balance] is
 * [BalanceStatus.BALANCED], otherwise as sampled. [spread] is the clay patch's (see [PatchSample]);
 * [uneven] says it is too large for one color to stand for the patch.
 */
data class ClayMeasurement(val color: Srgb, val spread: Double, val balance: BalanceStatus) {
    val uneven: Boolean
        get() = spread > UNEVEN_SPREAD
}

/**
 * Measures the clay at [clay] in [buffer]. When [card] marks a reference card of known [cardColor]
 * in the same image, the clay is white-balanced against it; a card that cannot be sampled or
 * trusted leaves the clay uncorrected and says so. Null when the clay patch has no pixel to sample.
 */
fun measureClay(buffer: PixelBuffer, clay: Patch, card: Patch?, cardColor: Srgb): ClayMeasurement? {
    val sample = samplePatch(buffer, clay) ?: return null
    val cardSample = card?.let { samplePatch(buffer, it) }
    val balance = cardSample?.let { WhiteBalance.fromReference(it.color, cardColor) }
    val status =
        when {
            card == null -> BalanceStatus.NO_CARD
            balance == null -> BalanceStatus.CARD_UNUSABLE
            else -> BalanceStatus.BALANCED
        }
    val color = balance?.correct(sample.color) ?: sample.color
    return ClayMeasurement(color, sample.spread, status)
}

/** ΔE00: past this, the patch is visibly more than one color (see Closeness.NOTICEABLE). */
private const val UNEVEN_SPREAD = 5.0
