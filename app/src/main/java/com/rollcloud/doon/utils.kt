package com.rollcloud.doon

import kotlin.math.*

fun Double.toHoursAndDays(signed: Boolean = false): String {
  val sign = this.sign
  val whole = floor(this.absoluteValue).toInt()
  val decimal = this.absoluteValue - whole
  val denominator = 24
  val numerator = (decimal * denominator).roundToInt()

  return "${if (signed) if (sign < 0 ) '-' else '+' else ""}" +
    (if (numerator > 0) "$numerator hrs " else "") +
    if (whole >= 1) "$whole days" else ""
}
