package com.rollcloud.doon

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.CircleSpan
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sign

fun Float.toHoursAndDays(signed: Boolean = false): String {
  val sign = this.sign
  val whole = floor(this.absoluteValue).toInt()
  val decimal = this.absoluteValue - whole
  val denominator = 24
  val numerator = (decimal * denominator).roundToInt()

  return "${if (signed) if (sign < 0 ) '-' else '+' else ""}" +
    (if (numerator > 0) "$numerator hrs " else "") +
    if (whole >= 1) "$whole days" else ""
}

class ActionDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {
  private val dates: HashSet<CalendarDay>

  override fun shouldDecorate(day: CalendarDay): Boolean {
    return dates.contains(day)
  }

  override fun decorate(view: DayViewFacade) {
    view.addSpan(CircleSpan(color))
  }

  init {
    this.dates = HashSet(dates)
  }
}
