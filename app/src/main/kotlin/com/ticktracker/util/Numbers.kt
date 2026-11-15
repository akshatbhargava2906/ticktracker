package com.ticktracker.util

import java.math.BigDecimal
import java.math.RoundingMode

/** An amount without a needless decimal part: 5.0 becomes "5", 2.50 becomes "2.5". */
fun formatAmount(value: Double): String =
    BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()

/** Keeps only digits and a single decimal point, so typed amounts are always plain numbers. */
fun cleanDecimalInput(text: String): String {
    val filtered = text.filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    val single = if (firstDot < 0) {
        filtered
    } else {
        filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
    }
    return single.take(9)
}
